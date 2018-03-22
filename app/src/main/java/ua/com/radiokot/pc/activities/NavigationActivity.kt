package ua.com.radiokot.pc.activities

import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
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
        val BOOKS_NAVIGATION_ITEM = 1L
        val QUOTES_NAVIGATION_ITEM = 2L
        val PROFILE_NAVIGATION_ITEM = 3L
        val LOGOUT_NAVIGATION_ITEM = 4L
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
                .withProfiles(mutableListOf(
                        ProfileDrawerItem()
                                .withIdentifier(PROFILE_NAVIGATION_ITEM)
                                .withIcon(R.drawable.default_profile_image)
                ) as List<IProfile<Any>>)
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
                .withSliderBackgroundColorRes(R.color.material_drawer_background)
                .withSelectedItem(getNavigationItemId())
                .addDrawerItems(
                        booksItem,
                        quotesItem,
                        DividerDrawerItem(),
                        profileSettingsItem,
                        logoutItem
                )
                .withOnDrawerItemClickListener { _, _, item ->
                    return@withOnDrawerItemClickListener onNavigationItemSelected(item)
                }
                .build()

        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView?, uri: Uri?,
                             placeholder: Drawable?, tag: String?) {
                Picasso.with(this@NavigationActivity)
                        .load(uri)
                        .placeholder(R.drawable.default_profile_image)
                        .transform(ReplaceDefaultAvatarTransformation())
                        .fit()
                        .into(imageView)
            }

            override fun cancel(imageView: ImageView?) {
                Picasso.with(this@NavigationActivity)
                        .cancelRequest(imageView);
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
                                        if (it.avatarUrl != null) {
                                            withIcon(it.avatarUrl)
                                        } else {
                                            withIcon(R.drawable.default_profile_image)
                                        }
                                    }
                    )
                }
    }

    protected fun onNavigationItemSelected(item: IDrawerItem<Any, RecyclerView.ViewHolder>):
            Boolean {
        when (item.identifier) {
            getNavigationItemId() -> return false
            BOOKS_NAVIGATION_ITEM -> Navigator.toMainActivity(this)
            QUOTES_NAVIGATION_ITEM -> Navigator.toQuotesActivity(this)
            LOGOUT_NAVIGATION_ITEM ->
                ConfirmationDialog(this).show(getString(R.string.logout_confirmation)) {
                    AuthManager.logOut()
                }
            else -> return false
        }

        return false
    }

    abstract fun getNavigationItemId(): Long
}