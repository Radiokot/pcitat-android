package ua.com.radiokot.pc.activities.books

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.view.adapter.BaseViewHolder
import ua.com.radiokot.pc.view.util.TypefaceUtil

class BookViewHolder(view: View, coverHeight: Int) : BaseViewHolder<BookListItem>(view) {
    private val titleTextView = view.findViewById<TextView>(R.id.book_title_text_view)
    private val quotesCountTextView = view.findViewById<TextView>(R.id.quotes_count_text_view)
    private val coverImageView = view.findViewById<ImageView>(R.id.book_cover_image_view)
    private val twitterExportIndicator = view.findViewById<View>(R.id.twitter_export_icon)

    private val coverPlaceholder =
        ColorDrawable(ContextCompat.getColor(view.context, R.color.colorPrimaryDark))

    init {
        coverImageView.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                coverHeight
            )
    }

    override fun bind(item: BookListItem) {
        titleTextView.text = item.title
        quotesCountTextView.text = (item.quotesCount ?: 0).toString()
        quotesCountTextView.typeface = TypefaceUtil.getRobotoSlabRegular()

        twitterExportIndicator.visibility =
            if (item.isTwitterBook == true) View.VISIBLE else View.GONE

        Picasso.get()
            .load(item.coverUrl)
            .placeholder(coverPlaceholder)
            .fit()
            .centerCrop()
            .into(coverImageView)
    }
}
