object Mandelbrot {

    fun generate(arguments: Arguments, colorGradient: ColorGradient): ImageResult {
        val pixels = UByteArray(arguments.size.x * arguments.size.y * 3)

        var pixelOffset = 0
        for (pixelY in 0..<arguments.size.y) {
            for (pixelX in 0..<arguments.size.x) {
                val re = (pixelX - arguments.size.x/2).toDouble() / arguments.zoom + arguments.center.x
                val im = (pixelY - arguments.size.y/2).toDouble() / arguments.zoom - arguments.center.y
                val complex = Complex(re, im)

                val iterations = calculatePoint(complex, arguments.threshold, arguments.iterations)
                val color = colorGradient.getColor(iterations, arguments.iterations)

                pixels[pixelOffset++] = color.r
                pixels[pixelOffset++] = color.g
                pixels[pixelOffset++] = color.b
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
     * pixel data RGB, saved left-to-right, top-to-bottom
     */
    val pixels: UByteArray
)
