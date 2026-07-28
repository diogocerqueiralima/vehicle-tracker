#ifndef MQTT_H

#define MQTT_H

#include "esp_err.h"
#include "mqtt_client.h"
#include <stdint.h>

extern esp_mqtt_client_handle_t mqtt_client;

static void set_default_mqtt_client(esp_mqtt_client_handle_t client)
{
		mqtt_client = client;
}

/**
* @brief Initialize the MQTT client with the given parameters.
*
* @param hostname The hostname of the MQTT broker.
* @param port The port of the MQTT broker.
* @param certificate The client certificate for authentication.
* @param certificate_len The length of the client certificate.
* @param key The client private key for authentication.
* @param key_len The length of the client private key.
* @return ESP_OK on success, or an error code on failure.
*/
esp_err_t mqtt_init(char *hostname, uint32_t port, char *certificate, size_t certificate_len, char *key, size_t key_len);

/**
*
* @brief Publish a message to the specified MQTT topic.
*
* @param topic The MQTT topic to publish to.
* @param payload The message payload to publish, must be a null-terminated string.
* @param qos The Quality of Service level for the message (0, 1, or 2).
* @param retain Whether to retain the message on the broker (true or false).
* @return ESP_OK on success, or an error code on failure.
*/
esp_err_t mqtt_publish(const char *topic, const char *payload, int qos, bool retain);

/**
*
* @brief Publish a message to the specified MQTT topic asynchronously.
*
* @param topic The MQTT topic to publish to.
* @param payload The message payload to publish, must be null-terminated string.
* @param qos The Quality of Service level for the message (0, 1, or 2).
* @param retain Whether to retain the message on the broker (true or false).
* @return ESP_OK on success, or an error code on failure.
*/
esp_err_t mqtt_publish_async(const char *topic, const char *payload, int qos, bool retain);

/**
	*
	* @brief Destroy the MQTT client and free associated resources.
	*
	* @return ESP_OK on success, or an error code on failure.
	*/
esp_err_t mqtt_destroy();

#endif
