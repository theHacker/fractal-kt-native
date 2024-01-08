import kotlin.experimental.ExperimentalNativeApi
import kotlin.math.ceil
import kotlin.math.floor

class ColorGradient(private val colorStops: Array<ColorStop>) {

    @OptIn(ExperimentalNativeApi::class)
    fun getColor(iterations: Int, maxIterations: Int): Color {
        val position = iterations.toDouble() / maxIterations.toDouble()
        assert(position in 0.0..1.0)

        val indexStart = floor(position * colorStops.size)
            .toInt()
            .coerceAtMost(colorStops.size - 1)

        val indexEnd = ceil(position * colorStops.size)
            .toInt()
            .coerceAtMost(colorStops.size - 1)

        val colorStopStart = colorStops[indexStart].color
        val colorStopEnd = colorStops[indexEnd].color

        val positionBetweenStops = (position * colorStops.size)
            .let {
                if (it > 1.0) {
                    it - floor(position * colorStops.size)
                } else {
                    it
                }
            }

        return Color(
            (colorStopStart.r.toDouble() + ((colorStopEnd.r - colorStopStart.r).toDouble() * positionBetweenStops)),
            (colorStopStart.g.toDouble() + ((colorStopEnd.g - colorStopStart.g).toDouble() * positionBetweenStops)),
            (colorStopStart.b.toDouble() + ((colorStopEnd.b - colorStopStart.b).toDouble() * positionBetweenStops))
        )
    }
}

data class ColorStop(
    val color: Color
)

data class Color(
    val r: UByte,
    val g: UByte,
    val b: UByte
) {
    constructor(r: Int, g: Int, b: Int) : this(
        r.toUByte(),
        g.toUByte(),
        b.toUByte()
    )

    constructor(r: Double, g: Double, b: Double) : this(
        r.toUInt().toUByte(),
        g.toUInt().toUByte(),
        b.toUInt().toUByte()
    )
}
