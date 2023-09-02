package ua.com.radiokot.pc.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.databinding.ActivityEditQuoteBinding
import ua.com.radiokot.pc.logic.repository.QuotesByBookRepository
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.util.ToastManager
import ua.com.radiokot.pc.util.error_handlers.ErrorHandlerFactory
import ua.com.radiokot.pc.util.extensions.getStringExtra
import ua.com.radiokot.pc.view.dialog.ConfirmationDialog
import ua.com.radiokot.pc.view.util.TypefaceUtil

class EditQuoteActivity : BaseActivity() {
    companion object {
        const val IS_ADDING_EXTRA = "is_adding"
        const val BOOK_ID_EXTRA = "book_id"
        const val BOOK_TITLE_EXTRA = "book_title"
        const val QUOTE_ID_EXTRA = "quote_id"
        const val QUOTE_TEXT_EXTRA = "quote_text"
        const val QUOTE_IS_PUBLIC_EXTRA = "quote_is_public"

        val EDIT_QUOTE_REQUEST = "edit_quote".hashCode() and 0xffff
    }

    private lateinit var view: ActivityEditQuoteBinding

    private val isAdding: Boolean
        get() = intent.getBooleanExtra(IS_ADDING_EXTRA, true)
    private val bookId: Long
        get() = intent.getLongExtra(BOOK_ID_EXTRA, 0)
    private val bookTitle: String
        get() = intent.getStringExtra(BOOK_TITLE_EXTRA, "")
    private val quoteId: Long
        get() = intent.getLongExtra(QUOTE_ID_EXTRA, 0)
    private val quoteText: String
        get() = intent.getStringExtra(QUOTE_TEXT_EXTRA, "")
    private val quoteIsPublic: Boolean
        get() = intent.getBooleanExtra(QUOTE_IS_PUBLIC_EXTRA, true)

    private val quotesRepository: QuotesByBookRepository
        get() = Repositories.quotes(bookId)

    private val lensIntent: Intent? by lazy {
        packageManager.getLaunchIntentForPackage("com.google.ar.lens")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (quoteText.isNotEmpty()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

        view = ActivityEditQuoteBinding.inflate(layoutInflater)
        setContentView(view.root)

        initToolbar(if (isAdding) R.string.add_quote_title else 0)

        initInput()
    }

    // region Init
    private fun initInput() {
        view.quoteEditText.apply {
            typeface = TypefaceUtil.getRobotoSlabRegular()
            background = null

            setText(quoteText)
            if (quoteText.isNotEmpty()) {
                //setSelection(quoteText.length, quoteText.length)
                view.focusGrabber.requestFocus()
            }
        }

        view.quoteIsPublicCheckbox.apply {
            typeface = TypefaceUtil.getRobotoSlabRegular()

            isChecked = quoteIsPublic
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.quote, menu)
        if (isAdding) {
            menu?.findItem(R.id.delete)?.isVisible = false
            menu?.findItem(R.id.share)?.isVisible = false
        }

        if (!isAdding || lensIntent == null) {
            menu?.findItem(R.id.scan_with_lens)?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }
    // endregion

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> tryToDelete()
            R.id.save -> tryToSave()
            R.id.share -> share()
            R.id.scan_with_lens -> tryToOpenLens()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun tryToDelete() {
        ConfirmationDialog(this)
            .show(getString(R.string.delete_quote_confirmation)) {
                delete()
            }
    }

    // region Progress
    var progressDialog: ProgressDialog? = null

    private fun displayProgress(
        message: String,
        cancelListener: DialogInterface.OnCancelListener? = null
    ) {
        if (progressDialog?.isShowing == true) {
            progressDialog?.setMessage(message)
            progressDialog?.setCancelable(cancelListener != null)
        }
        progressDialog = ProgressDialog.show(
            this, null,
            message, true,
            cancelListener != null
        )
        progressDialog?.setOnCancelListener(cancelListener)
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }
    // endregion

    private var deleteDisposable: Disposable? = null
    private fun delete() {
        deleteDisposable?.dispose()
        deleteDisposable = quotesRepository.delete(quoteId)
            .compose(ObservableTransformers.defaultSchedulersCompletable())
            .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
            .doOnSubscribe {
                displayProgress(getString(R.string.quote_deleting_progress),
                    DialogInterface.OnCancelListener {
                        saveDisposable?.dispose()
                    })
            }
            .subscribeBy(
                onComplete = {
                    hideProgress()
                    ToastManager.short(R.string.quote_deleted)
                    finishWithResult()
                },
                onError = {
                    hideProgress()
                    ErrorHandlerFactory.getDefault().handle(it)
                }
            )
    }

    private fun tryToSave() {
        if (view.quoteEditText.text.isBlank()) {
            ToastManager.short(R.string.input_text_hint)
            return
        }

        save()
    }

    private var saveDisposable: Disposable? = null
    private fun save() {
        saveDisposable?.dispose()

        val text = view.quoteEditText.text.trim().toString()
        val isPublic = view.quoteIsPublicCheckbox.isChecked

        val saveObservable =
            if (isAdding)
                quotesRepository.add(text, isPublic)
            else
                quotesRepository.update(quoteId, text, isPublic)

        saveDisposable = saveObservable
            .compose(ObservableTransformers.defaultSchedulers())
            .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
            .doOnSubscribe {
                displayProgress(getString(R.string.quote_saving_progress),
                    DialogInterface.OnCancelListener {
                        saveDisposable?.dispose()
                    })
            }
            .subscribeBy(
                onNext = {
                    hideProgress()
                    ToastManager.short(R.string.quote_saved)
                    finishWithResult()
                },
                onError = {
                    hideProgress()
                    ErrorHandlerFactory.getDefault().handle(it)
                }
            )
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun share() {
        var text = quoteText
        if (bookTitle.isNotEmpty()) {
            text += "\n" + getString(R.string.quote_book_title, bookTitle)
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun tryToOpenLens() {
        if (lensIntent != null) {
            startActivity(lensIntent)
        }
    }
}
