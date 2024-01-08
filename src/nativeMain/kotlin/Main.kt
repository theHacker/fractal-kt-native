fun main(args: Array<String>) {
    val arguments = ArgumentsParser.parseArguments(args)
    print(
        "Generating a ${arguments.size.x}x${arguments.size.y} image " +
            "at center coords (${arguments.center.x}, ${arguments.center.y}) " +
            "with zoom ${arguments.zoom}, threshold ${arguments.threshold} and max iterations ${arguments.iterations}..."
    )

    val imageResult = Mandelbrot.generate(arguments)

    val path = "output.png"

    if (PngWriter.writePng(imageResult, path) == 0) {
        println(" OK.\nWrote result as PNG into $path.")
    } else {
        println(" FAILED.\nFailure writing PNG.")
    }
}
