package ua.com.radiokot.pc.activities.books

import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.SimpleItemAnimator
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_books.*
import kotlinx.android.synthetic.main.default_toolbar.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import org.jetbrains.anko.onClick
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.NavigationActivity
import ua.com.radiokot.pc.logic.AuthManager
import ua.com.radiokot.pc.logic.event_bus.events.BookAddedEvent
import ua.com.radiokot.pc.logic.event_bus.events.PcEvent
import ua.com.radiokot.pc.logic.event_bus.events.TwitterBookChangedEvent
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.SearchUtil
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.view.util.HideFabOnScrollListener
import ua.com.radiokot.pc.view.util.LoadingIndicatorManager
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class BooksActivity : NavigationActivity() {
    companion object {
        private val minBookWidthDp = 140
        val BOOK_COVER_PROPORTION = 1.42
    }

    private val booksRepository: BooksRepository
        get() = Repositories.books()
    private val booksAdapter = BooksAdapter()
    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { swipe_refresh.isRefreshing = true },
            hideLoading = { swipe_refresh.isRefreshing = false }
    )
    private val whitespaceSeriesRegex = " +".toRegex()
    private val searchQuerySubject = BehaviorSubject.createDefault("")
    private val isOnSearch: Boolean
        get() = !searchQuerySubject.value.isEmpty()

    override fun getNavigationItemId(): Long = BOOKS_NAVIGATION_ITEM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isAuthorized()) {
            Navigator.toLoginActivity(this)
            return
        }

        setContentView(R.layout.activity_books)
        initToolbar(titleResId = R.string.my_books, needUpButton = false)
        initNavigation()

        initSwipeRefresh()
        initBooksList()
        initSearch()
        initFab()

        update()
    }

    // region Init
    private fun initSwipeRefresh() {
        swipe_refresh.setColorSchemeResources(R.color.accent)
        swipe_refresh.setOnRefreshListener { update(force = true) }
    }

    private fun initBooksList() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val oneDpInPx = displayMetrics.density

        val screenWidth = displayMetrics.widthPixels
        val screenWidthDp = (screenWidth / oneDpInPx).roundToInt()
        val spanCount = screenWidthDp / minBookWidthDp

        val estimatedBookCoverWidth = screenWidth * 1f / spanCount -
                2 * resources.getDimensionPixelSize(R.dimen.half_standard_margin)
        val bookCoverHeight = estimatedBookCoverWidth * BOOK_COVER_PROPORTION
        booksAdapter.coverHeight = bookCoverHeight.roundToInt()

        booksAdapter.onItemClick { _, book ->
            Navigator.openQuotesActivity(this,
                    book.id, book.title, book.authorName, book.isTwitterBook)
        }

        books_list.apply {
            layoutManager = GridLayoutManager(this@BooksActivity, spanCount)
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = booksAdapter
        }

        error_empty_view.observeAdapter(booksAdapter) {
            getString(if (isOnSearch) R.string.not_found else R.string.books_empty)
        }
        error_empty_view.setEmptyViewDenial { booksRepository.isNeverUpdated }

        subscribeToBooks()
    }

    private var booksItemsDisposable: Disposable? = null
    private var booksLoadingDisposable: Disposable? = null
    private var booksErrorsDisposable: Disposable? = null
    private fun subscribeToBooks() {
        booksItemsDisposable?.dispose()
        booksItemsDisposable = booksRepository.itemsSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe { displayBooks() }

        booksLoadingDisposable?.dispose()
        booksLoadingDisposable = booksRepository.loadingSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe { loadingIndicator.setLoading(it, "books") }

        booksErrorsDisposable?.dispose()
        booksErrorsDisposable = booksRepository.errorsSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe {
                    if (booksAdapter.hasData) {
                        ErrorHandlerFactory.getByNetworkState().handle(it)
                    } else {
                        error_empty_view.showError(it) {
                            update(true)
                        }
                    }
                }
    }

    private fun initSearch() {
        searchQuerySubject
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .debounce(100, TimeUnit.MILLISECONDS)
                .compose(ObservableTransformers.defaultSchedulers())
                .subscribe { displayBooks() }
    }

    private fun initFab() {
        books_list.addOnScrollListener(HideFabOnScrollListener(add_fab))
        add_fab.onClick {
            Navigator.openAddBookActivity(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.books, menu)

        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = getString(R.string.search)
        searchView?.setOnQueryTextFocusChangeListener { _, focused ->
            if (!focused && searchView.query.isBlank()) {
                searchItem.collapseActionView()
            }
        }
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    val newQuery = query.trim().replace(whitespaceSeriesRegex, " ")
                    if (newQuery != searchQuerySubject.value) {
                        searchQuerySubject.onNext(newQuery)
                    }
                }
                return true
            }
        })

        searchItem?.setOnMenuItemClickListener {
            TransitionManager.beginDelayedTransition(toolbar, Fade())
            searchItem.expandActionView()
            hideFabForSearch()
            true
        }

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                showFabAfterSearch()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }
    // endregion

    private fun displayBooks() {
        val books = booksRepository.itemsSubject.value

        val searchQuery = searchQuerySubject.value
        val searchPredicate: (Book) -> Boolean =
                if (isOnSearch)
                    { book ->
                        SearchUtil.isMatchGeneralCondition(searchQuery,
                                listOf(book.title, book.authorName))
                    }
                else
                    { _ -> true }

        booksAdapter.setData(books.filter(searchPredicate))
    }

    private var fabWasVisible = false
    private fun showFabAfterSearch() {
        add_fab.isEnabled = true
        if (fabWasVisible) {
            add_fab.show()
        }
    }

    private fun hideFabForSearch() {
        fabWasVisible = add_fab.visibility == View.VISIBLE
        add_fab.isEnabled = false
        add_fab.hide()
    }

    private fun update(force: Boolean = false) {
        if (!force) {
            booksRepository.updateIfNotFresh()
        } else {
            booksRepository.update()
        }
    }

    override fun onPcEvent(event: PcEvent) {
        when (event) {
            is BookAddedEvent ->
                books_list.smoothScrollToPosition(0)
            is TwitterBookChangedEvent ->
                booksAdapter.notifyDataSetChanged()
        }
    }
}
