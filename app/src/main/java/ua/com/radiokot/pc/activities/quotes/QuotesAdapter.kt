package ua.com.radiokot.pc.activities.quotes

import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.model.Quote
import ua.com.radiokot.pc.view.adapter.BaseRecyclerAdapter

class QuotesAdapter : BaseRecyclerAdapter<Quote, QuoteViewHolder>() {
    var needBookTitles = false

    override fun createItemViewHolder(parent: ViewGroup?): QuoteViewHolder {
        val view = LayoutInflater.from(parent?.context ?: App.instance)
                .inflate(R.layout.list_item_quote, parent, false)
        return QuoteViewHolder(view, needBookTitles)
    }

    override fun getDiffCallback(newItems: List<Quote>): DiffUtil.Callback? {
        return object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == newItems[newItemPosition].id
            }

            override fun getOldListSize(): Int {
                return items.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].text == newItems[newItemPosition].text
            }
        }
    }
}