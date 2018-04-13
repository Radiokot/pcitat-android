package ua.com.radiokot.pc.util.extensions

import java.nio.charset.Charset
import java.security.MessageDigest

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(this.toByteArray(Charset.forName("UTF-8")))
    return hashBytes.toHexString()
}