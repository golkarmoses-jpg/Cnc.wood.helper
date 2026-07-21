package com.example.cncwood

class GCodeGenerator {

    companion object {
        private const val FEED_RATE = 100
        private const val RAPID_MOVE_HEIGHT = 5.0
        private const val SAFE_HEIGHT = 10.0
    }

    fun generateGCode(
        edges: Array<IntArray>,
        width: Double,
        height: Double,
        depth: Double
    ): String {
        val gcode = StringBuilder()
        gcode.append(generateHeader())
        gcode.append(generateFooter())
        return gcode.toString()
    }

    private fun generateHeader(): String {
        return """
            ; CNC Wood Helper - Generated G-Code
            G17
            G20
            G90
            M5
            M9
            G00 Z$SAFE_HEIGHT
            
            """.trimIndent() + "\n"
    }

    private fun generateFooter(): String {
        return """
            G00 Z$SAFE_HEIGHT
            G00 X0 Y0
            M5
            M9
            M30
            %
            
            """.trimIndent()
    }
}
