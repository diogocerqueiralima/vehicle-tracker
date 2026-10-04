#include "sdkconfig.h"
#include "unity.h"
#include <stdlib.h>

#include "esp_log.h"

void app_main()
{
    unity_run_all_tests();

#if CONFIG_IDF_TARGET_LINUX
    ESP_LOGI("[main]", "All tests completed. Exiting with code %d", Unity.TestFailures == 0 ? EXIT_SUCCESS : EXIT_FAILURE);
    exit(Unity.TestFailures == 0 ? EXIT_SUCCESS : EXIT_FAILURE);
#endif
}
