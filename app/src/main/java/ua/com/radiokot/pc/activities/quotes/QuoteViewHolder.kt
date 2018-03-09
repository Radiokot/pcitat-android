package ua.com.radiokot.pc.activities.quotes

import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.find
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.view.adapter.BaseViewHolder
import ua.com.radiokot.pc.view.util.TypefaceUtil

class QuoteViewHolder(view: View,
                      private val needBookTitles: Boolean) : BaseViewHolder<Quote>(view) {
    private val quoteTextView = view.find<TextView>(R.id.quote_text_view)
    private val quoteBookTextView = view.find<TextView>(R.id.quote_book_text_view)

    private val context = view.context
    private val accentColor = ContextCompat.getColor(view.context, R.color.accent)

    override fun bind(item: Quote) {
        TypefaceUtil.getRobotoSlabRegular().let {
            quoteTextView.typeface = it
            quoteBookTextView.typeface = it
        }

        quoteTextView.text = item.text

        if (needBookTitles && item.bookTitle != null) {
            quoteBookTextView.visibility = View.VISIBLE

            val title = item.bookTitle
            val titleString = context.getString(R.string.quote_book_title, title)
            val titleStart = titleString.indexOf(title)
            val spannableTitleString = SpannableString(titleString)
            spannableTitleString.setSpan(ForegroundColorSpan(accentColor),
                    titleStart,
                    titleStart + title.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            quoteBookTextView.text = spannableTitleString
        } else {
            quoteBookTextView.visibility = View.GONE
        }
    }
}