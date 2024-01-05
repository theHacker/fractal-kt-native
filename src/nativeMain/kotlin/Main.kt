import kotlinx.cinterop.ExperimentalForeignApi
import libpng.png_access_version_number

fun main() {
    val libPngVersion = getLibPngVersion()
    val imageResult = Mandelbrot.generate()

    println("Hello Mandelbrot!")
    println("powered by libpng v$libPngVersion\n")

    val path = "output.png"

    if (PngWriter.writePng(imageResult, path) == 0) {
        println("Wrote result as PNG into $path.")
    } else {
        println("Failure writing PNG.")
    }
}

@OptIn(ExperimentalForeignApi::class)
fun getLibPngVersion(): String = png_access_version_number().let {
    val major = it.floorDiv(10000u)
    val minor = it.mod(10000u).floorDiv(100u)
    val patch = it.mod(100u)

    "$major.$minor.$patch"
}

