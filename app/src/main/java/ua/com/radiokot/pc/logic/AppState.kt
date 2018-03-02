package ua.com.radiokot.pc.logic

import ua.com.radiokot.pc.logic.repository.BooksRepository
import ua.com.radiokot.pc.logic.repository.UserRepository

/**
 * Holds state of the application.
 */
class AppState(
        var userRepository: UserRepository? = null,
        var booksRepository: BooksRepository? = null
)