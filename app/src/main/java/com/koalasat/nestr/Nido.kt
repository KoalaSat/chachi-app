package com.koalasat.nido

import android.app.Application
import android.content.ContentResolver
import com.koalasat.nido.models.EncryptedStorage
import com.vitorpamplona.quartz.encoders.Nip19Bech32
import com.vitorpamplona.quartz.encoders.Nip19Bech32.uriToRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class Nido : Application() {
    private val applicationIOScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationIOScope.cancel()
    }

    fun contentResolverFn(): ContentResolver = contentResolver

    fun getHexKey(): String {
        val pubKey = EncryptedStorage.pubKey.value
        var hexKey = ""
        val parseReturn = uriToRoute(pubKey)
        when (val parsed = parseReturn?.entity) {
            is Nip19Bech32.NPub -> {
                hexKey = parsed.hex
            }
        }
        return hexKey
    }

    companion object {
        @Volatile
        private var instance: Nido? = null

        fun getInstance(): Nido =
            instance ?: synchronized(this) {
                instance ?: Nido().also { instance = it }
            }
    }
}
