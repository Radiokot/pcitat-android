package ua.com.radiokot.pc.logic.event_bus.events

class QuoteDeletedEvent(val quoteId: Long, val bookId: Long): PcEvent