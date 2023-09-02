package ua.com.radiokot.pc.activities

import android.annotation.TargetApi
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.disposables.Disposable
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.AuthManager
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers
import ua.com.radiokot.pc.view.dialog.ConfirmationDialog
import ua.com.radiokot.pc.view.util.ReplaceDefaultAvatarTransformation


/**
 * Root activity with navigation drawer.
 */
abstract class NavigationActivity : BaseActivity() {
    companion object {
        const val BOOKS_NAVIGATION_ITEM = 1L
        const val QUOTES_NAVIGATION_ITEM = 2L
        const val PROFILE_NAVIGATION_ITEM = 3L
        const val LOGOUT_NAVIGATION_ITEM = 4L
    }

    protected var navigationDrawer: Drawer? = null

    protected fun initNavigation() {
        val toolbar = getToolbar()

        val profileHeader = AccountHeaderBuilder()
            .withActivity(this)
            .withSelectionListEnabledForSingleProfile(false)
            .withHeaderBackground(R.drawable.navigation_bg)
            .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
            .withProfileImagesVisible(true)
            .withProfiles(
                mutableListOf(
                    ProfileDrawerItem()
                        .withIdentifier(PROFILE_NAVIGATION_ITEM)
                        .withIcon(R.drawable.default_profile_image)
                )
            )
            .build()
        subscribeToUser(profileHeader)

        val booksItem = PrimaryDrawerItem()
            .withName(R.string.my_books)
            .withIdentifier(BOOKS_NAVIGATION_ITEM)
            .withIcon(R.drawable.ic_book)

        val quotesItem = PrimaryDrawerItem()
            .withName(R.string.my_quotes)
            .withIdentifier(QUOTES_NAVIGATION_ITEM)
            .withIcon(R.drawable.ic_quote)

        val profileSettingsItem = PrimaryDrawerItem()
            .withName(R.string.profile_settings)
            .withIdentifier(PROFILE_NAVIGATION_ITEM)
            .withIcon(R.drawable.ic_profile_settings)

        val logoutItem = PrimaryDrawerItem()
            .withName(R.string.logout_action)
            .withIdentifier(LOGOUT_NAVIGATION_ITEM)
            .withSelectable(false)
            .withIcon(R.drawable.ic_logout)

        val navigationDrawerBuilder = DrawerBuilder()
        if (toolbar != null) {
            navigationDrawerBuilder.withToolbar(toolbar)
        }

        navigationDrawer = navigationDrawerBuilder
            .withActivity(this)
            .withAccountHeader(profileHeader)
            .withHeaderDivider(false)
            .withDelayDrawerClickEvent(280)
            .withSliderBackgroundColorRes(com.mikepenz.materialdrawer.R.color.material_drawer_background)
            .withSelectedItem(getNavigationItemId())
            .addDrawerItems(
                booksItem,
                quotesItem,
                DividerDrawerItem(),
                profileSettingsItem,
                logoutItem
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean = onNavigationItemSelected(drawerItem)
            })
            .withOnDrawerListener(object : Drawer.OnDrawerListener {
                // Disable light status bar when drawer is opened.
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setLightStatusBarEnabled(slideOffset < 0.3)
                    }
                }

                var enabledNow: Boolean = true

                @TargetApi(Build.VERSION_CODES.M)
                private fun setLightStatusBarEnabled(enabled: Boolean) {
                    if (enabled == enabledNow) {
                        return
                    }

                    val flags = window.decorView.systemUiVisibility
                    window.decorView.systemUiVisibility =
                        if (enabled)
                            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        else
                            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

                    enabledNow = enabled
                }

                override fun onDrawerClosed(drawerView: View) {}
                override fun onDrawerOpened(drawerView: View) {}

            })
            .build()

        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(
                imageView: ImageView,
                uri: Uri,
                placeholder: Drawable,
                tag: String?
            ) {
                Picasso.get()
                    .load(uri)
                    .placeholder(
                        ContextCompat.getDrawable(
                            this@NavigationActivity,
                            R.drawable.default_profile_image
                        )!!
                    )
                    .transform(ReplaceDefaultAvatarTransformation())
                    .fit()
                    .into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Picasso.get()
                    .cancelRequest(imageView)
            }
        })

        overridePendingTransition(0, R.anim.activity_fade_out)
    }

    private var userDisposable: Disposable? = null
    private fun subscribeToUser(profileHeader: AccountHeader) {
        userDisposable?.dispose()
        userDisposable = Repositories.user().itemSubject
            .compose(ObservableTransformers.defaultSchedulers())
            .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
            .doOnError { it.printStackTrace() }
            .subscribe {
                profileHeader.updateProfile(
                    ProfileDrawerItem()
                        .withIdentifier(PROFILE_NAVIGATION_ITEM)
                        .withName(it.name)
                        .withEmail(it.email)
                        .apply {
                            it.avatarUrl.also { avatarUrl ->
                                if (avatarUrl != null) {
                                    withIcon(avatarUrl)
                                } else {
                                    withIcon(R.drawable.default_profile_image)
                                }
                            }
                        }
                )
            }
    }

    protected fun onNavigationItemSelected(item: IDrawerItem<*>): Boolean {
        when (item.identifier) {
            getNavigationItemId() -> return false
            BOOKS_NAVIGATION_ITEM -> Navigator.toMainActivity(this)
            QUOTES_NAVIGATION_ITEM -> Navigator.toQuotesActivity(this)
            PROFILE_NAVIGATION_ITEM -> Navigator.toProfileInfoActivity(this)
            LOGOUT_NAVIGATION_ITEM ->
                ConfirmationDialog(this).show(getString(R.string.logout_confirmation)) {
                    AuthManager.logOut()
                }

            else -> return false
        }

        return false
    }

    abstract fun getNavigationItemId(): Long

    override fun onBackPressed() {
        if (navigationDrawer?.isDrawerOpen == true) {
            navigationDrawer?.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }
}
