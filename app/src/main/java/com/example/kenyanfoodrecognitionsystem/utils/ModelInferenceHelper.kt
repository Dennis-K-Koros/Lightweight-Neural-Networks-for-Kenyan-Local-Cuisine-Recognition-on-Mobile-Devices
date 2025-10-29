package com.example.kenyanfoodrecognitionsystem.utils

import android.content.Context
import android.graphics.Bitmap
import com.example.kenyanfoodrecognitionsystem.ml.BestKenyanFoodModelV3
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

// Define constants based on your model's input details
const val IMAGE_SIZE_X = 224
const val IMAGE_SIZE_Y = 224
const val NUM_CLASSES = 13

fun classifyFoodImageManual(context: Context, bitmap: Bitmap): String {
    // --- PART A: Preprocessing and Input Preparation ---

    // 1. Convert bitmap to ARGB_8888 if it's not already
    val argbBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        bitmap
    }

    // 2. Create a TensorImage object
    var tensorImage = TensorImage(DataType.FLOAT32)
    tensorImage.load(argbBitmap)

    // 3. Create image processor
    val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(IMAGE_SIZE_X, IMAGE_SIZE_Y, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f))
        .build()

    // 4. Process the image
    tensorImage = imageProcessor.process(tensorImage)

    // 5. Create the input TensorBuffer
    val inputFeature0 = TensorBuffer.createFixedSize(
        intArrayOf(1, IMAGE_SIZE_X, IMAGE_SIZE_Y, 3), // [1, 224, 224, 3]
        DataType.FLOAT32
    )

    inputFeature0.loadBuffer(tensorImage.buffer)

    // 6. Load model and run inference
    val model = BestKenyanFoodModelV3.newInstance(context)
    val outputs = model.process(inputFeature0)
    val outputBuffer = outputs.outputFeature0AsTensorBuffer
    val probabilities = outputBuffer.floatArray

    // 7. Define labels
    val labels = listOf(
        "bhaji", "chapati", "githeri", "kachumbari", "kukuchoma", "Mandazi",
        "masalachips", "Matoke", "Mukimo", "Nyamachoma", "pilau", "Sukuma Wiki", "Ugali"
    )

    // 8. Find the maximum probability
    var maxIndex = 0
    var maxProbability = 0.0f

    for (i in probabilities.indices) {
        if (probabilities[i] > maxProbability) {
            maxProbability = probabilities[i]
            maxIndex = i
        }
    }

    // 9. Close the model
    model.close()

    // 10. Return the result with formatted label
    val foodName = labels[maxIndex].replaceFirstChar { it.uppercase() }
    return "$foodName (${String.format("%.2f", maxProbability * 100)}%)"
}