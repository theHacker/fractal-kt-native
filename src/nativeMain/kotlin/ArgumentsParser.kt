import kotlinx.cinterop.*
import platform.posix.getopt
import platform.posix.optarg
import kotlin.system.exitProcess

object ArgumentsParser {

    private val regexSize = Regex("\\(?(-?\\d+)[,xX](-?\\d+)\\)?")
    private val regexCenter = Regex("\\(?(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)\\)?")

    @OptIn(ExperimentalForeignApi::class)
    fun parseArguments(args: Array<String>): Arguments {
        // C-ify the args:
        // cinterop cuts out the first argument like used to from Kotlin.
        // C libraries however does not expect this!
        val argc: Int = args.size + 1
        val argv: CValuesRef<CPointerVar<ByteVar>> = (arrayOf("fractal-generator.kexe") + args)
            .map { it.cstr.getPointer(Arena()) }
            .toCValues()

        // Parse
        var size = Coords(300, 200)
        var center = Coords(-0.5, 0.0)
        var zoom = 20.0
        var threshold = 50.0
        var iterations = 25

        while (true) {
            when (getopt(argc, argv, "s:c:z:t:i:")) {
                -1 -> break

                's'.code -> {
                    size = parseSize(optarg!!.toKString())
                }
                'c'.code -> {
                    center = parseCenter(optarg!!.toKString())
                }
                'z'.code -> {
                    zoom = optarg!!.toKString()
                        .let {
                            it.toDoubleOrNull()
                            ?: run {
                                println("Cannot parse '$it' as zoom.")
                                exitProcess(1)
                            }
                        }
                }
                't'.code -> {
                    threshold = optarg!!.toKString()
                        .let {
                            it.toDoubleOrNull()
                                ?: run {
                                    println("Cannot parse '$it' as threshold.")
                                    exitProcess(1)
                                }
                        }
                }
                'i'.code -> {
                    iterations = optarg!!.toKString()
                        .let {
                            it.toIntOrNull()
                                ?: run {
                                    println("Cannot parse '$it' as iterations.")
                                    exitProcess(1)
                                }
                        }
                }

                '?'.code -> {
                    exitProcess(1)
                }
            }
        }

        // Return struct
        return Arguments(size, center, zoom, threshold, iterations)
    }

    private fun parseSize(string: String): Coords<Int> = regexSize
        .matchEntire(string)
        ?.let { Coords(it.groupValues[1].toInt(), it.groupValues[2].toInt()) }
        ?: run {
            println("Cannot parse '$string' as size.")
            exitProcess(1)
        }

    private fun parseCenter(string: String): Coords<Double> = regexCenter
        .matchEntire(string)
        ?.let { Coords(it.groupValues[1].toDouble(), it.groupValues[2].toDouble()) }
        ?: run {
            println("Cannot parse '$string' as center.")
            exitProcess(1)
        }
}

data class Coords<T>(
    val x: T,
    val y: T
)

data class Arguments(
    val size: Coords<Int>,
    val center: Coords<Double>,
    val zoom: Double,
    val threshold: Double,
    val iterations: Int
)
