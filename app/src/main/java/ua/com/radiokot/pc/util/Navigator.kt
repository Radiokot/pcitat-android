package ua.com.radiokot.pc.util

import android.app.Activity
import android.support.v4.app.ActivityCompat
import org.jetbrains.anko.intentFor
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.BooksActivity
import ua.com.radiokot.pc.activities.LoginActivity

object Navigator {
    private fun fadeOut(activity: Activity) {
        ActivityCompat.finishAfterTransition(activity)
        activity.overridePendingTransition(0, R.anim.activity_fade_out)
        activity.finish()
    }

    fun toMainActivity(activity: Activity) {
        activity.startActivity(activity.intentFor<BooksActivity>())
        activity.finish()
    }

    fun toLoginActivity(activity: Activity) {
        activity.startActivity(activity.intentFor<LoginActivity>())
        activity.finish()
    }
}