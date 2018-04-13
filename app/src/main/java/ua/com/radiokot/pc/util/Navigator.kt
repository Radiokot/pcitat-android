package ua.com.radiokot.pc.util

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import org.jetbrains.anko.intentFor
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.EditQuoteActivity
import ua.com.radiokot.pc.activities.LoginActivity
import ua.com.radiokot.pc.activities.ProfileInfoActivity
import ua.com.radiokot.pc.activities.TwitterOauthActivity
import ua.com.radiokot.pc.activities.add_book.AddBookActivity
import ua.com.radiokot.pc.activities.books.BooksActivity
import ua.com.radiokot.pc.activities.quotes.QuotesActivity

object Navigator {
    private fun fadeOut(activity: Activity) {
        ActivityCompat.finishAfterTransition(activity)
        activity.overridePendingTransition(0, R.anim.activity_fade_out)
        activity.finish()
    }

    private fun createTransitionBundle(activity: Activity,
                                       vararg pairs: Pair<View?, String>): Bundle {
        val sharedViews = arrayListOf<android.support.v4.util.Pair<View, String>>()

        pairs.forEach {
            val view = it.first
            if (view != null) {
                sharedViews.add(android.support.v4.util.Pair(view, it.second))
            }
        }

        return if (sharedViews.isEmpty()) {
            Bundle.EMPTY
        } else {
            ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    *sharedViews.toTypedArray()).toBundle() ?: Bundle.EMPTY
        }
    }

    fun toMainActivity(activity: Activity) {
        activity.startActivity(activity.intentFor<BooksActivity>())
        activity.finish()
    }

    fun toLoginActivity(activity: Activity) {
        activity.startActivity(activity.intentFor<LoginActivity>())
        activity.finish()
    }

    fun openAddBookActivity(activity: Activity) {
        activity.startActivityForResult(activity.intentFor<AddBookActivity>(),
                AddBookActivity.ADD_BOOK_REQUEST)
    }

    fun toQuotesActivity(activity: Activity) {
        activity.startActivity(activity.intentFor<QuotesActivity>())
        fadeOut(activity)
    }

    fun openQuotesActivity(activity: Activity, bookId: Long?, bookTitle: String?,
                           bookAuthor: String?, isTwitterBook: Boolean?) {
        activity.startActivityForResult(activity.intentFor<QuotesActivity>(
                QuotesActivity.BOOK_ID_EXTRA to bookId,
                QuotesActivity.BOOK_TITLE_EXTRA to bookTitle,
                QuotesActivity.BOOK_AUTHOR_EXTRA to bookAuthor,
                QuotesActivity.BOOK_IS_TWITTER_EXTRA to isTwitterBook
        ), QuotesActivity.UPDATE_BOOK_REQUEST)
    }

    fun openAddQuoteActivity(activity: Activity, bookId: Long?) {
        activity.startActivityForResult(activity.intentFor<EditQuoteActivity>(
                EditQuoteActivity.IS_ADDING_EXTRA to true,
                EditQuoteActivity.BOOK_ID_EXTRA to bookId
        ), EditQuoteActivity.EDIT_QUOTE_REQUEST)
    }

    fun openEditQuoteActivity(activity: Activity, bookId: Long?, bookTitle: String?,
                              quoteId: Long?, quoteText: String?) {
        activity.startActivityForResult(activity.intentFor<EditQuoteActivity>(
                EditQuoteActivity.IS_ADDING_EXTRA to false,
                EditQuoteActivity.BOOK_ID_EXTRA to bookId,
                EditQuoteActivity.BOOK_TITLE_EXTRA to bookTitle,
                EditQuoteActivity.QUOTE_ID_EXTRA to quoteId,
                EditQuoteActivity.QUOTE_TEXT_EXTRA to quoteText
        ), EditQuoteActivity.EDIT_QUOTE_REQUEST)
    }

    fun openTwitterOauthActivity(activity: Activity, mode: TwitterOauthActivity.Mode) {
        activity.startActivityForResult(activity.intentFor<TwitterOauthActivity>(
                TwitterOauthActivity.MODE_EXTRA to mode.toString()
        ), TwitterOauthActivity.OAUTH_REQUEST)
    }

    fun toProfileInfoActivity(activity: Activity) {
        activity.startActivity(activity.intentFor<ProfileInfoActivity>())
        activity.finish()
    }
}