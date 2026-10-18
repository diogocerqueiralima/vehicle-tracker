#include <string.h>

#include "esp_err.h"
#include "nvs_flash.h"
#include "unity.h"
#include "storage.h"

#define TEST_TAG "[storage]"
#define NAMESPACE "test_ns"
#define KEY "test_key"

// Context builders for the access levels under test
#define RW(ns_, key_) ((storage_ctx_t){.namespace = (ns_), .key = (key_), .mode = STORAGE_MODE_READ_WRITE})
#define RO(ns_, key_) ((storage_ctx_t){.namespace = (ns_), .key = (key_), .mode = STORAGE_MODE_READ_ONLY})
#define PURGE(ns_, key_) ((storage_ctx_t){.namespace = (ns_), .key = (key_), .mode = STORAGE_MODE_READ_WRITE_PURGE})

// Longer than the 15 characters NVS accepts for namespaces and keys
#define TOO_LONG_NAME "abcdefghijklmnop"

/**
 * Every test starts from an empty NVS partition, so no test depends on what another one stored.
 */
void setUp() {
    storage_destroy();
    TEST_ASSERT_EQUAL(ESP_OK, nvs_flash_erase());
    TEST_ASSERT_EQUAL(ESP_OK, storage_init());
}

void tearDown() {
    storage_destroy();
}

// --- Lifecycle ---

TEST_CASE("init succeeds again after destroy", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, storage_destroy());
    TEST_ASSERT_EQUAL(ESP_OK, storage_init());
}

TEST_CASE("stored value survives destroy and init", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 1234));

    TEST_ASSERT_EQUAL(ESP_OK, storage_destroy());
    TEST_ASSERT_EQUAL(ESP_OK, storage_init());

    uint32_t value = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&ctx, &value));
    TEST_ASSERT_EQUAL_UINT32(1234, value);
}

// --- Integers ---

TEST_CASE("unsigned integers round trip at their limits", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);

    uint8_t u8 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u8(&ctx, UINT8_MAX));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u8(&ctx, &u8));
    TEST_ASSERT_EQUAL_UINT8(UINT8_MAX, u8);

    uint16_t u16 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u16(&ctx, UINT16_MAX));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u16(&ctx, &u16));
    TEST_ASSERT_EQUAL_UINT16(UINT16_MAX, u16);

    uint32_t u32 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, UINT32_MAX));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&ctx, &u32));
    TEST_ASSERT_EQUAL_UINT32(UINT32_MAX, u32);

    uint64_t u64 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u64(&ctx, UINT64_MAX));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u64(&ctx, &u64));
    TEST_ASSERT_EQUAL_UINT64(UINT64_MAX, u64);
}

TEST_CASE("signed integers round trip at their limits", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);

    int8_t i8 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_i8(&ctx, INT8_MIN));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_i8(&ctx, &i8));
    TEST_ASSERT_EQUAL_INT8(INT8_MIN, i8);

    int16_t i16 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_i16(&ctx, INT16_MIN));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_i16(&ctx, &i16));
    TEST_ASSERT_EQUAL_INT16(INT16_MIN, i16);

    int32_t i32 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_i32(&ctx, INT32_MIN));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_i32(&ctx, &i32));
    TEST_ASSERT_EQUAL_INT32(INT32_MIN, i32);

    int64_t i64 = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_i64(&ctx, INT64_MIN));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_i64(&ctx, &i64));
    TEST_ASSERT_EQUAL_INT64(INT64_MIN, i64);
}

TEST_CASE("integer overwrite keeps the latest value", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    uint32_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 1));
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 2));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&ctx, &value));
    TEST_ASSERT_EQUAL_UINT32(2, value);
}

TEST_CASE("get integer fails with type mismatch when another type is stored", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    uint16_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 1));
    TEST_ASSERT_NOT_EQUAL(ESP_OK, storage_get_u16(&ctx, &value));
}

// --- Floating point ---

TEST_CASE("float round trips", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    float value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_float(&ctx, -41.1496f));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_float(&ctx, &value));
    TEST_ASSERT_EQUAL_FLOAT(-41.1496f, value);
}

