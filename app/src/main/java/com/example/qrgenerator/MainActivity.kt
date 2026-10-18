package com.example.qrgenerator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.qrgenerator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentQr: Bitmap? = null

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                saveQr()
            } else {
                Toast.makeText(this, getString(R.string.error_permission_denied), Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerate.setOnClickListener {
            generateQr()
        }

        binding.btnSave.setOnClickListener {
            onSaveClicked()
        }

        binding.btnShare.setOnClickListener {
            onShareClicked()
        }
    }

    private fun generateQr() {
        val text = binding.etInput.text.toString().trim()

        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_input), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bitmap = QrGenerator.generate(text)
            currentQr = bitmap
            binding.ivQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_generate_failed), Toast.LENGTH_LONG).show()
        }
    }

    private fun onSaveClicked() {
        if (currentQr == null) {
            Toast.makeText(this, getString(R.string.error_no_qr), Toast.LENGTH_SHORT).show()
            return
        }

        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveQr()
        }
    }

    private fun saveQr() {
        val bitmap = currentQr ?: return

        val success = QrStorage.saveToGallery(this, bitmap)
        val messageRes = if (success) R.string.msg_saved else R.string.error_save_failed
        Toast.makeText(this, getString(messageRes), Toast.LENGTH_LONG).show()
    }

    private fun onShareClicked() {
        val bitmap = currentQr
        if (bitmap == null) {
            Toast.makeText(this, getString(R.string.error_no_qr), Toast.LENGTH_SHORT).show()
            return
        }

        val success = QrSharer.share(this, bitmap)
        if (!success) {
            Toast.makeText(this, getString(R.string.error_share_failed), Toast.LENGTH_LONG).show()
        }
    }
}