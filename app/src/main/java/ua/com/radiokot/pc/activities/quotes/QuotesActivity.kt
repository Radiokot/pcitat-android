package ua.com.radiokot.pc.activities.quotes

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
import kotlinx.android.synthetic.main.activity_quotes.*
import kotlinx.android.synthetic.main.default_toolbar.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import kotlinx.android.synthetic.main.quotes_book_appbar.*
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.NavigationActivity
import ua.com.radiokot.pc.logic.repository.QuotesRepository
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.util.extensions.getStringExtra
import ua.com.radiokot.pc.view.util.HideFabOnScrollListener
import ua.com.radiokot.pc.view.util.LoadingIndicatorManager
import ua.com.radiokot.pc.view.util.TypefaceUtil

class QuotesActivity : NavigationActivity() {
    companion object {
        val BOOK_ID_EXTRA = "book_id"
        val BOOK_TITLE_EXTRA = "book_title"
        val BOOK_AUTHOR_EXTRA = "book_author"
        val BOOK_IS_TWITTER_EXTRA = "book_is_twitter"
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
            twitterMenuItem?.icon =
                    if (value)
                        ContextCompat.getDrawable(this, R.drawable.ic_twitter)
                    else
                        ContextCompat.getDrawable(this, R.drawable.ic_twitter_outline)
        }
    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { swipe_refresh.isRefreshing = true },
            hideLoading = { swipe_refresh.isRefreshing = false }
    )

    private val quotesRepository: QuotesRepository
        get() =
            if (withBook)
                Repositories.quotes(bookId)
            else
                Repositories.quotes()
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
            isBookUsedForTwitter = isBookUsedForTwitter

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

        quotesAdapter.needBookTitles = !withBook
        quotesAdapter.setData(listOf())

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
                .subscribe { loadingIndicator.setLoading(it, "quotes") }

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

    private fun update(force: Boolean = false) {
        if (!force) {
            quotesRepository.updateIfNotFresh()
        } else {
            quotesRepository.update()
        }
    }

    private fun displayQuotes() {
        val quotes = quotesRepository.itemsSubject.value
        quotesAdapter.setData(quotes)
    }
}
