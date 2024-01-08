import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val arguments = ArgumentsParser.parseArguments(args)
    val colorStops = arrayOf(
        ColorStop(Color(0, 0, 128)),
        ColorStop(Color(0, 128, 255)),
        ColorStop(Color(192, 255, 255)),
        ColorStop(Color(255, 255, 0)),
        ColorStop(Color(255, 128, 0)),
        ColorStop(Color(0, 0, 0)),
        ColorStop(Color(0, 0, 128))
    )

    val imageResult = Mandelbrot.generate(arguments, ColorGradient(colorStops))

    if (PngWriter.writePng(imageResult) != 0) {
        fprintf(stderr, "Failure writing PNG.\n")
    }
}