TEST_CASE("double round trips", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    double value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_double(&ctx, 8.611234567890123));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_double(&ctx, &value));
    TEST_ASSERT_EQUAL_DOUBLE(8.611234567890123, value);
}

TEST_CASE("get float fails when a double is stored", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    float value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_double(&ctx, 1.5));
    TEST_ASSERT_NOT_EQUAL(ESP_OK, storage_get_float(&ctx, &value));
}

// --- Strings ---

TEST_CASE("string round trips", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    char buffer[32] = {};

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&ctx, "vehicle-tracker"));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_str(&ctx, buffer, sizeof(buffer)));
    TEST_ASSERT_EQUAL_STRING("vehicle-tracker", buffer);
}

TEST_CASE("string size includes the null terminator", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    size_t len = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&ctx, "abc"));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_str_size(&ctx, &len));
    TEST_ASSERT_EQUAL_UINT(4, len);
}

TEST_CASE("get string fails when the buffer is too small", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    char buffer[3] = {};

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&ctx, "abcdef"));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_INVALID_LENGTH, storage_get_str(&ctx, buffer, sizeof(buffer)));
}

TEST_CASE("get string fails with type mismatch when a blob is stored", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    const uint8_t blob[] = {1, 2, 3};
    char buffer[8] = {};

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_blob(&ctx, blob, sizeof(blob)));
    TEST_ASSERT_NOT_EQUAL(ESP_OK, storage_get_str(&ctx, buffer, sizeof(buffer)));
}

// --- Blobs ---

TEST_CASE("blob round trips", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    const uint8_t blob[] = {0x00, 0xFF, 0x10, 0x00, 0x7F};
    uint8_t read[sizeof(blob)] = {};

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_blob(&ctx, blob, sizeof(blob)));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_blob(&ctx, read, sizeof(read)));
    TEST_ASSERT_EQUAL_UINT8_ARRAY(blob, read, sizeof(blob));
}

TEST_CASE("blob size matches the stored length", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    const uint8_t blob[7] = {1, 2, 3, 4, 5, 6, 7};
    size_t len = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_blob(&ctx, blob, sizeof(blob)));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_blob_size(&ctx, &len));
    TEST_ASSERT_EQUAL_UINT(sizeof(blob), len);
}

TEST_CASE("get blob fails when the buffer is too small", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    const uint8_t blob[8] = {1, 2, 3, 4, 5, 6, 7, 8};
    uint8_t read[4] = {};

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_blob(&ctx, blob, sizeof(blob)));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_INVALID_LENGTH, storage_get_blob(&ctx, read, sizeof(read)));
}

TEST_CASE("blob overwrite with a shorter value reports the new size", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    const uint8_t long_blob[8] = {1, 2, 3, 4, 5, 6, 7, 8};
    const uint8_t short_blob[2] = {9, 10};
    size_t len = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_blob(&ctx, long_blob, sizeof(long_blob)));
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_blob(&ctx, short_blob, sizeof(short_blob)));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_blob_size(&ctx, &len));
    TEST_ASSERT_EQUAL_UINT(sizeof(short_blob), len);
}

// --- Missing values and erase ---

TEST_CASE("get fails with not found when the key was never stored", TEST_TAG) {
    const storage_ctx_t rw = RW(NAMESPACE, KEY);
    uint32_t value = 0;

    // Namespace exists, key does not
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&rw, 1));
    const storage_ctx_t other_key = RW(NAMESPACE, "other_key");
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_u32(&other_key, &value));
}

TEST_CASE("read-only get fails with not found when the namespace does not exist", TEST_TAG) {
    const storage_ctx_t ctx = RO("missing_ns", KEY);
    uint32_t value = 0;
    size_t len = 0;

    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_u32(&ctx, &value));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_blob_size(&ctx, &len));
}

TEST_CASE("erase removes the value", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    uint32_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 5));
    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&ctx));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_u32(&ctx, &value));
}

TEST_CASE("erase fails with not found when there is nothing to erase", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);

    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_erase(&ctx));
}

TEST_CASE("erase only removes the given key", TEST_TAG) {
    const storage_ctx_t first = RW(NAMESPACE, "first");
    const storage_ctx_t second = RW(NAMESPACE, "second");
    uint32_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&first, 1));
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&second, 2));
    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&first));

    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&second, &value));
    TEST_ASSERT_EQUAL_UINT32(2, value);
}

