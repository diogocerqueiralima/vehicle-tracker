#include "sdkconfig.h"
#include "unity.h"
#include <stdlib.h>

#include "esp_log.h"
#include "nvs_flash.h"
#include "security.h"
#include "storage.h"

/**
 * Runs before every test of every suite. Every test starts from an empty NVS partition, so no test depends on what
 * another one stored.
 */
void setUp() {
    storage_destroy();
    TEST_ASSERT_EQUAL(ESP_OK, nvs_flash_erase());
    TEST_ASSERT_EQUAL(ESP_OK, storage_init());
}

/**
 * Runs after every test of every suite.
 */
void tearDown() {
    security_destroy();
    storage_destroy();
}

void app_main()
{
    unity_run_all_tests();

#if CONFIG_IDF_TARGET_LINUX
    ESP_LOGI("[main]", "All tests completed. Exiting with code %d", Unity.TestFailures == 0 ? EXIT_SUCCESS : EXIT_FAILURE);
    exit(Unity.TestFailures == 0 ? EXIT_SUCCESS : EXIT_FAILURE);
#endif
}
