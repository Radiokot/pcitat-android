package ua.com.radiokot.pc.activities

import android.os.Build
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.default_toolbar.*
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.view.util.TypefaceUtil

abstract class BaseActivity : RxAppCompatActivity() {
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

    protected fun initToolbar(titleResId: Int? = null, needUpButton: Boolean = true) =
            initToolbar(if (titleResId != null) getString(titleResId) else null, needUpButton)

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    protected open fun getToolbar(): Toolbar? {
        return toolbar
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
}