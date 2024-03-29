package ua.com.radiokot.pc.activities.add_book

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import okhttp3.HttpUrl
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.BaseActivity
import ua.com.radiokot.pc.databinding.ActivityAddBookBinding
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

    private lateinit var view: ActivityAddBookBinding

    private val searchQuerySubject = BehaviorSubject.createDefault("")
    private val booksAdapter = SuggestedBooksAdapter()
    private val bookSearcher = LiveLibBookSearcher()

    private val booksRepository: BooksRepository
        get() = Repositories.books()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(view.root)

        initToolbar(R.string.add_book_title)

        initSearch()
        initBooksList()
        initButtons()
    }

    // region Init
    private fun initSearch() {
        view.addBookSearchView.onActionViewExpanded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.addBookSearchView.importantForAutofill =
                View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        view.addBookSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    val newQuery = query.trim()
                    val parsedUrl = HttpUrl.parse(newQuery)
                    if (parsedUrl != null) {
                        searchQuerySubject.onNext("")
                    } else if (newQuery != searchQuerySubject.value) {
                        searchQuerySubject.onNext(newQuery)
                    }
                    view.addByLinkButton.isVisible = parsedUrl != null
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
        view.suggestedBooksList.apply {
            layoutManager = LinearLayoutManager(this@AddBookActivity)
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = booksAdapter
        }

        view.includeErrorEmptyView.errorEmptyView.observeAdapter(booksAdapter, R.string.not_found)

        booksAdapter.onItemClick { _, item ->
            addBook(item)
        }
        booksAdapter.setData(listOf())
    }

    private fun initButtons() {
        view.addByLinkButton.setOnClickListener {
            val queryString = view.addBookSearchView.query.trim().toString()
            if (HttpUrl.parse(queryString) != null) {
                addBook(ExternalSiteBook(externalUrl = queryString))
            }
        }
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
                    view.includeProgress.progress.show()
                }
                .subscribeBy(
                    onNext = {
                        view.includeProgress.progress.hide()
                        displayFoundBooks(it)
                    },
                    onError = {
                        view.includeProgress.progress.hide()
                        if (booksAdapter.hasData) {
                            ErrorHandlerFactory.getDefault().handle(it)
                        } else {
                            view.includeErrorEmptyView.errorEmptyView.showError(it) {
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
        addingProgressDialog = ProgressDialog.show(
            this, null,
            getString(R.string.book_adding_progress), true, true
        )
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
                            ToastManager.long(
                                if (book.title != null)
                                    getString(
                                        R.string.error_book_name_already_added,
                                        book.title
                                    )
                                else
                                    getString(R.string.error_book_already_added)
                            )

                        else -> ErrorHandlerFactory.getDefault().handle(it)
                    }
                }
            )
    }
}
