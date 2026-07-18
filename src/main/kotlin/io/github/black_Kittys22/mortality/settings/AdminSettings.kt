package io.github.black_Kittys22.mortality.settings

class AdminSettings {
    var maxHeartsSetting: Int = 3
    val EMPTY_HEART = "\uE000"

    private val PURPLE = "\uE001"
    private val RED = "\uE002"
    private val GOLD = "\uE003"
    private val GREEN = "\uE004"
    private val BLUE = "\uE005"

    private var _activeHeartSymbol = PURPLE

    var PURPLE_HEART: String
        get() = _activeHeartSymbol
        set(value) {
            _activeHeartSymbol = value
        }

    var activeHeartSymbol: String
        get() = _activeHeartSymbol
        set(value) {
            _activeHeartSymbol = value
        }

    fun setHeartColor(color: HeartColor) {
        activeHeartSymbol = when(color) {
            HeartColor.PURPLE -> PURPLE
            HeartColor.RED -> RED
            HeartColor.GOLD -> GOLD
            HeartColor.GREEN -> GREEN
            HeartColor.BLUE -> BLUE
        }
    }

    enum class HeartColor {
        PURPLE, RED, GOLD, GREEN, BLUE
    }
}
