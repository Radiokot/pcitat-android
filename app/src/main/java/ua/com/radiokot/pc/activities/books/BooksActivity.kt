package ua.com.radiokot.pc.activities.books

import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.NavigationActivity
import ua.com.radiokot.pc.databinding.ActivityBooksBinding
import ua.com.radiokot.pc.logic.AuthManager
import ua.com.radiokot.pc.logic.event_bus.events.BookAddedEvent
import ua.com.radiokot.pc.logic.event_bus.events.PcEvent
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.SearchUtil
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.view.util.HideFabOnScrollListener
import ua.com.radiokot.pc.view.util.LoadingIndicatorManager
import ua.com.radiokot.pc.view.util.ScrollOnTopItemUpdateAdapterObserver
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class BooksActivity : NavigationActivity() {
    companion object {
        private const val minBookWidthDp = 140
        const val BOOK_COVER_PROPORTION = 1.42
    }

    private lateinit var view: ActivityBooksBinding

    private val booksRepository: BooksRepository
        get() = Repositories.books()
    private val booksAdapter = BooksAdapter()
    private val loadingIndicator = LoadingIndicatorManager(
        showLoading = { view.swipeRefresh.isRefreshing = true },
        hideLoading = { view.swipeRefresh.isRefreshing = false }
    )
    private val whitespaceSeriesRegex = " +".toRegex()
    private val searchQuerySubject = BehaviorSubject.createDefault("")
    private val isOnSearch: Boolean
        get() = !searchQuerySubject.value.isNullOrEmpty()

    override fun getNavigationItemId(): Long = BOOKS_NAVIGATION_ITEM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isAuthorized()) {
            Navigator.toLoginActivity(this)
            return
        }

        view = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(view.root)

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
        view.swipeRefresh.setColorSchemeResources(R.color.colorAccent)
        view.swipeRefresh.setOnRefreshListener { update(force = true) }
    }

    private fun initBooksList() {
        initBooksRecyclerView()

        booksAdapter.onItemClick { _, book ->
            Navigator.openQuotesActivity(
                this,
                book.id, book.title, book.authorName, book.isTwitterBook
            )
        }

        view.includeErrorEmptyView.errorEmptyView.observeAdapter(booksAdapter) {
            getString(if (isOnSearch) R.string.not_found else R.string.books_empty)
        }
        view.includeErrorEmptyView.errorEmptyView.setEmptyViewDenial { booksRepository.isNeverUpdated }

        subscribeToBooks()
    }

    private fun initBooksRecyclerView(reset: Boolean = false) {
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

        var firstVisibleItem = 0

        if (reset) {
            firstVisibleItem =
                (view.booksList.layoutManager as? LinearLayoutManager)
                    ?.findFirstVisibleItemPosition() ?: 0

            view.booksList.adapter = null
            view.booksList.layoutManager = null
        }

        view.booksList.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

            val gridLayoutManager = GridLayoutManager(this@BooksActivity, spanCount)
            layoutManager = gridLayoutManager
            adapter = booksAdapter

            if (firstVisibleItem > 0) {
                gridLayoutManager.scrollToPosition(firstVisibleItem)
            }
        }

        booksAdapter.registerAdapterDataObserver(ScrollOnTopItemUpdateAdapterObserver(view.booksList))
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
                    view.includeErrorEmptyView.errorEmptyView.showError(it) {
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
        view.booksList.addOnScrollListener(HideFabOnScrollListener(view.addFab))
        view.addFab.setOnClickListener {
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
            TransitionManager.beginDelayedTransition(findViewById(R.id.toolbar), Fade())
            searchItem.expandActionView()
            hideFabForSearch()
            true
        }

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                showFabAfterSearch()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }
    // endregion

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initBooksRecyclerView(reset = true)
    }

    private fun displayBooks() {
        val books = booksRepository.itemsSubject.value
            ?: return

        val searchQuery = searchQuerySubject.value ?: ""
        val searchPredicate: (Book) -> Boolean =
            if (isOnSearch)
                { book ->
                    SearchUtil.isMatchGeneralCondition(
                        searchQuery,
                        listOf(book.title, book.authorName)
                    )
                }
            else
                { _ -> true }

        booksAdapter.setData(
            books
                .filter(searchPredicate)
                .map { BookListItem(it) }
        )
    }

    private var fabWasVisible = false
    private fun showFabAfterSearch() {
        view.addFab.isEnabled = true
        if (fabWasVisible) {
            view.addFab.show()
        }
    }

    private fun hideFabForSearch() {
        fabWasVisible = view.addFab.visibility == View.VISIBLE
        view.addFab.isEnabled = false
        view.addFab.hide()
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
                view.booksList.smoothScrollToPosition(0)
        }
    }
}
