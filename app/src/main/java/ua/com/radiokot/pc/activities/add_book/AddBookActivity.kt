package ua.com.radiokot.pc.activities.add_book

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_add_book.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import kotlinx.android.synthetic.main.layout_progress.*
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.BaseActivity
import ua.com.radiokot.pc.logic.books_search.LiveLibBookSearcher
import ua.com.radiokot.pc.logic.exceptions.ConflictException
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.ToastManager
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import java.util.concurrent.TimeUnit

class AddBookActivity : BaseActivity() {
    companion object {
        val ADD_BOOK_REQUEST = "add_book".hashCode() and 0xffff
    }

    private val searchQuerySubject = BehaviorSubject.createDefault("")
    private val booksAdapter = SuggestedBooksAdapter()
    private val bookSearcher = LiveLibBookSearcher()

    private val booksRepository: BooksRepository
        get() = Repositories.books()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        initToolbar(R.string.add_book_title)

        initSearch()
        initBooksList()
    }

    // region Init
    private fun initSearch() {
        add_book_search_view.onActionViewExpanded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add_book_search_view.importantForAutofill =
                    View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        add_book_search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    val newQuery = query.trim()
                    if (newQuery != searchQuerySubject.value) {
                        searchQuerySubject.onNext(newQuery)
                    }
                }
                return true
            }
        })

        subscribeToSearchQuery()
    }

    private fun subscribeToSearchQuery() {
        searchQuerySubject
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .debounce(1, TimeUnit.SECONDS)
                .compose(ObservableTransformers.defaultSchedulers())
                .subscribe { searchBooks(it) }
    }

    private fun initBooksList() {
        suggested_books_list.apply {
            layoutManager = LinearLayoutManager(this@AddBookActivity)
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = booksAdapter
        }

        error_empty_view.observeAdapter(booksAdapter, R.string.not_found)

        booksAdapter.onItemClick { _, item ->
            addBook(item)
        }
        booksAdapter.setData(listOf())
    }
    // endregion

    private var searchDisposable: Disposable? = null
    private fun searchBooks(query: String) {
        searchDisposable?.dispose()

        if (query.isEmpty()) {
            displayFoundBooks(listOf())
        } else {
            searchDisposable = bookSearcher.search(query)
                    .compose(ObservableTransformers.defaultSchedulers())
                    .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                    .doOnSubscribe {
                        progress.show()
                    }
                    .subscribeBy(
                            onNext = {
                                progress.hide()
                                displayFoundBooks(it)
                            },
                            onError = {
                                progress.hide()
                                if (booksAdapter.hasData) {
                                    ErrorHandlerFactory.getDefault().handle(it)
                                } else {
                                    error_empty_view.showError(it) {
                                        searchBooks(query)
                                    }
                                }
                            }
                    )
        }
    }

    private fun displayFoundBooks(suggestions: Collection<ExternalSiteBook>) {
        booksAdapter.setData(suggestions)
    }

    var addingProgressDialog: ProgressDialog? = null
    private fun displayAddingProgress(cancelListener: DialogInterface.OnCancelListener? = null) {
        hideAddingProgress()
        addingProgressDialog = ProgressDialog.show(this, null,
                getString(R.string.book_adding_progress), true, true)
        addingProgressDialog?.setOnCancelListener(cancelListener)
    }

    private fun hideAddingProgress() {
        addingProgressDialog?.dismiss()
    }

    private var addBookDisposable: Disposable? = null
    private fun addBook(book: ExternalSiteBook) {
        addBookDisposable?.dispose()
        addBookDisposable = booksRepository.addExternalBook(book)
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .doOnSubscribe {
                    displayAddingProgress(DialogInterface.OnCancelListener {
                        addBookDisposable?.dispose()
                    })
                }
                .subscribeBy(
                        onNext = {
                            hideAddingProgress()
                            ToastManager.short(R.string.book_added)
                            setResult(Activity.RESULT_OK)
                            finish()
                        },
                        onError = {
                            hideAddingProgress()
                            when (it) {
                                is ConflictException ->
                                    ToastManager.long(getString(R.string.error_book_name_already_added,
                                            book.title))
                                else -> ErrorHandlerFactory.getDefault().handle(it)
                            }
                        }
                )
    }
}
