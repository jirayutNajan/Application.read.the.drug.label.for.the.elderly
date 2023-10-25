package com.example.testanimation

import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import org.json.JSONObject
import java.io.InputStream
import java.util.Locale


class Information : AppCompatActivity() {

    lateinit var mTTS: TextToSpeech
    lateinit var mButtonSpeak: Button

    lateinit var readable: TextView
    lateinit var imageView: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        imageView = findViewById(R.id.imageView2)
        mButtonSpeak = findViewById(R.id.read)

        mTTS = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = mTTS.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TTS", "Language not supported")
                } else {
                    mButtonSpeak.isEnabled = true
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }

        val medname = intent.getStringExtra("drugname")

        val medInfo = getMedicinesInfo(medname.toString())
        /*val infoText = "$medname\n" +
                "\nชื่อไทย: ${medInfo.getString("tradeNameTha")}\n" +
                "\nชื่ออังกฤษ: ${medInfo.getString("tradeNameEng")}\n" +
                "\nชื่อสารสำคัญ: ${medInfo.getString("substanceName")}\n" +
                "\nความแรง: ${medInfo.getString("power")}\n" +
                "\nสรรพคุณ: ${medInfo.getString("properties")}\n" +
                "\nวิธีใช้: ${medInfo.getString("howToUse")}\n" +
                "\nคำเตือน: ${medInfo.getString("warning")}\n" +
                "\nเลขทะเบียน: ${medInfo.getString("registrationNumber")}\n" +
                "\nรูปแบบจัดกลุ่ม: ${medInfo.getString("groupFormat")}\n" +
                "\nรูปแบบตามทะเบียน: ${medInfo.getString("registrationForm")}\n" +
                "\nชื่อผู้รับอนุญาต: ${medInfo.getString("licenseeName")}\n" +
                "\nรหัสยา 24 หลัก: ${medInfo.getString("24DigitDrugCode")}\n" +
                "\nรหัสกลุ่มยา ATC: ${medInfo.getString("drugGroupCodeATC")}\n" +
                "\nรหัสยามาตรฐาน: (TMT)\n ${medInfo.getString("standardDrugCode(TMT)")}\n"*/

        val toRead = "$medname\n" +
                "Properties: ${medInfo.getString("properties")}\n\n" +
                "How to use: ${medInfo.getString("howToUse")}\n\n" +
                "Warning: \n${medInfo.getString("warning")}\n\n"

        val nottoread = "Substance name: ${medInfo.getString("substanceName")}\n\n" +
                "Potion of medicine: ${medInfo.getString("power")}\n\n" +
                "Registration number: ${medInfo.getString("registrationNumber")}\n\n" +
                "Grouped form: ${medInfo.getString("groupFormat")}\n\n" +
                "Registration format: ${medInfo.getString("registrationForm")}\n\n" +
                "Licensee name: ${medInfo.getString("licenseeName")}\n\n" +
                "24 digit drug code: ${medInfo.getString("24DigitDrugCode")}\n\n" +
                "Drug group code ATC: ${medInfo.getString("drugGroupCodeATC")}\n\n" +
                "Standard drug code: (TMT)\n ${medInfo.getString("standardDrugCode(TMT)")}\n\n"

        imageView = findViewById(R.id.imageView2)
        imageView.setImageBitmap(BitmapFactory.decodeStream(assets.open("drugImage/$medname.jpg")))

        /*textView = findViewById(R.id.readable)
        textView.movementMethod = ScrollingMovementMethod()
        textView.text = infoText*/

        readable = findViewById(R.id.readable)
        readable.movementMethod = ScrollingMovementMethod()
        readable.text = toRead + nottoread

        mButtonSpeak.setOnClickListener { speak(toRead) }
    }

    private fun speak(toread: String) {
        mTTS.speak(toread, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onDestroy() {
        if (mTTS != null) {
            mTTS.stop()
            mTTS.shutdown()
        }
        super.onDestroy()
    }

    fun getMedicinesInfo(name: String): JSONObject {

        var json: String? = null

        val inputStream: InputStream = assets.open("medicineinfo.json")
        json = inputStream.bufferedReader().use { it.readText() }

        var jsonObj = JSONObject(json).getJSONObject("medicines").getJSONObject(name)

        return jsonObj
    }
}