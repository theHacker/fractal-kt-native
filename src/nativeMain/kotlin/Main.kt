import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val arguments = ArgumentsParser.parseArguments(args)

    val imageResult = Mandelbrot.generate(arguments)

    if (PngWriter.writePng(imageResult) != 0) {
        fprintf(stderr, "Failure writing PNG.\n")
    }
}
