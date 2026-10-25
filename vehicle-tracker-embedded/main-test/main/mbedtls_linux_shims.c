#include "sdkconfig.h"

#if CONFIG_IDF_TARGET_LINUX

#include <stdlib.h>
#include <time.h>

#include "mbedtls/platform_time.h"

/*
 * The mbedtls port of ESP-IDF is not built for the linux target, but the mbedtls library still expects these symbols.
 * They are the plain host equivalents of what the port provides on the chip.
 */

void *esp_mbedtls_mem_calloc(const size_t n, const size_t size) {
    return calloc(n, size);
}

void esp_mbedtls_mem_free(void *ptr) {
    free(ptr);
}

mbedtls_ms_time_t mbedtls_ms_time() {
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (mbedtls_ms_time_t) now.tv_sec * 1000 + now.tv_nsec / 1000000;
}

#endif
