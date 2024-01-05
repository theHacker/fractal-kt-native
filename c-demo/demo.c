#include <png.h>
#include <stdio.h>

int main() {
    FILE* fp = fopen("output.png", "wb");
    if (!fp) return 1;

    png_structp png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
    if (!png_ptr) {
        fclose(fp);
        return 1;
    }

    png_infop info_ptr = png_create_info_struct(png_ptr);
    if (!info_ptr) {
        png_destroy_write_struct(&png_ptr, NULL);
        fclose(fp);
        return 1;
    }

    int w = 20, h = 20;
    png_set_IHDR(png_ptr, info_ptr, w, h, 8, PNG_COLOR_TYPE_RGB, PNG_INTERLACE_NONE, PNG_COMPRESSION_TYPE_DEFAULT, PNG_FILTER_TYPE_DEFAULT);
 
    png_byte** row_pointers = png_malloc(png_ptr, h * sizeof(png_byte*));
    for (int y = 0; y < h; y++) {
        png_byte* row = png_malloc(png_ptr, w * 3 * sizeof(png_byte));
        row_pointers[y] = row;

        png_byte* ptr = row;
        for (int x = 0; x < w; x++) {
            png_byte r = x * 4;
            png_byte g = 0;
            png_byte b = y * 4;

            *ptr++ = r;
            *ptr++ = g;
            *ptr++ = b;
        }
    }

    png_init_io(png_ptr, fp);
    png_set_rows(png_ptr, info_ptr, row_pointers);
    png_write_png(png_ptr, info_ptr, PNG_TRANSFORM_IDENTITY, NULL);

    for (int y = 0; y < h; y++) {
        png_free(png_ptr, row_pointers[y]);
    }
    png_free(png_ptr, row_pointers);
    
    png_destroy_write_struct(&png_ptr, &info_ptr);
    fclose(fp);

    printf("Worked :-)\n");
    return 0;
}
