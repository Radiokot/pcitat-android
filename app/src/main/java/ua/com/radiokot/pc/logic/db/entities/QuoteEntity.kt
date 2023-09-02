package ua.com.radiokot.pc.logic.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ua.com.radiokot.pc.logic.model.Quote

@Entity(
    tableName = "quote",
    indices = [(Index("id")), (Index("book_id"))]
)
data class QuoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long?,
    @ColumnInfo(name = "text")
    var text: String?,
    @ColumnInfo(name = "is_public")
    var isPublic: Boolean,
    @ColumnInfo(name = "book_id")
    var bookId: Long?,
    @ColumnInfo(name = "book_title")
    var bookTitle: String?
) {
    companion object {
        fun fromQuote(quote: Quote): QuoteEntity {
            return quote.let {
                QuoteEntity(it.id, it.text, it.isPublic, it.bookId, it.bookTitle)
            }
        }
    }

    fun toQuote(): Quote {
        return Quote(id, bookId, bookTitle, text, isPublic)
    }
}
