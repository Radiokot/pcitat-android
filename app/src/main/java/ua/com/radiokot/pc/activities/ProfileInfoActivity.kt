package ua.com.radiokot.pc.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.AppBarLayout
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.databinding.ActivityProfileInfoBinding
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.logic.repository.UserRepository
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.view.util.LoadingIndicatorManager
import ua.com.radiokot.pc.view.util.TypefaceUtil

class ProfileInfoActivity : NavigationActivity() {
    private lateinit var view: ActivityProfileInfoBinding

    private val loadingIndicator = LoadingIndicatorManager(
        showLoading = { view.includeProgress.progress.show() },
        hideLoading = { view.includeProgress.progress.hide() }
    )

    private val userRepository: UserRepository
        get() = Repositories.user()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityProfileInfoBinding.inflate(layoutInflater)
        setContentView(view.root)

        initToolbar()
        initNavigation()

        initLabels()
        initButtons()
        subscribeToUser()
    }

    override fun getNavigationItemId(): Long = PROFILE_NAVIGATION_ITEM

    private fun initToolbar() {
        initToolbar(R.string.profile_settings, false)
        (getToolbar()?.layoutParams as? AppBarLayout.LayoutParams)?.scrollFlags = 0
    }

    private fun initLabels() {
        listOf(view.userNameTextView, view.emailTextView, view.twitterNicknameTextView)
            .forEach {
                it.typeface = TypefaceUtil.getRobotoSlabRegular()
            }
    }

    private fun initButtons() {
        listOf(view.twitterAuthButton, view.twitterFirstAuthButton)
            .forEach {
                it.setOnClickListener {
                    Navigator.openTwitterOauthActivity(
                        this,
                        TwitterOauthActivity.Mode.UPDATE_EXISTING
                    )
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
            ?: return

        view.userNameTextView.text = user.name
        view.emailTextView.text = user.email

        user.twitterIntegration?.username.let { twitterNickname ->
            if (twitterNickname != null) {
                view.twitterInfoLayout.visibility = View.VISIBLE
                view.twitterAuthButton.visibility = View.VISIBLE
                view.twitterFirstAuthButton.visibility = View.GONE

                view.twitterNicknameTextView.text = twitterNickname
            } else {
                view.twitterInfoLayout.visibility = View.GONE
                view.twitterAuthButton.visibility = View.GONE
                view.twitterFirstAuthButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK &&
            requestCode == TwitterOauthActivity.OAUTH_REQUEST
        ) {
            userRepository.update()
        }
    }
}
