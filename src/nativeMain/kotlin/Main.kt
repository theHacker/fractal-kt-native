import kotlinx.cinterop.ExperimentalForeignApi
import libpng.fprintf
import libpng.stderr

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val arguments = ArgumentsParser.parseArguments(args)
    fprintf(
        stderr,
        "Generating a ${arguments.size.x}x${arguments.size.y} image " +
            "at center coords (${arguments.center.x}, ${arguments.center.y}) " +
            "with zoom ${arguments.zoom}, threshold ${arguments.threshold} and max iterations ${arguments.iterations}..."
    )

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

    if (PngWriter.writePng(imageResult) == 0) {
        fprintf(stderr, " OK.\nWrote result as PNG to stdout.\n")
    } else {
        fprintf(stderr, " FAILED.\nFailure writing PNG.\n")
    }
}
