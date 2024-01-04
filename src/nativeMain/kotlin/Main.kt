fun main() {
    val imageResult = Mandelbrot.generate()

    println("Hello Mandelbrot!\n")
    printAsciiImage(imageResult)
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
