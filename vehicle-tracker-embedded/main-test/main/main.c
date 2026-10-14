#include "sdkconfig.h"
#include "unity.h"
#include <stdlib.h>

#include "esp_log.h"
#include "nvs_flash.h"
#include "security.h"
#include "storage.h"

/**
 * Unity runs one setUp/tearDown pair for the whole binary, so they live here and serve every test suite.
 * Every test starts from an empty NVS partition with a fresh device identity and no credentials,
 * so no test depends on what another one stored or generated.
 */
void setUp() {
    storage_destroy();
    TEST_ASSERT_EQUAL(ESP_OK, nvs_flash_erase());
    TEST_ASSERT_EQUAL(ESP_OK, storage_init());
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
}

void tearDown() {
    security_revoke();
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
