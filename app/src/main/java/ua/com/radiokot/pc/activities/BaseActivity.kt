package ua.com.radiokot.pc.activities

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.event_bus.PcEvents
import ua.com.radiokot.pc.logic.event_bus.events.PcEvent
import ua.com.radiokot.pc.view.util.TypefaceUtil

abstract class BaseActivity : RxAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, R.anim.activity_fade_out)
        super.onCreate(savedInstanceState)

        subscribeToEvents()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.activity_fade_out)
    }

    // region Event bus
    protected open fun subscribeToEvents() {
        PcEvents.subscribeUntilDestroy(this, this::onPcEvent)
    }

    protected open fun onPcEvent(event: PcEvent) {}
    // endregion

    // region Toolbar
    protected fun initToolbar(title: String? = null, needBackButton: Boolean = true) {
        setSupportActionBar(getToolbar())
        setTitle(title)
        if (needBackButton) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getToolbarTitleTextView()?.transitionName = getString(R.string.transition_title)
        }

        getToolbarTitleTextView()?.typeface = TypefaceUtil.getCondensedBold()
    }

    protected fun initToolbar(titleResId: Int = 0, needUpButton: Boolean = true) =
        initToolbar(if (titleResId != 0) getString(titleResId) else null, needUpButton)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    protected open fun getToolbar(): Toolbar? {
        return findViewById(R.id.toolbar)
    }

    private fun getToolbarTitleTextView(): TextView? {
        val toolbar = getToolbar()
        var titleTextView: TextView? = null
        for (i in 0 until (toolbar?.childCount ?: 0)) {
            val view = toolbar?.getChildAt(i)
            if (view is TextView) {
                titleTextView = view
                break
            }
        }
        return titleTextView
    }
    // endregion
}
