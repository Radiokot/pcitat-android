package ua.com.radiokot.pc.logic.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ua.com.radiokot.pc.logic.model.Book

@Entity(tableName = "book",
        indices = [Index("id"), Index("paging_token")])
data class BookEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Long? = null,
        @ColumnInfo(name = "paging_token")
        var pagingToken: Int? = null,
        @ColumnInfo(name = "title")
        var title: String? = null,
        @ColumnInfo(name = "author")
        var authorName: String? = null,
        @ColumnInfo(name = "cover")
        var coverUrl: String? = null,
        @ColumnInfo(name = "quotes_count")
        var quotesCount: Int? = null,
        @ColumnInfo(name = "is_twitter_book")
        var isTwitterBook: Boolean? = null
) {
    companion object {
        fun fromBook(book: Book): BookEntity {
            return book.let {
                BookEntity(it.id, it.pagingToken, it.title, it.authorName, it.coverUrl,
                        it.quotesCount, it.isTwitterBook)
            }
        }
    }

    fun toBook(): Book {
        return Book(id, pagingToken, title, authorName, coverUrl, quotesCount, isTwitterBook)
    }
}