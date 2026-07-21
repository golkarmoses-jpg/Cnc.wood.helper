package com.example.cncwood

class AdvancedGCodeGenerator {

    fun generateGCodeWithFormat(
        contours: List<List<Pair<Double, Double>>>,
        width: Double,
        height: Double,
        depth: Double,
        format: GCodeFormat
    ): String {
        val gcode = StringBuilder()
        gcode.append(generateCustomHeader(format))

        if (contours.isEmpty()) {
            gcode.append("; No contours found\n")
            gcode.append(generateFooter(format))
            return gcode.toString()
        }

        var isFirstPath = true

        for ((index, contour) in contours.withIndex()) {
            gcode.append("; Contour ${index + 1}\n")

            if (contour.isEmpty()) continue

            val simplified = simplifyContour(contour, 0.5)
            val startPoint = simplified.first()

            if (isFirstPath) {
                gcode.append("${format.rapidCommand} X%.4f Y%.4f\n".format(startPoint.first, startPoint.second))
                if (format.shouldIncludeZMovement) {
                    gcode.append("${format.rapidCommand} Z10\n")
                }
                isFirstPath = false
            } else {
                gcode.append("${format.rapidCommand} X%.4f Y%.4f\n".format(startPoint.first, startPoint.second))
                if (format.shouldIncludeZMovement) {
                    gcode.append("${format.rapidCommand} Z10\n")
                }
            }

            if (format.shouldIncludeZMovement) {
                gcode.append("${format.linearCommand} Z-%.4f F${format.feedRate}\n".format(depth))
            }

            for (point in simplified.drop(1)) {
                gcode.append("${format.linearCommand} X%.4f Y%.4f F${format.feedRate}\n".format(point.first, point.second))
            }

            gcode.append("${format.linearCommand} X%.4f Y%.4f F${format.feedRate}\n".format(startPoint.first, startPoint.second))

            if (format.shouldIncludeZMovement) {
                gcode.append("${format.rapidCommand} Z10\n")
            }
            gcode.append("\n")
        }

        gcode.append(generateFooter(format))
        return gcode.toString()
    }

    private fun generateCustomHeader(format: GCodeFormat): String {
        val header = StringBuilder()
        header.append("; Generated with CNC Wood Helper\n")
        header.append("; Format: ${format.name}\n")
        header.append("G17\nG90\n")
        if (format.shouldIncludeSpindleControl && format.spindleSpeed > 0) {
            header.append("M03 S${format.spindleSpeed}\n")
        }
        header.append("\n")
        return header.toString()
    }

    private fun generateFooter(format: GCodeFormat): String {
        val footer = StringBuilder()
        if (format.shouldIncludeZMovement) {
            footer.append("${format.rapidCommand} Z10\n")
        }
        footer.append("${format.rapidCommand} X0 Y0\n")
        if (format.shouldIncludeSpindleControl) {
            footer.append("M05\n")
        }
        footer.append("M30\n%\n")
        return footer.toString()
    }

    private fun simplifyContour(
        contour: List<Pair<Double, Double>>,
        epsilon: Double = 0.5
    ): List<Pair<Double, Double>> {
        if (contour.size < 3) return contour

        var dmax = 0.0
        var index = 0

        for (i in 1 until contour.size - 1) {
            val d = perpendicularDistance(
                contour[i],
                contour[0],
                contour[contour.size - 1]
            )
            if (d > dmax) {
                index = i
                dmax = d
            }
        }

        return if (dmax > epsilon) {
            val rec1 = simplifyContour(contour.subList(0, index + 1), epsilon)
            val rec2 = simplifyContour(contour.subList(index, contour.size), epsilon)
            rec1 + rec2.drop(1)
        } else {
            listOf(contour[0], contour[contour.size - 1])
        }
    }

    private fun perpendicularDistance(
        point: Pair<Double, Double>,
        lineStart: Pair<Double, Double>,
        lineEnd: Pair<Double, Double>
    ): Double {
        val px = point.first
        val py = point.second
        val x1 = lineStart.first
        val y1 = lineStart.second
        val x2 = lineEnd.first
        val y2 = lineEnd.second

        val numerator = kotlin.math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1)
        val denominator = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1))

        return if (denominator == 0.0) 0.0 else numerator / denominator
    }
}

class ContourEditor {
    fun addPoint(contour: MutableList<ContourPoint>, point: ContourPoint, index: Int = -1) {
        if (index == -1) {
            contour.add(point)
        } else {
            contour.add(index, point)
        }
    }

    fun removePoint(contour: MutableList<ContourPoint>, index: Int): Boolean {
        return if (index in contour.indices) {
            contour.removeAt(index)
            true
        } else {
            false
        }
    }

    fun movePoint(contour: MutableList<ContourPoint>, index: Int, newX: Double, newY: Double): Boolean {
        return if (index in contour.indices) {
            contour[index] = contour[index].copy(x = newX, y = newY)
            true
        } else {
            false
        }
    }

    fun smoothContour(contour: List<ContourPoint>): List<ContourPoint> {
        if (contour.size < 3) return contour
        val smoothed = mutableListOf<ContourPoint>()
        for (i in contour.indices) {
            val prev = contour[(i - 1 + contour.size) % contour.size]
            val curr = contour[i]
            val next = contour[(i + 1) % contour.size]
            val smoothX = (prev.x + curr.x + next.x) / 3.0
            val smoothY = (prev.y + curr.y + next.y) / 3.0
            smoothed.add(ContourPoint(smoothX, smoothY, curr.isSelected))
        }
        return smoothed
    }

    fun reverseContour(contour: List<ContourPoint>): List<ContourPoint> {
        return contour.reversed()
    }
}
