package ua.com.radiokot.pc.activities.add_book

import android.view.View
import android.widget.TextView
import org.jetbrains.anko.find
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.view.adapter.BaseViewHolder

class SuggestedBookViewHolder(view: View) : BaseViewHolder<ExternalSiteBook>(view) {
    private val titleTextView = view.find<TextView>(R.id.book_title_text_view)
    private val authorTextView = view.find<TextView>(R.id.book_author_text_view)

    override fun bind(item: ExternalSiteBook) {
        titleTextView.text = item.title
        authorTextView.text = item.authorName
    }
}