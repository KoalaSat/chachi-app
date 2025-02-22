package com.koalasat.chachi.models

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.koalasat.chachi.Chachi
import com.koalasat.chachi.R
import com.vitorpamplona.quartz.encoders.toHexKey
import com.vitorpamplona.quartz.events.Event
import com.vitorpamplona.quartz.signers.ExternalSignerLauncher
import com.vitorpamplona.quartz.signers.SignerType
import com.vitorpamplona.quartz.utils.TimeUtils
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

object ExternalSigner {
    private lateinit var nostrSignerLauncher: ActivityResultLauncher<Intent>
    private lateinit var externalSignerLauncher: ExternalSignerLauncher

    fun init(activity: AppCompatActivity) {
        nostrSignerLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != Activity.RESULT_OK) {
                    Log.e("Chachi", "ExternalSigner result error: ${result.resultCode}")
                    Toast.makeText(activity, activity.getString(R.string.amber_not_found), Toast.LENGTH_SHORT).show()
                } else {
                    result.data?.let { externalSignerLauncher.newResult(it) }
                }
            }

        startLauncher()
    }

    fun savePubKey() {
        externalSignerLauncher.openSignerApp(
            "",
            SignerType.GET_PUBLIC_KEY,
            "",
            UUID.randomUUID().toString(),
        ) { result ->
            val split = result.split("-")
            val pubkey = split.first()
            if (split.first().isNotEmpty()) {
                EncryptedStorage.updatePubKey(pubkey)
                if (split.size > 1) {
                    EncryptedStorage.updateExternalSigner(split[1])
                }
                startLauncher()
            }
        }
    }

    fun auth(
        relayUrl: String,
        challenge: String,
        onReady: (Event) -> Unit,
    ) {
        val pubKey = Chachi.getInstance().getHexKey()
        val createdAt = TimeUtils.now()
        val kind = 22242
        val content = ""
        val tags =
            arrayOf(
                arrayOf("relay", relayUrl),
                arrayOf("challenge", challenge),
            )
        val id = Event.generateId(pubKey, createdAt, kind, tags, content).toHexKey()
        val event =
            Event(
                id = id,
                pubKey = pubKey,
                createdAt = createdAt,
                kind = kind,
                tags = tags,
                content = content,
                sig = "",
            )
        externalSignerLauncher.openSigner(
            event,
        ) {
            onReady(
                Event(
                    id = id,
                    pubKey = pubKey,
                    createdAt = createdAt,
                    kind = kind,
                    tags = tags,
                    content = content,
                    sig = it,
                ),
            )
        }
    }

    fun sign(
        event: Event,
        onReady: (String) -> Unit,
    ) {
        externalSignerLauncher.openSigner(
            event,
            onReady,
        )
    }

    private fun startLauncher() {
        var pubKey = EncryptedStorage.pubKey.value
        if (pubKey == null) pubKey = ""
        var externalSignerPackage = EncryptedStorage.externalSigner.value
        if (externalSignerPackage == null) externalSignerPackage = ""
        if (pubKey.isEmpty()) externalSignerPackage = ""
        externalSignerLauncher = ExternalSignerLauncher(pubKey, signerPackageName = externalSignerPackage)
        externalSignerLauncher.registerLauncher(
            launcher = {
                try {
                    nostrSignerLauncher.launch(it)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("Chachi", "Error opening Signer app", e)
                }
            },
            contentResolver = { Chachi.getInstance().contentResolverFn() },
        )
    }
}
