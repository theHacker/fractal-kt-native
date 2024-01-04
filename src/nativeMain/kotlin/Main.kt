import kotlinx.cinterop.ExperimentalForeignApi
import libpng.png_access_version_number

fun main() {
    val libPngVersion = getLibPngVersion()
    val imageResult = Mandelbrot.generate()

    println("Hello Mandelbrot!")
    println("powered by libpng v$libPngVersion\n")
    printAsciiImage(imageResult)
}

@OptIn(ExperimentalForeignApi::class)
fun getLibPngVersion(): String = png_access_version_number().let {
    val major = it.floorDiv(10000u)
    val minor = it.mod(10000u).floorDiv(100u)
    val patch = it.mod(100u)

    "$major.$minor.$patch"
}

fun printAsciiImage(imageResult: ImageResult) {
    var offset = 0
    for (y in 0..<imageResult.height) {
        for (x in 0..<imageResult.width) {
            val char = Char(65 + imageResult.pixels[offset++].toInt())

            print(char)
        }
        println()
    }
    println()
}
