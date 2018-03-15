package ua.com.radiokot.pc.logic.event_bus.events

/**
 * Emitted when book for Twitter export was changed. Holds new book id.
 */
class TwitterBookChangedEvent(bookId: Long) : PcEvent