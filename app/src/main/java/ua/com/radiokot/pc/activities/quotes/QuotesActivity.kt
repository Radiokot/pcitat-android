package ua.com.radiokot.pc.activities.quotes

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.StaticLayout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.NavigationActivity
import ua.com.radiokot.pc.databinding.ActivityQuotesBinding
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
import ua.com.radiokot.pc.view.util.ScrollOnTopItemUpdateAdapterObserver
import ua.com.radiokot.pc.view.util.TypefaceUtil

class QuotesActivity : NavigationActivity() {
    companion object {
        val UPDATE_BOOK_REQUEST = "update_book".hashCode() and 0xffff

        const val BOOK_ID_EXTRA = "book_id"
        const val BOOK_TITLE_EXTRA = "book_title"
        const val BOOK_AUTHOR_EXTRA = "book_author"
        const val BOOK_IS_TWITTER_EXTRA = "book_is_twitter"
    }

    private lateinit var view: ActivityQuotesBinding

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

    private val collapsingToolbar: CollapsingToolbarLayout by lazy {
        findViewById(R.id.collapsing_toolbar)
    }
    private val bookAuthorTextView: TextView by lazy {
        findViewById(R.id.book_author_text_view)
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

        view = ActivityQuotesBinding.inflate(layoutInflater)
        setContentView(view.root)

        initToolbar()
        initFab()
        initSwipeRefresh()
        initQuotesList()

        isBookUsedForTwitter = intent.getBooleanExtra(BOOK_IS_TWITTER_EXTRA, false)

        update()
    }

    override fun getToolbar(): Toolbar? =
        if (withBook)
            findViewById(R.id.collapsing_inner_toolbar)
        else
            findViewById(R.id.toolbar)

    // region Init
    private fun initToolbar() = if (!withBook) {
        view.appbarViewStub.layoutResource = R.layout.default_appbar
        view.appbarViewStub.inflate()

        initToolbar(R.string.my_quotes, false)
        initNavigation()
    } else {
        view.appbarViewStub.layoutResource = R.layout.quotes_book_appbar
        view.appbarViewStub.inflate()

        initToolbar(bookTitle)
        collapsingToolbar.title = bookTitle
        bookAuthorTextView.text = bookAuthor

        TypefaceUtil.getCondensedBold().let {
            collapsingToolbar.setCollapsedTitleTypeface(it)
            collapsingToolbar.setExpandedTitleTypeface(it)
        }

        findViewById<AppBarLayout>(R.id.collapsing_appbar).targetElevation = 0f
        view.includeAppbarElevation.appbarElevationView.visibility = View.VISIBLE

        bookAuthorTextView.typeface = TypefaceUtil.getRobotoSlabRegular()
    }

    // TODO: This is no more working.
    private fun getExpandedTitleLinesCount(): Int? {
        // I really don't want to rebuild the library, so
        // let's do some reflection.
        try {
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
            view.addFab.visibility = View.GONE
            view.addFab.isEnabled = false
        } else {
            view.quotesList.addOnScrollListener(HideFabOnScrollListener(view.addFab))

            view.addFab.setOnClickListener {
                Navigator.openAddQuoteActivity(this, bookId)
            }
        }
    }

    private fun initSwipeRefresh() {
        view.swipeRefresh.setColorSchemeResources(R.color.colorAccent)
        view.swipeRefresh.setOnRefreshListener { update(force = true) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (withBook) {
            menuInflater.inflate(R.menu.book, menu)

            twitterMenuItem = menu.findItem(R.id.use_for_twitter).apply {
                MenuItemCompat.setIconTintList(
                    this,
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@QuotesActivity,
                            R.color.icon
                        )
                    )
                )
            }

            updateTwitterIndicator()
            updateBookAuthorSpacing()
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateBookAuthorSpacing() {
        collapsingToolbar.post {
            getExpandedTitleLinesCount()?.let { linesCount ->
                if (linesCount > 1) {
                    bookAuthorTextView.setPadding(
                        0,
                        resources.getDimensionPixelSize(
                            R.dimen.expanded_book_author_extra_padding_top
                        ),
                        0, 0
                    )
                }
            }
            AnimationUtil.fadeInView(bookAuthorTextView)
        }
    }

    private fun initQuotesList() {
        view.quotesList.apply {
            layoutManager = LinearLayoutManager(this@QuotesActivity)
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = quotesAdapter
        }

        quotesAdapter.registerAdapterDataObserver(ScrollOnTopItemUpdateAdapterObserver(view.quotesList))

        view.includeErrorEmptyView.errorEmptyView.observeAdapter(
            quotesAdapter,
            R.string.quotes_empty
        )
        view.includeErrorEmptyView.errorEmptyView.setEmptyViewDenial { quotesRepository.isNeverUpdated }

        quotesAdapter.apply {
            needBookTitles = !withBook
            setData(listOf())
            onItemClick { _, item ->
                Navigator.openEditQuoteActivity(
                    this@QuotesActivity, item.source.bookId,
                    item.bookTitle, item.id, item.text, item.source.isPublic
                )
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
            .subscribe { view.swipeRefresh.isRefreshing = it }

        quotesErrorsDisposable?.dispose()
        quotesErrorsDisposable = quotesRepository.errorsSubject
            .compose(ObservableTransformers.defaultSchedulers())
            .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
            .subscribe {
                if (quotesAdapter.hasData) {
                    ErrorHandlerFactory.getByNetworkState().handle(it)
                } else {
                    view.includeErrorEmptyView.errorEmptyView.showError(it) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
            ?: return

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
                view.includeProgress.progress.show()
            }
            .doOnTerminate {
                view.includeProgress.progress.hide()
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
        deleteProgressDialog = ProgressDialog.show(
            this, null,
            getString(R.string.book_deleting_progress), true, true
        )
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
