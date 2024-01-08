object Mandelbrot {

    fun generate(arguments: Arguments): ImageResult {
        val pixels = UByteArray(arguments.size.x * arguments.size.y)

        var pixelOffset = 0
        for (pixelY in 0..<arguments.size.y) {
            for (pixelX in 0..<arguments.size.x) {
                val re = (pixelX - arguments.size.x/2).toDouble() / arguments.zoom + arguments.center.x
                val im = (pixelY - arguments.size.y/2).toDouble() / arguments.zoom + arguments.center.y
                val complex = Complex(re, im)

                val iterations = calculatePoint(complex, arguments.threshold, arguments.iterations)

                pixels[pixelOffset++] = iterations.toUByte()
            }
        }

        return ImageResult(arguments.size.x.toUInt(), arguments.size.y.toUInt(), pixels)
    }

    private fun calculatePoint(c: Complex, threshold: Double, iterations: Int): Int {
        var z = Complex(0.0, 0.0) // z0

        for (iteration in 0..<iterations) {
            z = z * z + c // z(n+1)

            if (z.abs() > threshold) return iteration
        }

        return 0
    }
}

class ImageResult(
    val width: UInt,
    val height: UInt,

    /**
     * for now: just iterations (0..255), saved left-to-right, top-to-bottom
     */
    val pixels: UByteArray
)
