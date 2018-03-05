package ua.com.radiokot.pc.activities.add_book

import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.model.ExternalSiteBook
import ua.com.radiokot.pc.view.adapter.BaseRecyclerAdapter

class SuggestedBooksAdapter : BaseRecyclerAdapter<ExternalSiteBook, SuggestedBookViewHolder>() {
    override fun createItemViewHolder(parent: ViewGroup?): SuggestedBookViewHolder {
        val view = LayoutInflater.from(parent?.context ?: App.instance)
                .inflate(R.layout.list_item_suggested_book, parent, false)
        return SuggestedBookViewHolder(view)
    }

    override fun getDiffCallback(newItems: List<ExternalSiteBook>): DiffUtil.Callback? {
        return object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].externalUrl == newItems[newItemPosition].externalUrl
            }

            override fun getOldListSize(): Int {
                return items.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(oldItemPosition, newItemPosition)
                        && items[oldItemPosition].authorName == newItems[newItemPosition].authorName
                        && items[oldItemPosition].title == newItems[newItemPosition].title
            }
        }
    }
}