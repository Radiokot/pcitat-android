package ua.com.radiokot.pc.activities.books

import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.model.Book
import ua.com.radiokot.pc.view.adapter.BaseRecyclerAdapter

class BooksAdapter : BaseRecyclerAdapter<Book, BookViewHolder>() {
    var coverHeight: Int = 0

    override fun createItemViewHolder(parent: ViewGroup?): BookViewHolder {
        val view = LayoutInflater.from(parent?.context ?: App.instance)
                .inflate(R.layout.list_item_book, parent, false)
        return BookViewHolder(view, coverHeight)
    }

    override fun getDiffCallback(newItems: List<Book>): DiffUtil.Callback? {
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
                return areItemsTheSame(oldItemPosition, newItemPosition)
                        && items[oldItemPosition].quotesCount == newItems[newItemPosition].quotesCount
            }
        }
    }
}