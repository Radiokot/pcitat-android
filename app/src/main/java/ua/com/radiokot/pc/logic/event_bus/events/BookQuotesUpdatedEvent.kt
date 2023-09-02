package ua.com.radiokot.pc.logic.event_bus.events

import ua.com.radiokot.pc.logic.model.Quote

class BookQuotesUpdatedEvent(val bookId: Long, val quotes: List<Quote>) : PcEvent
