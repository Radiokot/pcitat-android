package ua.com.radiokot.pc.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import kotlinx.android.synthetic.main.activity_profile_info.*
import kotlinx.android.synthetic.main.layout_progress.*
import org.jetbrains.anko.onClick
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.logic.repository.UserRepository
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.view.util.LoadingIndicatorManager
import ua.com.radiokot.pc.view.util.TypefaceUtil

class ProfileInfoActivity : NavigationActivity() {
    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )

    private val userRepository: UserRepository
        get() = Repositories.user()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        initToolbar(R.string.profile_settings, false)
        initNavigation()

        initLabels()
        initButtons()
        subscribeToUser()
    }

    override fun getNavigationItemId(): Long = PROFILE_NAVIGATION_ITEM

    private fun initLabels() {
        listOf(user_name_text_view, email_text_view, twitter_nickname_text_view)
                .forEach {
                    it.typeface = TypefaceUtil.getRobotoSlabRegular()
                }
    }

    private fun initButtons() {
        listOf(twitter_auth_button, twitter_first_auth_button)
                .forEach {
                    it.onClick {
                        Navigator.openTwitterOauthActivity(this,
                                TwitterOauthActivity.Mode.UPDATE_EXISTING)
                    }
                }
    }

    private fun subscribeToUser() {
        userRepository.itemSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe {
                    displayUserInfo()
                }

        userRepository.loadingSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe {
                    loadingIndicator.setLoading(it, "user")
                }
    }

    private fun displayUserInfo() {
        val user = userRepository.itemSubject.value

        user_name_text_view.text = user.name
        email_text_view.text = user.email

        user?.twitterIntegration?.username.let { twitterNickname ->
            if (twitterNickname != null) {
                twitter_info_layout.visibility = View.VISIBLE
                twitter_auth_button.visibility = View.VISIBLE
                twitter_first_auth_button.visibility = View.GONE

                twitter_nickname_text_view.text = twitterNickname
            } else {
                twitter_info_layout.visibility = View.GONE
                twitter_auth_button.visibility = View.GONE
                twitter_first_auth_button.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK &&
                requestCode == TwitterOauthActivity.OAUTH_REQUEST) {
            userRepository.update()
        }
    }
}
