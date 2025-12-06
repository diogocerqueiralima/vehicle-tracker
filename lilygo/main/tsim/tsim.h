#ifndef TSIM_H
#define TSIM_H

#include <stdbool.h>

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "driver/gpio.h"
#include "esp_modem_api.h"
#include "esp_event.h"
#include "esp_netif.h"
#include "esp_system.h"

#include "../utils/at.h"

#define PWRKEY_PIN  4
#define TX_PIN      27
#define RX_PIN      26

bool tsim_init();

#endif // TSIM_H