package ua.com.radiokot.pc.activities

import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.default_toolbar.*

abstract class BaseActivity : RxAppCompatActivity() {
    protected fun initToolbar(title: String? = null, needBackButton: Boolean = true) {
        setSupportActionBar(getToolbar())
        setTitle(title)
        if (needBackButton) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected fun initToolbar(titleResId: Int? = null, needUpButton: Boolean = true) =
            initToolbar(if (titleResId != null) getString(titleResId) else null, needUpButton)

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun getToolbar(): Toolbar? {
        return toolbar
    }
}