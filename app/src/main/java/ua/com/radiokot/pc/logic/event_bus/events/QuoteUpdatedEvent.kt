package ua.com.radiokot.pc.logic.event_bus.events

import ua.com.radiokot.pc.logic.model.Quote

class QuoteUpdatedEvent(val quote: Quote) : PcEvent