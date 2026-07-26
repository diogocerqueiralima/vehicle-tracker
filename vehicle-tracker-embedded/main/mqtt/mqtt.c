#include "mqtt.h"
#include "esp_err.h"
#include "mqtt_client.h"
#include <string.h>

esp_mqtt_client_handle_t mqtt_client = NULL;

esp_err_t mqtt_init(char *hostname, uint32_t port, char *certificate, size_t certificate_len, char *key, size_t key_len)
{

	// 1. Create an MQTT client configuration structure
	esp_mqtt_client_config_t config = {
		.broker = {
			.address = {
				.hostname = hostname,
				.port = port,
				.transport = MQTT_TRANSPORT_OVER_SSL
			},
			.verification = {
				.use_global_ca_store = true
			}
		},
		.credentials = {
			.authentication = {
				.certificate = certificate,
				.certificate_len = certificate_len,
				.key = key,
				.key_len = key_len
			}
		}
	};

	// 2. Initialize the MQTT client with the configuration
	esp_mqtt_client_handle_t client = esp_mqtt_client_init(&config);
	if (client == NULL)
	{
		return ESP_FAIL;
	}

	// 3. Set the default MQTT client handle
	set_default_mqtt_client(client);

	// 4. Start the MQTT client
	esp_err_t error = esp_mqtt_client_start(client);
	if (error != ESP_OK)
	{
		return error;
	}

	return ESP_OK;
}

esp_err_t mqtt_publish(const char *topic, const char *payload, int qos, bool retain)
{
	
	// 1. Check if the MQTT client is initialized 
	if (mqtt_client == NULL)
	{
		return ESP_FAIL;
	}

	// 2. Publish the message to the specified topic
	int msg_id = esp_mqtt_client_publish(mqtt_client, topic, payload, 0, qos, retain);
	if (msg_id < 0)
	{
		return ESP_FAIL;
	}

	return ESP_OK;
}

esp_err_t mqtt_destroy()
{

	// 1. Check if the MQTT client is initialized 
	if (mqtt_client == NULL)
	{
		return ESP_FAIL;
	}

	// 2. Stop the MQTT client
	esp_err_t error = esp_mqtt_client_stop(mqtt_client);
	if (error != ESP_OK)
	{
		return error;
	}

	// 3. Destroy the MQTT client 
	error = esp_mqtt_client_destroy(mqtt_client);
	if (error != ESP_OK)
	{
		return error;
	}

	// 4. Reset the default MQTT client handle
	set_default_mqtt_client(NULL);

	return ESP_OK;
}
