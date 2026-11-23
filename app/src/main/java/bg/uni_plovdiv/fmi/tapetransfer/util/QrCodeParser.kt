package bg.uni_plovdiv.fmi.tapetransfer.util

object QrCodeParser {

    private const val APP_TAG = "TapeTransfer"

    fun parseOrderId(text: String?): Int? {
        if (text == null) return null

        val lines = text.lines().map { it.trim() }
        if (lines.firstOrNull() != APP_TAG) return null

        val numberLine = lines.find { it.startsWith("№") } ?: return null
        return numberLine.removePrefix("№").toIntOrNull()
    }
}
