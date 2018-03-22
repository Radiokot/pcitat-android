package ua.com.radiokot.pc.activities.quotes

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.support.v7.widget.Toolbar
import android.text.StaticLayout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_quotes.*
import kotlinx.android.synthetic.main.default_toolbar.*
import kotlinx.android.synthetic.main.include_appbar_elevation.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import kotlinx.android.synthetic.main.layout_progress.*
import kotlinx.android.synthetic.main.quotes_book_appbar.*
import org.jetbrains.anko.onClick
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.NavigationActivity
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.logic.repository.base.MultipleItemsRepository
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.ToastManager
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.util.extensions.getStringExtra
import ua.com.radiokot.pc.view.dialog.ConfirmationDialog
import ua.com.radiokot.pc.view.util.AnimationUtil
import ua.com.radiokot.pc.view.util.HideFabOnScrollListener
import ua.com.radiokot.pc.view.util.TypefaceUtil

class QuotesActivity : NavigationActivity() {
    companion object {
        val UPDATE_BOOK_REQUEST = "update_book".hashCode() and 0xffff

        const val BOOK_ID_EXTRA = "book_id"
        const val BOOK_TITLE_EXTRA = "book_title"
        const val BOOK_AUTHOR_EXTRA = "book_author"
        const val BOOK_IS_TWITTER_EXTRA = "book_is_twitter"
    }

    private val bookId: Long
        get() = intent.getLongExtra(BOOK_ID_EXTRA, 0)
    private val bookTitle: String
        get() = intent.getStringExtra(BOOK_TITLE_EXTRA, "")
    private val bookAuthor: String
        get() = intent.getStringExtra(BOOK_AUTHOR_EXTRA, "")
    private val withBook: Boolean
        get() = bookId > 0

    private var twitterMenuItem: MenuItem? = null
    private var isBookUsedForTwitter = false
        set(value) {
            field = value
            updateTwitterIndicator()
        }

    private val quotesRepository: MultipleItemsRepository<Quote>
        get() =
            if (withBook)
                Repositories.quotes(bookId)
            else
                Repositories.quotes()
    private val booksRepository: BooksRepository
        get() = Repositories.books()
    private val quotesAdapter = QuotesAdapter()

