package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.model.User
import ua.com.radiokot.pc.logic.repository.base.SimpleSingleItemRepository

/**
 * Holds application user info.
 */
class UserRepository : SimpleSingleItemRepository<User>() {
    override fun getItem(): Observable<User> {
        return ApiFactory.getUserService().getInfo()
                .map { it.data }
    }

    fun set(newUser: User) {
        this.item = newUser
        this.isFresh = true
        this.isNewerUpdated = false
        broadcast()
    }
}