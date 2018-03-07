package ua.com.radiokot.pc.logic.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import ua.com.radiokot.pc.logic.model.Book

@Entity(tableName = "book",
        indices = [Index("id"), Index("display_order")])
data class BookEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Long? = null,
        @ColumnInfo(name = "title")
        var title: String? = null,
        @ColumnInfo(name = "author")
        var authorName: String? = null,
        @ColumnInfo(name = "cover")
        var coverUrl: String? = null,
        @ColumnInfo(name = "quotes_count")
        var quotesCount: Int? = null,
        @ColumnInfo(name = "is_twitter_book")
        var isTwitterBook: Boolean? = null,
        @ColumnInfo(name = "display_order")
        var displayOrder: Int = 0
) {
    companion object {
        fun fromBook(book: Book, displayOrder: Int = 0): BookEntity {
            return book.let {
                BookEntity(it.id, it.title, it.authorName, it.coverUrl,
                        it.quotesCount, it.isTwitterBook, displayOrder)
            }
        }
    }

    fun toBook(): Book {
        return Book(id, title, authorName, coverUrl, quotesCount, isTwitterBook)
    }
}