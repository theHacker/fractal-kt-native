import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr
import kotlin.math.round

object Mandelbrot {

    @OptIn(ExperimentalForeignApi::class)
    fun generate(arguments: Arguments, colorGradient: ColorGradient): ImageResult {
        if (arguments.showProgress) {
            fprintf(
                stderr,
                "Generating a ${arguments.size.x}x${arguments.size.y} image " +
                    "at center coords (${arguments.center.x}, ${arguments.center.y}) " +
                    "with zoom ${arguments.zoom}, threshold ${arguments.threshold} and max iterations ${arguments.iterations}...\n"
            )
        }

        val pixels = UByteArray(arguments.size.x * arguments.size.y * 3)

        var pixelOffset = 0
        for (pixelY in 0..<arguments.size.y) {
            for (pixelX in 0..<arguments.size.x) {
                val re = (pixelX - arguments.size.x/2).toDouble() / arguments.zoom + arguments.center.x
                val im = (pixelY - arguments.size.y/2).toDouble() / arguments.zoom - arguments.center.y
                val complex = Complex(re, im)

                val iterations = calculatePoint(complex, arguments.threshold, arguments.iterations)

                val color = if (iterations == -1) {
                    Color.BLACK
                } else {
                    colorGradient.getColor(iterations, arguments.iterations)
                }

                pixels[pixelOffset++] = color.r
                pixels[pixelOffset++] = color.g
                pixels[pixelOffset++] = color.b
            }

            if (arguments.showProgress) {
                val percent = (pixelY + 1).toDouble() / arguments.size.y
                val percent100 = round(percent * 1000.0) / 10.0
                val bar = generateAsciiBar(percent, 60)

                fprintf(stderr, "\r%5.1f%% ... [%s]", percent100, bar)
            }
        }
        if (arguments.showProgress) {
            fprintf(stderr, "\nAll done.\n")
        }

        return ImageResult(arguments.size.x.toUInt(), arguments.size.y.toUInt(), pixels)
    }

    private fun generateAsciiBar(percent: Double, width: Int): String {
        val filledChars = (percent * width).toInt()
        val unfilledChars = width - filledChars

        return "▓".repeat(filledChars) + "░".repeat(unfilledChars)
    }

    private fun calculatePoint(c: Complex, threshold: Double, iterations: Int): Int {
        var z = Complex(0.0, 0.0) // z0

        for (iteration in 0..<iterations) {
            z = z * z + c // z(n+1)

            if (z.abs() > threshold) return iteration
        }

        return -1
    }
}

class ImageResult(
    val width: UInt,
    val height: UInt,

    /**
     * pixel data RGB, saved left-to-right, top-to-bottom
     */
    val pixels: UByteArray
)
