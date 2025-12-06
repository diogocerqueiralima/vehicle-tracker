#include "tsim.h"
#include "../mqtt/mqtt.h"

static const char *TAG = "TSIM";

static void power_on() {

    ESP_LOGI(TAG, "Powering modem...");

    gpio_set_direction(PWRKEY_PIN, GPIO_MODE_OUTPUT);

    gpio_set_level(PWRKEY_PIN, 0);
    vTaskDelay(pdMS_TO_TICKS(100));
    gpio_set_level(PWRKEY_PIN, 1);
    vTaskDelay(pdMS_TO_TICKS(500));
    gpio_set_level(PWRKEY_PIN, 0);

    ESP_LOGI(TAG, "Power sequence finished.");
}

static void ppp_event_handler(void *arg, esp_event_base_t event_base, int32_t event_id, void *event_data) {

    if (event_id == IP_EVENT_PPP_GOT_IP) {

        ip_event_got_ip_t *event = (ip_event_got_ip_t *) event_data;

        ESP_LOGI(TAG, "PPP CONNECTED");
        ESP_LOGI(TAG, "IP: " IPSTR, IP2STR(&event->ip_info.ip));
        
        esp_mqtt_client_handle_t mqtt_client = mqtt_start();
        if (mqtt_client == NULL) {
            ESP_LOGE(TAG, "Failed to start MQTT client");
            return;
        }

    }

    if (event_id == IP_EVENT_PPP_LOST_IP) {
        ESP_LOGW(TAG, "PPP LOST IP");
    }
    
}

bool tsim_init() {

    power_on();

    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());

    esp_netif_config_t netif_cfg = ESP_NETIF_DEFAULT_PPP();
    esp_netif_t *ppp_netif = esp_netif_new(&netif_cfg);

    ESP_ERROR_CHECK(esp_event_handler_register(
        IP_EVENT,
        ESP_EVENT_ANY_ID,
        &ppp_event_handler,
        NULL
    ));

    esp_modem_dte_config_t dte_config = ESP_MODEM_DTE_DEFAULT_CONFIG();
    dte_config.uart_config.tx_io_num = TX_PIN;
    dte_config.uart_config.rx_io_num = RX_PIN;
    dte_config.uart_config.baud_rate = 115200;
    dte_config.uart_config.port_num = UART_NUM_1;
    dte_config.uart_config.flow_control = ESP_MODEM_FLOW_CONTROL_NONE;
    
    esp_modem_dce_config_t dce_config = ESP_MODEM_DCE_DEFAULT_CONFIG("internet");

    ESP_LOGI(TAG, "Creating modem instance...");
    esp_modem_dce_t *modem = esp_modem_new(&dte_config, &dce_config, ppp_netif);
    if (!modem) {
        ESP_LOGE(TAG, "Failed to create modem");
        return false;
    }

    esp_err_t ret;

    ret = esp_modem_set_mode(modem, ESP_MODEM_MODE_COMMAND);
    if (ret != ESP_OK) {
        ESP_LOGE(TAG, "Failed to set modem mode: %s", esp_err_to_name(ret));
        return false;
    }

    ret = esp_modem_sync(modem);
    if (ret != ESP_OK) {
        ESP_LOGE(TAG, "Failed to sync modem: %s", esp_err_to_name(ret));
        return false;
    }

    ret = esp_modem_set_mode(modem, ESP_MODEM_MODE_DATA);
    if (ret != ESP_OK) {
        ESP_LOGE(TAG, "Failed to set modem to DATA mode: %s", esp_err_to_name(ret));
        return false;
    }

    return true;
}