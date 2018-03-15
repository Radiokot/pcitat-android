package ua.com.radiokot.pc.logic.event_bus.events

import ua.com.radiokot.pc.logic.model.Book

/**
 * Emitted when a book was added. Holds new book.
 */
class BookAddedEvent(val book: Book) : PcEvent