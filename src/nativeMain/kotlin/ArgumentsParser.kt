import kotlinx.cinterop.*
import platform.posix.fprintf
import platform.posix.getopt
import platform.posix.optarg
import platform.posix.stderr
import kotlin.system.exitProcess

@OptIn(ExperimentalForeignApi::class)
object ArgumentsParser {

    private val regexSize = Regex("\\(?(-?\\d+)[,xX](-?\\d+)\\)?")
    private val regexCenter = Regex("\\(?(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)\\)?")
    private val regexColorStop = Regex("\\(?(\\d+)[, ](\\d+)[, ](\\d+)\\)?")

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
        var showProgress = false
        var parallelizeCoroutines = 1
        var colorStops = listOf(
            ColorStop(Color(0, 0, 128)),
            ColorStop(Color(0, 128, 255)),
            ColorStop(Color(192, 255, 255)),
            ColorStop(Color(255, 255, 0)),
            ColorStop(Color(255, 128, 0)),
            ColorStop(Color(0, 0, 0)),
            ColorStop(Color(0, 0, 128))
        )

        while (true) {
            when (getopt(argc, argv, "s:c:z:t:i:px:g:")) {
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
                                fprintf(stderr, "Cannot parse '$it' as zoom.\n")
                                exitProcess(1)
                            }
                        }
                }
                't'.code -> {
                    threshold = optarg!!.toKString()
                        .let {
                            it.toDoubleOrNull()
                                ?: run {
                                    fprintf(stderr, "Cannot parse '$it' as threshold.\n")
                                    exitProcess(1)
                                }
                        }
                }
                'i'.code -> {
                    iterations = optarg!!.toKString()
                        .let {
                            it.toIntOrNull()
                                ?: run {
                                    fprintf(stderr, "Cannot parse '$it' as iterations.\n")
                                    exitProcess(1)
                                }
                        }
                }
                'p'.code -> {
                    showProgress = true
                }
                'x'.code -> {
                    parallelizeCoroutines = optarg!!.toKString()
                        .let {
                            it.toIntOrNull()
                                ?: run {
                                    fprintf(stderr, "Cannot parse '$it' as threads.\n")
                                    exitProcess(1)
                                }
                        }
                }
                'g'.code -> {
                    colorStops = parseColorStops(optarg!!.toKString())
                }

                '?'.code -> {
                    exitProcess(1)
                }
            }
        }

        // Return struct
        return Arguments(size, center, zoom, threshold, iterations, showProgress, parallelizeCoroutines, ColorGradient(colorStops))
    }

    private fun parseSize(string: String): Coords<Int> = regexSize
        .matchEntire(string)
        ?.let { Coords(it.groupValues[1].toInt(), it.groupValues[2].toInt()) }
        ?: run {
            fprintf(stderr, "Cannot parse '$string' as size.\n")
            exitProcess(1)
        }

    private fun parseCenter(string: String): Coords<Double> = regexCenter
        .matchEntire(string)
        ?.let { Coords(it.groupValues[1].toDouble(), it.groupValues[2].toDouble()) }
        ?: run {
            fprintf(stderr, "Cannot parse '$string' as center.\n")
            exitProcess(1)
        }

    private fun parseColorStops(string: String): List<ColorStop> = string
        .split(';')
        .map { colorStopString ->
            regexColorStop.matchEntire(colorStopString)
                ?.let {
                    val r = parseColorComponent(it.groupValues[1])
                    val g = parseColorComponent(it.groupValues[2])
                    val b = parseColorComponent(it.groupValues[3])

                    ColorStop(Color(r, g, b))
                }
                ?: run {
                    fprintf(stderr, "Cannot parse '$colorStopString' as color stop.\n")
                    exitProcess(1)
                }
        }

    private fun parseColorComponent(string: String): Int = string
        .toIntOrNull()
        ?.takeIf { it in 0..255 }
        ?: run {
            fprintf(stderr, "Cannot parse '$string' as color component (must be integer between 0 and 255).\n")
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
    val iterations: Int,
    val showProgress: Boolean,
    val parallelizeCoroutines: Int,
    val colorGradient: ColorGradient
)
