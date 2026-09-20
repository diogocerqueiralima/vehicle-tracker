#include "mqtt.h"
#include "esp_err.h"
#include "mqtt_client.h"
#include <string.h>

#include "esp_key_config.h"
#include "esp_transport_ssl.h"

esp_mqtt_client_handle_t mqtt_client = nullptr;

esp_err_t mqtt_init(
	const char *hostname, const uint32_t port, unsigned char *certificate, const size_t certificate_len,
	const psa_key_id_t key_id
)
{

	// 1. Build the SSL transport ourselves: esp-mqtt has no field for a PSA key
	// identifier, only esp_transport_ssl_set_client_key_config() does.
	esp_transport_handle_t ssl = esp_transport_ssl_init();
	if (ssl == NULL)
	{
		return ESP_FAIL;
	}

	esp_transport_ssl_enable_global_ca_store(ssl);
	esp_transport_ssl_set_client_cert_data(ssl, (char *) certificate, (int) certificate_len);

	// esp_transport_ssl_set_client_key_config() stores this pointer, not a copy.
	static esp_key_config_t key_config;
	key_config = (esp_key_config_t) {
		.source = ESP_KEY_SOURCE_PSA,
		.psa = {
			.key_id = key_id
		}
	};
	esp_transport_ssl_set_client_key_config(ssl, &key_config);

	// 2. Create an MQTT client configuration structure, handing it the transport
	// built above. esp-mqtt uses it as-is and destroys it on esp_mqtt_client_destroy().
	const esp_mqtt_client_config_t config = {
		.broker = {
			.address = {
				.hostname = hostname,
				.port = port,
				.transport = MQTT_TRANSPORT_OVER_SSL
			}
		},
		.network = {
			.transport = ssl
		}
	};

	// 3. Initialize the MQTT client with the configuration
	esp_mqtt_client_handle_t client = esp_mqtt_client_init(&config);
	if (client == NULL)
	{
		esp_transport_destroy(ssl);
		return ESP_FAIL;
	}

	// 4. Set the default MQTT client handle
	set_default_mqtt_client(client);

	// 5. Start the MQTT client
	const esp_err_t error = esp_mqtt_client_start(client);
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

esp_err_t mqtt_publish_async(const char *topic, const char *payload, int qos, bool retain)
{

	// 1. Check if the MQTT client is initialized
	if (mqtt_client == NULL)
	{
		return ESP_FAIL;
	}

	// 2. Publish the message to the specified topic asynchronously
	int msg_id = esp_mqtt_client_enqueue(mqtt_client, topic, payload, 0, qos, retain, true);

	// 3. Check the return value of the enqueue function (-1 indicates failure, -2 indicates no memory)
	if (msg_id == -1)
	{
		return ESP_FAIL;
	}

	if (msg_id == -2)
	{
		return ESP_ERR_NO_MEM;
	}

	// 4. Return success if the message was enqueued successfully
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