// --- Namespaces and open modes ---

TEST_CASE("same key in different namespaces holds independent values", TEST_TAG) {
    const storage_ctx_t first = RW("ns_one", KEY);
    const storage_ctx_t second = RW("ns_two", KEY);
    uint32_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&first, 1));
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&second, 2));

    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&first, &value));
    TEST_ASSERT_EQUAL_UINT32(1, value);
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&second, &value));
    TEST_ASSERT_EQUAL_UINT32(2, value);
}

TEST_CASE("read-only context reads a value written by a read-write context", TEST_TAG) {
    const storage_ctx_t rw = RW(NAMESPACE, KEY);
    const storage_ctx_t ro = RO(NAMESPACE, KEY);
    uint32_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&rw, 77));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&ro, &value));
    TEST_ASSERT_EQUAL_UINT32(77, value);
}

TEST_CASE("read-only context rejects every write", TEST_TAG) {
    const storage_ctx_t rw = RW(NAMESPACE, KEY);
    const storage_ctx_t ro = RO(NAMESPACE, KEY);
    const uint8_t blob[] = {1};
    uint32_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&rw, 1));

    TEST_ASSERT_EQUAL(ESP_ERR_NVS_READ_ONLY, storage_set_u32(&ro, 2));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_READ_ONLY, storage_set_float(&ro, 2.0f));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_READ_ONLY, storage_set_str(&ro, "x"));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_READ_ONLY, storage_set_blob(&ro, blob, sizeof(blob)));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_READ_ONLY, storage_erase(&ro));

    // The original value is untouched
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&rw, &value));
    TEST_ASSERT_EQUAL_UINT32(1, value);
}

TEST_CASE("read-write-purge context writes, overwrites and erases", TEST_TAG) {
    const storage_ctx_t ctx = PURGE(NAMESPACE, KEY);
    uint32_t value = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 1));
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 2));
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&ctx, &value));
    TEST_ASSERT_EQUAL_UINT32(2, value);

    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&ctx));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_u32(&ctx, &value));
}

// --- Invalid arguments ---

TEST_CASE("operations reject a null context", TEST_TAG) {
    uint32_t value = 0;
    size_t len = 0;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_u32(nullptr, &value));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_u32(nullptr, 1));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_str_size(nullptr, &len));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_erase(nullptr));
}

TEST_CASE("operations reject a missing namespace or key", TEST_TAG) {
    const storage_ctx_t no_namespace = RW(nullptr, KEY);
    const storage_ctx_t no_key = RW(NAMESPACE, nullptr);

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_u32(&no_namespace, 1));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_u32(&no_key, 1));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_erase(&no_namespace));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_erase(&no_key));
}

TEST_CASE("operations reject empty names", TEST_TAG) {
    const storage_ctx_t empty_namespace = RW("", KEY);
    const storage_ctx_t empty_key = RW(NAMESPACE, "");

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_u32(&empty_namespace, 1));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_u32(&empty_key, 1));
}

TEST_CASE("operations reject names longer than the NVS limit", TEST_TAG) {
    const storage_ctx_t long_namespace = RW(TOO_LONG_NAME, KEY);
    const storage_ctx_t long_key = RW(NAMESPACE, TOO_LONG_NAME);

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_u32(&long_namespace, 1));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_u32(&long_key, 1));
}

TEST_CASE("getters reject a null output", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, 1));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_u32(&ctx, nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_float(&ctx, nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_double(&ctx, nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_str_size(&ctx, nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_str(&ctx, nullptr, 8));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_blob_size(&ctx, nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_blob(&ctx, nullptr, 8));
}

TEST_CASE("string and blob operations reject null values and empty buffers", TEST_TAG) {
    const storage_ctx_t ctx = RW(NAMESPACE, KEY);
    const uint8_t blob[] = {1};
    char buffer[4] = {};

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_str(&ctx, nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_blob(&ctx, nullptr, 1));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_set_blob(&ctx, blob, 0));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_str(&ctx, buffer, 0));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, storage_get_blob(&ctx, buffer, 0));
}
