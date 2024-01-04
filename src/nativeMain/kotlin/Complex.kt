import kotlin.math.sqrt

data class Complex(val re: Double, val im: Double) {

    operator fun plus(c: Complex) = Complex(re + c.re, im + c.im)
    operator fun minus(c: Complex) = Complex(re - c.re, im - c.im)
    operator fun times(c: Complex) = Complex(re * c.re - im * c.im, re * c.im + im * c.re)

    fun abs(): Double = sqrt(re * re + im * im)
}
