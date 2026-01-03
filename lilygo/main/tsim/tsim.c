#include "tsim.h"

static const char *TAG = "TSIM";

/**
 * @brief Power on the TSIM modem using the PWRKEY pin.
 */
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

int tsim_init(esp_modem_dce_t **modem, ppp_event_handler_t handler) {

    power_on();

    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());

    esp_netif_config_t netif_cfg = ESP_NETIF_DEFAULT_PPP();
    esp_netif_t *ppp_netif = esp_netif_new(&netif_cfg);

    ESP_ERROR_CHECK(esp_event_handler_register(
        IP_EVENT,
        ESP_EVENT_ANY_ID,
        handler,
        NULL
    ));

    esp_modem_dte_config_t dte_config = ESP_MODEM_DTE_DEFAULT_CONFIG();
    dte_config.uart_config.tx_io_num = TX_PIN;
    dte_config.uart_config.rx_io_num = RX_PIN;
    dte_config.uart_config.baud_rate = 115200;
    dte_config.uart_config.port_num = UART_NUM_1;
    dte_config.uart_config.flow_control = ESP_MODEM_FLOW_CONTROL_NONE;
    
    esp_modem_dce_config_t dce_config = ESP_MODEM_DCE_DEFAULT_CONFIG("internet");

    *modem = esp_modem_new_dev(ESP_MODEM_DCE_SIM7600, &dte_config, &dce_config, ppp_netif);
    if (*modem == NULL) {
        return CREATE_MODEM_ERROR;
    }

    esp_err_t ret;

    ret = esp_modem_set_mode(*modem, ESP_MODEM_MODE_COMMAND);

    ret = esp_modem_sync(*modem);
    if (ret != ESP_OK) {
        return SYNC_MODEM_ERROR;
    }

    ret = esp_modem_at(*modem, "ATE0", NULL, 1000);
    if (ret != ESP_OK) {
        return DISABLE_ECHO_ERROR;
    }

    ret = esp_modem_at_raw(*modem, "AT+CTZU=1\r", NULL, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return UPDATE_TIME_AND_TIMEZONE_ERROR;
    }

    ret = esp_modem_at_raw(*modem, "AT+CNTP=\"pool.ntp.org\",0\r", NULL, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return SET_NTP_SERVER_ERROR;
    }

    ret = esp_modem_at_raw(*modem, "AT+CNTP\r", NULL, "+CNTP: 0", "ERROR", 1000);
    if (ret != ESP_OK) {
        return UPDATE_NTP_ERROR;
    }

    return TSIM_SUCCESS;
}

int tsim_set_data_mode(esp_modem_dce_t *modem) {

    esp_err_t ret;
    
    ret = esp_modem_set_mode(modem, ESP_MODEM_MODE_DATA);
    if (ret != ESP_OK) {
        return SET_MODEM_DATA_MODE_ERROR;
    }
    
    return TSIM_SUCCESS;
}

int tsim_pause_data_mode(esp_modem_dce_t *modem) {

    esp_err_t ret;

    ret = esp_modem_pause_net(modem, true);
    if (ret != ESP_OK) {
        return PAUSE_NETWORK_ERROR;
    }
    
    return TSIM_SUCCESS;
}

int tsim_resume_data_mode(esp_modem_dce_t *modem) {

    esp_err_t ret;

    ret = esp_modem_pause_net(modem, false);
    if (ret != ESP_OK) {
        return RESUME_NETWORK_ERROR;
    }
    
    return TSIM_SUCCESS;
}

char *tsim_get_error_message(int code) {
    switch (code) {
        case TSIM_SUCCESS:
            return "Success";
        case CREATE_MODEM_ERROR:
            return "Failed to create modem instance";
        case SET_MODEM_COMMAND_MODE_ERROR:
            return "Failed to set modem to command mode";
        case SYNC_MODEM_ERROR:
            return "Failed to synchronize with modem";
        case DISABLE_ECHO_ERROR:
            return "Failed to disable echo on modem";
        case PAUSE_NETWORK_ERROR:
            return "Failed to pause network";
        case RESUME_NETWORK_ERROR:
            return "Failed to resume network";
        case UPDATE_TIME_AND_TIMEZONE_ERROR:
            return "Failed to update time and timezone settings";
        case SET_NTP_SERVER_ERROR:
            return "Failed to set NTP server";
        case UPDATE_NTP_ERROR:
            return "Failed to update NTP time";
        default:
            return "Unknown error code";
    }
}