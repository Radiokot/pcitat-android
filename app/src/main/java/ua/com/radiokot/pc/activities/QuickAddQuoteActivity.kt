package ua.com.radiokot.pc.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Scroller
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_quick_add_quote.*
import kotlinx.android.synthetic.main.layout_progress.*
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.view.util.LoadingIndicatorManager
import ua.com.radiokot.pc.view.util.TypefaceUtil

class QuickAddQuoteActivity : BaseActivity() {
    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )

    private val booksRepository: BooksRepository
        get() = Repositories.books()

    private var intentText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_add_quote)

        initToolbar(R.string.add_quote_title)

        initBookSelection()
        initFields()

        subscribeToBooks()

        booksRepository.updateIfNotFresh()
    }

    private fun initBookSelection() {

    }

    private fun initFields() {
        val intentText = intent
                .takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }
                ?.getStringExtra(Intent.EXTRA_TEXT)
                ?.trim()

        quote_edit_text.apply {
            typeface = TypefaceUtil.getRobotoSlabRegular()
            setText(intentText)
            if (!intentText.isNullOrEmpty()) {
                focus_grabber.requestFocus()
            }
        }
    }

    private var booksDisposable: CompositeDisposable? = null

    private fun subscribeToBooks() {
        booksDisposable?.dispose()
        booksDisposable = CompositeDisposable(
                booksRepository.loadingSubject
                        .compose(ObservableTransformers.defaultSchedulers())
                        .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                        .subscribe { loadingIndicator.setLoading(it, "books") },
                booksRepository.errorsSubject
                        .compose(ObservableTransformers.defaultSchedulers())
                        .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                        .subscribe { ErrorHandlerFactory.getByNetworkState().handle(it) }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.quote_add, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
