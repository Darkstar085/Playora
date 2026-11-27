package com.sipun.playora.feature.player.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract

class OpenDocumentAtInitialUri : ActivityResultContract<OpenDocumentAtInitialUri.Input, Uri?>() {

    class Input(val mimeTypes: Array<String>, val initialUri: Uri? = null)

    override fun createIntent(context: Context, input: Input): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("*/*")
            .putExtra(
                Intent.EXTRA_MIME_TYPES,
                input.mimeTypes,
            )
            .apply {
                if (input.initialUri != null) {
                    putExtra(
                        DocumentsContract.EXTRA_INITIAL_URI,
                        input.initialUri,
                    )
                }
            }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent.takeIf {
        resultCode == Activity.RESULT_OK
    }?.data
}
