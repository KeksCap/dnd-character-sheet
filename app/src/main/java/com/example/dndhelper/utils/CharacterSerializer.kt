package com.example.dndhelper.utils

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.dndhelper.data.CharacterSaveData
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object CharacterSerializer {
    private val gson = Gson()

    fun encodeCharacter(character: CharacterSaveData): String {
        return try {
            val json = gson.toJson(character)
            // GZIP compression
            val bos = ByteArrayOutputStream()
            val gzip = GZIPOutputStream(bos)
            gzip.write(json.toByteArray(Charsets.UTF_8))
            gzip.close()
            val compressedBytes = bos.toByteArray()
            // Base64 encoding (URL_SAFE to avoid issues in text chats)
            "DNDCHAR:" + Base64.encodeToString(compressedBytes, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun decodeCharacter(data: String): CharacterSaveData? {
        return try {
            val cleanData = if (data.startsWith("DNDCHAR:")) {
                data.substring("DNDCHAR:".length)
            } else {
                data
            }
            
            // Base64 decode
            val compressedBytes = Base64.decode(cleanData, Base64.URL_SAFE)
            // GZIP decompression
            val bais = ByteArrayInputStream(compressedBytes)
            val gzipIn = GZIPInputStream(bais)
            val reader = InputStreamReader(gzipIn, Charsets.UTF_8)
            
            gson.fromJson(reader, CharacterSaveData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateQrBitmap(content: String, sizePixels: Int): ImageBitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, sizePixels, sizePixels)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
