package ua.com.radiokot.pc.logic.event_bus.events

/**
 * Emitted when a book was deleted. Holds deleted book id.
 */
class BookDeletedEvent(val bookId: Long) : PcEvent