package ua.com.radiokot.pc.logic.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import ua.com.radiokot.pc.logic.model.Quote

@Entity(tableName = "quote",
        indices = [(Index("id")), (Index("book_id"))])
data class QuoteEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Long? = null,
        @ColumnInfo(name = "text")
        var text: String? = null,
        @ColumnInfo(name = "book_id")
        var bookId: Long? = null,
        @ColumnInfo(name = "book_title")
        var bookTitle: String? = null
) {
    companion object {
        fun fromQuote(quote: Quote): QuoteEntity {
            return quote.let {
                QuoteEntity(it.id, it.text, it.bookId, it.bookTitle)
            }
        }
    }

    fun toQuote(): Quote {
        return Quote(id, bookId, bookTitle, text)
    }
}