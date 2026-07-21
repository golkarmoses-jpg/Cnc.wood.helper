package com.example.cncwood

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

class ImageProcessor {

    fun detectEdges(bitmap: Bitmap): Array<IntArray> {
        val grayscale = toGrayscale(bitmap)
        return applySobel(grayscale)
    }

    private fun toGrayscale(bitmap: Bitmap): Array<IntArray> {
        val width = bitmap.width
        val height = bitmap.height
        val grayscale = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                grayscale[y][x] = gray
            }
        }

        return grayscale
    }

    private fun applySobel(grayscale: Array<IntArray>): Array<IntArray> {
        val height = grayscale.size
        val width = grayscale[0].size
        val edges = Array(height) { IntArray(width) }

        val sobelX = arrayOf(
            intArrayOf(-1, 0, 1),
            intArrayOf(-2, 0, 2),
            intArrayOf(-1, 0, 1)
        )

        val sobelY = arrayOf(
            intArrayOf(-1, -2, -1),
            intArrayOf(0, 0, 0),
            intArrayOf(1, 2, 1)
        )

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var gx = 0
                var gy = 0

                for (i in -1..1) {
                    for (j in -1..1) {
                        val pixel = grayscale[y + i][x + j]
                        gx += pixel * sobelX[i + 1][j + 1]
                        gy += pixel * sobelY[i + 1][j + 1]
                    }
                }

                val magnitude = sqrt((gx * gx + gy * gy).toDouble()).toInt()
                edges[y][x] = if (magnitude > 100) 255 else 0
            }
        }

        return edges
    }
}
