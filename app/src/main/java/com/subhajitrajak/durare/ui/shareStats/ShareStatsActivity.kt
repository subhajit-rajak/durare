package com.subhajitrajak.durare.ui.shareStats

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.databinding.ActivityShareStatsBinding
import com.subhajitrajak.durare.ui.howToUse.HowToUseFragment
import com.subhajitrajak.durare.utils.showToast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap

class ShareStatsActivity : AppCompatActivity() {

    private val binding: ActivityShareStatsBinding by lazy {
        ActivityShareStatsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // get data from intent
        val pushUps = intent.getStringExtra("pushUps")?: "0"
        val time = intent.getStringExtra("time") ?: "0m 0s"
        val rest = intent.getStringExtra("rest") ?: "0m 0s"

        // setup viewpager
        val adapter = StatsPagerAdapter(this, pushUps, time, rest)
        binding.viewPager.adapter = adapter

        // setup dots indicator
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.howToUse.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out_right
                )
                .replace(android.R.id.content, HowToUseFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.save.setOnClickListener {
            saveCurrentStats()
        }

        binding.export.setOnClickListener {
            shareCurrentStats()
        }
    }

    companion object {
        const val EXTRA_PUSH_UPS = "pushUps"
        const val EXTRA_TIME = "time"
        const val EXTRA_REST = "rest"
        const val REQUEST_WRITE_STORAGE = 1001
    }

    private fun saveCurrentStats() {
        val currentFragmentView = findCurrentStatsFragmentView() ?: run {
            showToast(this, getString(R.string.something_went_wrong))
            return
        }

        if (currentFragmentView.width == 0 || currentFragmentView.height == 0) {
            currentFragmentView.post { saveCurrentStats() }
            return
        }

        val bitmap = captureViewAsTransparentBitmap(currentFragmentView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = saveBitmapToMediaStore(bitmap)
            showSaveResult(uri != null)
        } else {
            // API 24-28: need WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
                // Save will be attempted in onRequestPermissionsResult
                pendingBitmapForLegacySave = bitmap
            } else {
                val ok = saveBitmapToLegacyPictures(bitmap)
                showSaveResult(ok)
            }
        }
    }

    private fun showSaveResult(success: Boolean) {
        showToast(this, if (success) getString(R.string.saved_successfully) else getString(R.string.something_went_wrong))
    }

    private fun shareCurrentStats() {
        val currentFragmentView = findCurrentStatsFragmentView() ?: run {
            showToast(this, getString(R.string.something_went_wrong))
            return
        }

        if (currentFragmentView.width == 0 || currentFragmentView.height == 0) {
            currentFragmentView.post { shareCurrentStats() }
            return
        }

        val bitmap = captureViewAsTransparentBitmap(currentFragmentView)
        shareBitmap(bitmap)
    }

    private fun shareBitmap(bitmap: Bitmap) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveBitmapToMediaStore(bitmap)
            } else {
                // For API 24-28, save to cache directory for sharing
                saveBitmapToCache(bitmap)
            }

            if (uri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Check out my push-up stats! 💪")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Share your push-up stats")
                startActivity(chooserIntent)
            } else {
                showToast(this, getString(R.string.something_went_wrong))
            }
        } catch (_: Exception) {
            showToast(this, getString(R.string.something_went_wrong))
        }
    }

    private fun captureViewAsTransparentBitmap(view: android.view.View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        view.draw(canvas)
        return bitmap
    }

    private fun findCurrentStatsFragmentView(): android.view.View? {
        val fragments = supportFragmentManager.fragments
        val currentItem = binding.viewPager.currentItem
        // Prefer the fragment matching current position and visible
        val candidate = fragments.firstOrNull { fragment ->
            fragment.isVisible &&
                ((currentItem == 0 && fragment is VerticalStatsFragment) || (currentItem == 1 && fragment is HorizontalStatsFragment))
        }
        return candidate?.view
            ?: fragments.firstOrNull { it.isVisible && (it is VerticalStatsFragment || it is HorizontalStatsFragment) }?.view
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBitmapToMediaStore(bitmap: Bitmap): Uri? {
        val filename = generateFilename()
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name))
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, contentValues) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                writeBitmapPng(bitmap, outputStream)
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            return uri
        } catch (_: Throwable) {
            resolver.delete(uri, null, null)
            return null
        }
    }

    private fun saveBitmapToLegacyPictures(bitmap: Bitmap): Boolean {
        return try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val targetDir = File(picturesDir, getString(R.string.app_name))
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val outFile = File(targetDir, generateFilename())
            FileOutputStream(outFile).use { fos ->
                writeBitmapPng(bitmap, fos)
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cacheDir = File(cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val imageFile = File(cacheDir, generateFilename())
            FileOutputStream(imageFile).use { fos ->
                writeBitmapPng(bitmap, fos)
            }
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
        } catch (_: Throwable) {
            null
        }
    }

    private fun writeBitmapPng(bitmap: Bitmap, outputStream: OutputStream) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
    }

    private fun generateFilename(): String {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "PushUp_${ts}.png"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            val bitmap = pendingBitmapForLegacySave
            pendingBitmapForLegacySave = null
            if (granted && bitmap != null) {
                val ok = saveBitmapToLegacyPictures(bitmap)
                showSaveResult(ok)
            } else if (!granted) {
                showToast(this, getString(R.string.permission_denied))
            }
        }
    }

    private var pendingBitmapForLegacySave: Bitmap? = null
}