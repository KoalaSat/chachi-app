package com.koalasat.chachi.models

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlin.apply

object PrefKeys {
    const val NOSTR_PUBKEY = "nostr_pubkey"
    const val EXTERNAL_SIGNER = "external_signer"
}

object DefaultKeys {
    const val EXTERNAL_SIGNER = "com.greenart7c3.nostrsigner"
}

object EncryptedStorage {
    private const val PREFERENCES_NAME = "secret_keeper"

    private lateinit var sharedPreferences: SharedPreferences

    private val _pubKey = MutableLiveData<String>()
    val pubKey: LiveData<String> get() = _pubKey
    private val _externalSigner = MutableLiveData<String>().apply { DefaultKeys.EXTERNAL_SIGNER }
    val externalSigner: LiveData<String> get() = _externalSigner

    fun init(context: Context) {
        val masterKey: MasterKey =
            MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        sharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                PREFERENCES_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            ) as EncryptedSharedPreferences

        _pubKey.value = sharedPreferences.getString(PrefKeys.NOSTR_PUBKEY, "")
        _externalSigner.value =
            sharedPreferences.getString(PrefKeys.EXTERNAL_SIGNER, DefaultKeys.EXTERNAL_SIGNER)
                ?: DefaultKeys.EXTERNAL_SIGNER
    }

    fun updateExternalSigner(newValue: String) {
        sharedPreferences.edit().putString(PrefKeys.EXTERNAL_SIGNER, newValue).apply()
        _externalSigner.value = newValue
    }

    fun updatePubKey(newValue: String) {
        sharedPreferences.edit().putString(PrefKeys.NOSTR_PUBKEY, newValue).apply()
        _pubKey.value = newValue
    }
}
