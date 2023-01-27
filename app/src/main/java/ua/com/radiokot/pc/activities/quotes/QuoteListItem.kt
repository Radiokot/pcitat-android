package ua.com.radiokot.pc.activities.quotes

import ua.com.radiokot.pc.logic.model.Quote

class QuoteListItem(quote: Quote) {
    val id = quote.id
    val text = quote.text
    val bookTitle = quote.bookTitle
    val source: Quote = quote
}