import kotlinx.cinterop.*
import platform.posix.sprintf
import kotlin.math.round

@OptIn(ObsoleteNativeApi::class) // see https://youtrack.jetbrains.com/issue/KT-55163 for details
class ProgressBar(private val max: Int, private val width: Int) {

    private val bitmapDone = BitSet(max)

    private var doneCount = 0
    private var percent = 0.0

    // Assuming markDone() is called exactly once per value. Otherwise, doneCount and percent don't work correctly.
    fun markDone(value: Int) {
        bitmapDone.set((value.toDouble() / max.toDouble() * width.toDouble()).toInt())

        doneCount++
        percent = doneCount.toDouble() / max.toDouble()
    }

    @OptIn(ExperimentalForeignApi::class)
    fun generate(): String {
        val sbBar = StringBuilder(width)
        for (i in 0..<width) {
            if (bitmapDone[i]) {
                sbBar.append("▓")
            } else {
                sbBar.append("░")
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
