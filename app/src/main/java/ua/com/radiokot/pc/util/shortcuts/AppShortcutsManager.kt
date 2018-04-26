package ua.com.radiokot.pc.util.shortcuts

import android.annotation.SuppressLint
import android.os.Build
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.logic.AuthManager

object AppShortcutsManager {
    @SuppressLint("NewApi")
    fun initShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (AuthManager.isAuthorized()) {
                val factory = ShortcutsFactory(App.instance)
                ShortcutsHelper.addShortcuts(listOf(
                        factory.getBooksShortcut(),
                        factory.getQuotesShortcut()
                ))
            } else {
                ShortcutsHelper.removeAll()
            }
        }
    }
}