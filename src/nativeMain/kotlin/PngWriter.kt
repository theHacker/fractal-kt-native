import kotlinx.cinterop.*
import libpng.*

object PngWriter {

    @OptIn(ExperimentalForeignApi::class)
    fun writePng(imageResult: ImageResult, path: String): Int {
        val fp = fopen(path, "wb")
            ?: return -1

        val pngPtr = png_create_write_struct(PNG_LIBPNG_VER_STRING, null, null, null)
            ?: run {
                fclose(fp)
                return -1
            }

        val infoPtr = png_create_info_struct(pngPtr)
            ?: run {
                png_destroy_write_struct(pngPtr.reinterpret(), null)
                fclose(fp)
                return -1
            }

        png_set_IHDR(
            pngPtr,
            infoPtr,
            imageResult.width,
            imageResult.height,
            8,
            PNG_COLOR_TYPE_RGB,
            PNG_INTERLACE_NONE,
            PNG_COMPRESSION_TYPE_DEFAULT,
            PNG_FILTER_TYPE_DEFAULT
        )

        val rowPointers = png_malloc(pngPtr, imageResult.height * sizeOf<CPointerVar<UByteVar>>().toULong())!!
            .reinterpret<CPointerVar<UByteVar>>()

        var pixelIndex = 0
        for (y in 0u..<imageResult.height) {
            val rowPointer = png_malloc(pngPtr, imageResult.width * 3uL)!!
                .also { rowPointers[y.toInt()] = it.reinterpret() }

            var ptr: NativePtr = rowPointer.rawValue
            for (x in 0u..<imageResult.width) {
                val r = (imageResult.pixels[pixelIndex] * 10u).toUByte()
                val g = (imageResult.pixels[pixelIndex] * 10u).toUByte()
                val b = (imageResult.pixels[pixelIndex] * 10u).toUByte()
                pixelIndex++

                interpretOpaquePointed(ptr).reinterpret<UByteVar>().value = r
                ptr = ptr.plus(1)

                interpretOpaquePointed(ptr).reinterpret<UByteVar>().value = g
                ptr = ptr.plus(1)

                interpretOpaquePointed(ptr).reinterpret<UByteVar>().value = b
                ptr = ptr.plus(1)
            }
        }

        png_init_io(pngPtr, fp)
        png_set_rows(pngPtr, infoPtr, rowPointers)
        png_write_png(pngPtr, infoPtr, PNG_TRANSFORM_IDENTITY, null)

        for (y in 0u..<imageResult.height) {
            png_free(pngPtr, rowPointers[y.toInt()])
        }
        png_free(pngPtr, rowPointers)

        png_destroy_write_struct(pngPtr.reinterpret(), infoPtr.reinterpret())
        fclose(fp)
        return 0
    }
}
