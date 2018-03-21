package ua.com.radiokot.pc.activities.books

import ua.com.radiokot.pc.logic.model.Book

class BookListItem(book: Book) {
    val id = book.id
    val title = book.title
    val quotesCount = book.quotesCount
    val coverUrl = book.coverUrl
    val authorName = book.authorName
    val isTwitterBook = book.isTwitterBook
}