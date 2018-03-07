package ua.com.radiokot.pc.logic.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import ua.com.radiokot.pc.logic.api.ApiFactory
import ua.com.radiokot.pc.logic.db.DbFactory
import ua.com.radiokot.pc.logic.db.entities.UserEntity
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

    override fun getStoredItem(): Observable<User> {
        return DbFactory.getAppDatabase().userDao
                .getFirst()
                .subscribeOn(Schedulers.io())
                .map { it.toUser() }
                .toObservable()
    }

    fun set(newUser: User) {
        onNewItem(newUser)
        storeItem(newUser)
    }

    override fun storeItem(item: User) {
        super.storeItem(item)
        doAsync {
            DbFactory.getAppDatabase().userDao.insert(UserEntity.fromUser(item))
        }
    }

}