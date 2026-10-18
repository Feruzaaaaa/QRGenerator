package com.example.qrgenerator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object QrSharer {

    private const val SHARE_DIR = "shared_qr"
    private const val FILE_NAME = "qr_code.png"

    fun share(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val dir = File(context.cacheDir, SHARE_DIR)
            if (!dir.exists() && !dir.mkdirs()) {
                throw IllegalStateException("Cannot create cache directory")
            }

            val file = File(dir, FILE_NAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title))
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}