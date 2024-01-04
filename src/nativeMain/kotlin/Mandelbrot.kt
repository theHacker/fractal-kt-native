object Mandelbrot {

    fun generate(): ImageResult {
        val imageWidth = 30
        val imageHeight = 20
        val centerX = 0.0
        val centerY = 1.0
        val zoom = 2.0

        val pixels = UByteArray(imageWidth * imageHeight)

        var pixelOffset = 0
        for (pixelY in 0..<imageHeight) {
            for (pixelX in 0..<imageWidth) {
                val re = (pixelX - imageWidth/2) / zoom + centerX
                val im = (pixelY - imageHeight/2) / zoom + centerY
                val complex = Complex(re, im)

                val iterations = calculatePoint(complex)

                pixels[pixelOffset++] = iterations.toUByte()
            }
        }

        return ImageResult(imageWidth, imageHeight, pixels)
    }

    private fun calculatePoint(c: Complex): Int {
        val threshold = 50.0
        val maxIterations = 25
        var z = Complex(0.0, 0.0) // z0

        for (iteration in 0..<maxIterations) {
            z = z * z + c // z(n+1)

            if (z.abs() > threshold) return iteration
        }

        return 0
    }
}

class ImageResult(
    val width: Int,
    val height: Int,

    /**
     * for now: just iterations (0..255), saved left-to-right, top-to-bottom
     */
    val pixels: UByteArray
)
