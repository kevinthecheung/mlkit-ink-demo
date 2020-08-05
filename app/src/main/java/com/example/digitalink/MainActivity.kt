package com.example.digitalink

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.digitalink.databinding.ActivityMainBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*

class MainActivity : AppCompatActivity() {

    private val drawAreaBg = Color.DKGRAY
    private val drawAreaFg = Color.WHITE
    private val inkDrawable = InkDrawable(Paint().apply {
        color = drawAreaFg
        isAntiAlias = true
        strokeWidth = 10f
    })
    private var inkBuilder = Ink.builder()
    private lateinit var strokeBuilder: Ink.Stroke.Builder

    private lateinit var inkRecognizer: DigitalInkRecognizer

    @SuppressLint("ClickableViewAccessibility") // Should provide alternative input methods.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        val modelIdentifier = DigitalInkRecognitionModelIdentifier.EN_US
        val inkRecognitionModel = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        inkRecognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(inkRecognitionModel).build())

        binding.goButton.isEnabled = false
        RemoteModelManager.getInstance()
            .download(inkRecognitionModel, DownloadConditions.Builder().build())
            .addOnSuccessListener {
                binding.goButton.isEnabled = true
            }

        binding.drawingView.setImageDrawable(inkDrawable)
        binding.drawingView.setBackgroundColor(drawAreaBg)
        binding.drawingView.setOnTouchListener { _, event ->
            Log.d("INK", event.toString())
            val t = System.currentTimeMillis()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    inkDrawable.addPoint(event.x, event.y)
                    strokeBuilder = Ink.Stroke.builder()
                    strokeBuilder.addPoint(Ink.Point.create(event.x, event.y, t))
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    inkDrawable.addPoint(event.x, event.y)
                    strokeBuilder.addPoint(Ink.Point.create(event.x, event.y, t))
                    true
                }
                MotionEvent.ACTION_UP -> {
                    inkDrawable.newStroke()
                    inkBuilder.addStroke(strokeBuilder.build())
                    true
                }
                else -> super.onTouchEvent(event)
            }
        }

        binding.goButton.setOnClickListener {
            val ink = inkBuilder.build()
            inkRecognizer.recognize(ink).addOnSuccessListener { result ->
                if (result.candidates.isNotEmpty()) {
                    binding.drawingLabel.text = result.candidates.first().text
                }
            }
        }

        binding.resetButton.setOnClickListener {
            binding.drawingLabel.text = ""
            inkDrawable.reset()
            strokeBuilder = Ink.Stroke.builder()
            inkBuilder = Ink.builder()
        }

        setContentView(binding.root)
    }

}