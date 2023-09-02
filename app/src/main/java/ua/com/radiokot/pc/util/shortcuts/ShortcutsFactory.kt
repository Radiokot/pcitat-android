package ua.com.radiokot.pc.util.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.activities.books.BooksActivity
import ua.com.radiokot.pc.activities.quotes.QuotesActivity

@RequiresApi(Build.VERSION_CODES.N_MR1)
class ShortcutsFactory(private val context: Context) {
    companion object {
        const val BOOKS_SHORTCUT_ID = "books"
        const val QUOTES_SHORTCUT_ID = "quotes"
    }

    fun getBooksShortcut(): ShortcutInfo {
        return ShortcutInfo.Builder(context, BOOKS_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.my_books))
            .setLongLabel(context.getString(R.string.my_books))
            .setRank(5)
            .setIcon(Icon.createWithResource(context, R.drawable.shortcut_books))
            .setIntent(getSingleTaskIntent<BooksActivity>(BOOKS_SHORTCUT_ID))
            .build()
    }

    fun getQuotesShortcut(): ShortcutInfo {
        return ShortcutInfo.Builder(context, QUOTES_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.my_quotes))
            .setLongLabel(context.getString(R.string.my_quotes))
            .setRank(0)
            .setIcon(Icon.createWithResource(context, R.drawable.shortcut_quotes))
            .setIntent(getSingleTaskIntent<QuotesActivity>(QUOTES_SHORTCUT_ID))
            .build()
    }

    private inline fun <reified T : Any> getSingleTaskIntent(action: String): Intent =
        Intent(context, T::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .setAction(action)
}
