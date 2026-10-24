#include <string.h>

#include "esp_err.h"
#include "unity.h"
#include "uuid.h"

#define TEST_TAG "[uuid]"

// Number of UUIDs generated when checking uniqueness
#define SAMPLE_COUNT 64

// Positions of the dashes in the canonical 8-4-4-4-12 representation
static const size_t DASH_POSITIONS[] = {8, 13, 18, 23};

// --- Generation ---

TEST_CASE("generate rejects a null output", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, uuid_generate_v4(nullptr));
}

TEST_CASE("generated uuid has version 4 and RFC 4122 variant", TEST_TAG) {
    for (int i = 0; i < SAMPLE_COUNT; i++) {
        uuid_t uuid;
        TEST_ASSERT_EQUAL(ESP_OK, uuid_generate_v4(&uuid));

        // High nibble of byte 6 is the version, two high bits of byte 8 are the variant (10)
        TEST_ASSERT_EQUAL_HEX8(0x40, uuid.bytes[6] & 0xF0);
        TEST_ASSERT_EQUAL_HEX8(0x80, uuid.bytes[8] & 0xC0);
    }
}

TEST_CASE("generated uuids are different", TEST_TAG) {
    uuid_t uuids[SAMPLE_COUNT];
    for (int i = 0; i < SAMPLE_COUNT; i++) {
        TEST_ASSERT_EQUAL(ESP_OK, uuid_generate_v4(&uuids[i]));
    }

    for (int i = 0; i < SAMPLE_COUNT; i++) {
        for (int j = i + 1; j < SAMPLE_COUNT; j++) {
            TEST_ASSERT_NOT_EQUAL(0, memcmp(uuids[i].bytes, uuids[j].bytes, UUID_BYTES_SIZE));
        }
    }
}

// --- String conversion ---

TEST_CASE("to_str formats a known uuid in lowercase", TEST_TAG) {
    const uuid_t uuid = {.bytes = {
        0x12, 0x3e, 0x45, 0x67, 0xe8, 0x9b, 0x42, 0xd3, 0xa4, 0x56, 0x42, 0x66, 0x14, 0x17, 0x40, 0xAB
    }};

    char str[UUID_STR_SIZE];
    TEST_ASSERT_EQUAL(ESP_OK, uuid_to_str(&uuid, str, sizeof(str)));
    TEST_ASSERT_EQUAL_STRING("123e4567-e89b-42d3-a456-4266141740ab", str);
}

TEST_CASE("to_str pads bytes below 0x10 with a zero", TEST_TAG) {
    const uuid_t uuid = {.bytes = {0}};

    char str[UUID_STR_SIZE];
    TEST_ASSERT_EQUAL(ESP_OK, uuid_to_str(&uuid, str, sizeof(str)));
    TEST_ASSERT_EQUAL_STRING("00000000-0000-0000-0000-000000000000", str);
}

TEST_CASE("to_str of a generated uuid has the canonical layout", TEST_TAG) {
    uuid_t uuid;
    TEST_ASSERT_EQUAL(ESP_OK, uuid_generate_v4(&uuid));

    char str[UUID_STR_SIZE];
    TEST_ASSERT_EQUAL(ESP_OK, uuid_to_str(&uuid, str, sizeof(str)));
    TEST_ASSERT_EQUAL(UUID_STR_LEN, strlen(str));

    for (size_t i = 0; i < UUID_STR_LEN; i++) {
        bool is_dash = false;
        for (size_t j = 0; j < sizeof(DASH_POSITIONS) / sizeof(DASH_POSITIONS[0]); j++) {
            is_dash |= (i == DASH_POSITIONS[j]);
        }

        if (is_dash) {
            TEST_ASSERT_EQUAL_CHAR('-', str[i]);
        } else {
            TEST_ASSERT_TRUE((str[i] >= '0' && str[i] <= '9') || (str[i] >= 'a' && str[i] <= 'f'));
        }
    }

    // The version digit is the first character of the third group
    TEST_ASSERT_EQUAL_CHAR('4', str[14]);
}

TEST_CASE("to_str rejects null arguments", TEST_TAG) {
    const uuid_t uuid = {.bytes = {0}};
    char str[UUID_STR_SIZE];

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, uuid_to_str(nullptr, str, sizeof(str)));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, uuid_to_str(&uuid, nullptr, sizeof(str)));
}

TEST_CASE("to_str rejects a buffer without room for the terminator", TEST_TAG) {
    const uuid_t uuid = {.bytes = {0}};
    char str[UUID_STR_SIZE];

    memset(str, 'x', sizeof(str));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_SIZE, uuid_to_str(&uuid, str, UUID_STR_LEN));

    // The buffer is left untouched on failure
    TEST_ASSERT_EQUAL_CHAR('x', str[0]);
}