    override fun getNavigationItemId(): Long = QUOTES_NAVIGATION_ITEM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)

        initToolbar()
        initFab()
        initSwipeRefresh()
        initQuotesList()

        isBookUsedForTwitter = intent.getBooleanExtra(BOOK_IS_TWITTER_EXTRA, false)

        update()
    }

    override fun getToolbar(): Toolbar? {
        return if (withBook) collapsing_inner_toolbar else toolbar
    }

    // region Init
    private fun initToolbar() {
        if (!withBook) {
            appbar_view_stub.layoutResource = R.layout.default_appbar
            appbar_view_stub.inflate()

            initToolbar(R.string.my_quotes, false)
            initNavigation()
        } else {
            appbar_view_stub.layoutResource = R.layout.quotes_book_appbar
            appbar_view_stub.inflate()

            initToolbar(bookTitle)
            collapsing_toolbar.title = bookTitle
            book_author_text_view.text = bookAuthor

            TypefaceUtil.getCondensedBold().let {
                collapsing_toolbar.setCollapsedTitleTypeface(it)
                collapsing_toolbar.setExpandedTitleTypeface(it)
            }

            collapsing_appbar.targetElevation = 0f
            appbar_elevation_view.visibility = View.VISIBLE

            book_author_text_view.typeface = TypefaceUtil.getRobotoSlabRegular()
        }
    }

    private fun getExpandedTitleLinesCount(): Int? {
        // I really don't want to rebuild the library, so
        // let's do some reflection.
        try {
            val collapsingToolbar = collapsing_toolbar ?: return null
            val collapsingTextHelper =
                    collapsingToolbar.javaClass.getDeclaredField("mCollapsingTextHelper")
                            .apply { isAccessible = true }
                            .get(collapsingToolbar) ?: return null
            val textLayout =
                    collapsingTextHelper.javaClass.getDeclaredField("mTextLayout")
                            .apply { isAccessible = true }
                            .get(collapsingTextHelper) as? StaticLayout

            return textLayout?.lineCount
        } catch (e: Exception) {
            return null
        }
    }

    private fun initFab() {
        if (!withBook) {
            add_fab.visibility = View.GONE
            add_fab.isEnabled = false
        } else {
            quotes_list.addOnScrollListener(HideFabOnScrollListener(add_fab))

            add_fab.onClick {
                Navigator.openAddQuoteActivity(this, bookId)
            }
        }
    }

    private fun initSwipeRefresh() {
        swipe_refresh.setColorSchemeResources(R.color.accent)
        swipe_refresh.setOnRefreshListener { update(force = true) }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (withBook) {
            menuInflater.inflate(R.menu.book, menu)

            twitterMenuItem = menu?.findItem(R.id.use_for_twitter)
            MenuItemCompat.setIconTintList(twitterMenuItem,
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.icon)))

            updateTwitterIndicator()
            updateBookAuthorSpacing()
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateBookAuthorSpacing() {
        collapsing_toolbar.post {
            getExpandedTitleLinesCount()?.let { linesCount ->
                if (linesCount > 1) {
                    book_author_text_view.setPadding(0,
                            resources.getDimensionPixelSize(
                                    R.dimen.expanded_book_author_extra_padding_top),
                            0, 0)
                }
            }
            AnimationUtil.fadeInView(book_author_text_view)
        }
    }

    private fun initQuotesList() {
        quotes_list.apply {
            layoutManager = LinearLayoutManager(this@QuotesActivity)
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = quotesAdapter
        }

        error_empty_view.observeAdapter(quotesAdapter, R.string.quotes_empty)
        error_empty_view.setEmptyViewDenial { quotesRepository.isNeverUpdated }

        quotesAdapter.apply {
            needBookTitles = !withBook
            setData(listOf())
            onItemClick { _, item ->
                Navigator.openEditQuoteActivity(this@QuotesActivity, item.bookId,
                        item.id, item.text)
            }
        }

        subscribeToQuotes()
    }

    private var quotesItemsDisposable: Disposable? = null
    private var quotesLoadingDisposable: Disposable? = null
    private var quotesErrorsDisposable: Disposable? = null
    private fun subscribeToQuotes() {
        quotesItemsDisposable?.dispose()
        quotesItemsDisposable = quotesRepository.itemsSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe { displayQuotes() }

        quotesLoadingDisposable?.dispose()
        quotesLoadingDisposable = quotesRepository.loadingSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe { swipe_refresh.isRefreshing = it }

        quotesErrorsDisposable?.dispose()
        quotesErrorsDisposable = quotesRepository.errorsSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe {
                    if (quotesAdapter.hasData) {
                        ErrorHandlerFactory.getByNetworkState().handle(it)
                    } else {
                        error_empty_view.showError(it) {
                            update(true)
                        }
                    }
                }
    }
    // endregion

    private fun updateTwitterIndicator() {
        twitterMenuItem?.icon =
                if (isBookUsedForTwitter)
                    ContextCompat.getDrawable(this, R.drawable.ic_twitter)
                else
                    ContextCompat.getDrawable(this, R.drawable.ic_twitter_outline)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.use_for_twitter -> tryEnableTwitterExport()
            R.id.delete -> tryToDeleteBook()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun update(force: Boolean = false) {
        if (!force) {
            quotesRepository.updateIfNotFresh()
        } else {
            quotesRepository.update()
        }
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        updateTwitterIndicator()
    }

    private fun displayQuotes() {
        val quotes = quotesRepository.itemsSubject.value
        quotesAdapter.setData(quotes.map { QuoteListItem(it) })
    }

    // region Twitter export
    private fun tryEnableTwitterExport() {
        if (isBookUsedForTwitter) {
            ToastManager.long(R.string.this_book_is_used_for_twitter_export)
        } else {
            enableTwitterExport()
        }
    }

    private fun enableTwitterExport() {
        booksRepository.setTwitterBook(bookId)
                .compose(ObservableTransformers.defaultSchedulersCompletable())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .doOnSubscribe {
                    progress.show()
                }
                .doOnTerminate {
                    progress.hide()
                }
                .subscribeBy(
                        onComplete = {
                            isBookUsedForTwitter = true
                            ToastManager.long(R.string.twitter_export_enabled)
                            setResult(Activity.RESULT_OK)
                        },
                        onError = {
                            ErrorHandlerFactory.getDefault().handle(it)
                        }
                )
    }
    // endregion

    // region Delete
    private fun tryToDeleteBook() {
        ConfirmationDialog(this)
                .show(getString(R.string.delete_book_name_confirmation, bookTitle)) {
                    deleteBook()
                }
    }

    private var deleteProgressDialog: ProgressDialog? = null
    private fun displayDeleteBookProgress(cancelListener: DialogInterface.OnCancelListener? = null) {
        hideDeleteBookProgress()
        deleteProgressDialog = ProgressDialog.show(this, null,
                getString(R.string.book_deleting_progress), true, true)
        deleteProgressDialog?.setOnCancelListener(cancelListener)
    }

    private fun hideDeleteBookProgress() {
        deleteProgressDialog?.dismiss()
    }

    private var deleteBookDisposable: Disposable? = null
    private fun deleteBook() {
        deleteBookDisposable?.dispose()
        deleteBookDisposable = booksRepository.delete(bookId)
                .compose(ObservableTransformers.defaultSchedulersCompletable())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .doOnSubscribe {
                    displayDeleteBookProgress(DialogInterface.OnCancelListener {
                        deleteBookDisposable?.dispose()
                    })
                }
                .subscribeBy(
                        onComplete = {
                            hideDeleteBookProgress()
                            ToastManager.short(R.string.book_deleted)
                            finish()
                        },
                        onError = {
                            hideDeleteBookProgress()
                            ErrorHandlerFactory.getDefault().handle(it)
                        }
                )
    }
    // endregion
}
