import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.posix.fprintf
import platform.posix.stderr
import kotlin.math.ceil

object Mandelbrot {

    @OptIn(ExperimentalForeignApi::class)
    fun generate(arguments: Arguments, colorGradient: ColorGradient): ImageResult {
        if (arguments.showProgress) {
            fprintf(
                stderr,
                "Generating a ${arguments.size.x}x${arguments.size.y} image " +
                    "at center coords (${arguments.center.x}, ${arguments.center.y}) " +
                    "with zoom ${arguments.zoom}, threshold ${arguments.threshold} and " +
                    "max iterations ${arguments.iterations}...\n"
            )

            if (arguments.parallelizeCoroutines > 1) {
                fprintf(
                    stderr,
                    "Using ${arguments.parallelizeCoroutines} coroutines to parallelize...\n"
                )
            }
        }

        val pixels = UByteArray(arguments.size.x * arguments.size.y * 3)
        val progressBar = ProgressBar(arguments.size.y, 60)

        val jobYSize = ceil(arguments.size.y.toDouble() / arguments.parallelizeCoroutines).toInt()

        runBlocking {
            (0..<arguments.size.y)
                .chunked(jobYSize)
                .forEach {
                    val pixelsY = it.min()..it.max()

                    val context = GenerationContext(pixelsY, arguments, colorGradient, progressBar, pixels)
                    launch(Dispatchers.Default) {
                        generateRows(context)
                    }
                }
        }

        if (arguments.showProgress) {
            fprintf(stderr, "\nAll done.\n")
        }

        return ImageResult(arguments.size.x.toUInt(), arguments.size.y.toUInt(), pixels)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun generateRows(context: GenerationContext) {
        val (pixelsY, arguments, colorGradient, progressBar, pixels) = context

        var pixelOffset = pixelsY.start * arguments.size.x * 3
        for (pixelY in pixelsY.start..pixelsY.endInclusive) {
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

            progressBar.markDone(pixelY + 1)
            if (arguments.showProgress) {
                fprintf(stderr, "%s", progressBar.generate())
            }
        }
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

private data class GenerationContext(
    val pixelsY: ClosedRange<Int>,
    val arguments: Arguments,
    val colorGradient: ColorGradient,
    val progressBar: ProgressBar, // shared writing into this!
    val pixels: UByteArray // shared writing into this!
)
