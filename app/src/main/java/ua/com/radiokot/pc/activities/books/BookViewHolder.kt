package ua.com.radiokot.pc.activities.books

import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.jetbrains.anko.find
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.view.adapter.BaseViewHolder
import ua.com.radiokot.pc.view.util.TypefaceUtil

class BookViewHolder(view: View, coverHeight: Int) : BaseViewHolder<Book>(view) {
    private val titleTextView = view.find<TextView>(R.id.book_title_text_view)
    private val quotesCountTextView = view.find<TextView>(R.id.quotes_count_text_view)
    private val coverImageView = view.find<ImageView>(R.id.book_cover_image_view)
    private val twitterExportIndicator = view.find<View>(R.id.twitter_export_icon)

    private val coverPlaceholder =
            ColorDrawable(ContextCompat.getColor(view.context, R.color.primary_dark))

    init {
        coverImageView.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        coverHeight)
    }

    override fun bind(item: Book) {
        titleTextView.text = item.title
        quotesCountTextView.text = (item.quotesCount ?: 0).toString()
        quotesCountTextView.typeface = TypefaceUtil.getRobotoSlabRegular()

        twitterExportIndicator.visibility =
                if (item.isTwitterBook == true) View.VISIBLE else View.GONE

        Picasso.with(coverImageView.context)
                .load(item.coverUrl)
                .placeholder(coverPlaceholder)
                .fit()
                .centerCrop()
                .into(coverImageView)
    }
}