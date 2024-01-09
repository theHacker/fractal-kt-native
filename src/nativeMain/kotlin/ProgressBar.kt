import kotlinx.cinterop.*
import platform.posix.sprintf
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.Volatile
import kotlin.math.round

@OptIn(ObsoleteNativeApi::class) // see https://youtrack.jetbrains.com/issue/KT-55163 for details
class ProgressBar(
    /** maximum value (inclusive), so we support values inside 0..max, though 0 will never be marked done */
    private val max: Int,
    /** number of characters of the progress bar */
    private val width: Int
) {

    private val bitmapDone = BitSet(max)
    private val bitmapInProgress = BitSet(max)

    private val valueToCharIndex: Map<Int, Int>
    private val charIndexToOpenValues: Map<Int, MutableSet<Int>> = (0..<width)
        .associateWith { mutableSetOf() }

    init {
        val tmpValueToCharIndex = mutableMapOf<Int, Int>()

        for (value in 1..max) { // start with 1, we never get called with markDone(0), that's the starting situation
            val charIndex = (value.toDouble() / max.toDouble() * (width - 1).toDouble()).toInt()

            tmpValueToCharIndex[value] = charIndex
            charIndexToOpenValues[charIndex]!!.add(value)
        }

        valueToCharIndex = tmpValueToCharIndex
    }

    private val doneCount = AtomicInt(0)

    @Volatile
    private var percent = 0.0

    // Assuming markDone() is called exactly once per value. Otherwise, doneCount and percent don't work correctly.
    fun markDone(value: Int) {
        val charIndex = valueToCharIndex[value]!!

        bitmapInProgress.set(charIndex)
        charIndexToOpenValues[charIndex]!!.remove(value)

        if (charIndexToOpenValues[charIndex]!!.isEmpty()) {
            bitmapDone.set(charIndex)
        }

        percent = doneCount.incrementAndGet().toDouble() / max.toDouble()
    }

    @OptIn(ExperimentalForeignApi::class)
    fun generate(): String {
        val sbBar = StringBuilder(width)
        for (i in 0..<width) {
            if (!bitmapInProgress[i]) {
                sbBar.append("░")
            } else if (bitmapDone[i]) {
                sbBar.append("▓")
            } else {
                sbBar.append("▒")
            }
        }

        val percentString = memScoped {
            val percent100 = round(percent * 1000.0) / 10.0

            val buffer = allocArray<ByteVar>(7)
            sprintf(buffer, "%5.1f%%", percent100)

            buffer.toKString()
        }

        return "\r$percentString ... [$sbBar]"
    }
}
