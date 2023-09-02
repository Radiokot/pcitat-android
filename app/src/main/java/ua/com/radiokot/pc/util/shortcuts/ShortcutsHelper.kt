package ua.com.radiokot.pc.util.shortcuts

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import ua.com.radiokot.pc.App

object ShortcutsHelper {
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun getShortcutManager(): ShortcutManager? {
        return App.instance.getSystemService(ShortcutManager::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun addShortcuts(shortcutInfos: Collection<ShortcutInfo>) {
        getShortcutManager()?.let { shortcutManager ->
            shortcutInfos.forEach { shortcutInfo ->
                shortcutManager.addDynamicShortcuts(listOf(shortcutInfo))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun disableShortcuts(shortcutIds: Collection<String>) {
        getShortcutManager()?.disableShortcuts(shortcutIds.toList())
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun removeAll() {
        getShortcutManager()?.removeAllDynamicShortcuts()
    }
}
