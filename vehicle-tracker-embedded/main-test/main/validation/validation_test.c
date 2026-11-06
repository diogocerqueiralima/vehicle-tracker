#include <stdint.h>

#include "unity.h"
#include "validation.h"

#define TEST_TAG "[validation]"

#define ARRAY_LEN(a) (sizeof(a) / sizeof((a)[0]))

// --- Helpers ---

static const validation_int_range_params_t QOS_RANGE = {.min = 0, .max = 2};
static const validation_string_length_params_t LEN_RANGE = {.min = 2, .max = 4};
static const validation_string_length_params_t LEN_EXACT_ZERO = {.min = 0, .max = 0};
static const char* const SCHEMES[] = {"mqtt", "mqtts"};
static const validation_string_one_of_params_t SCHEME_SET = {.values = SCHEMES, .count = ARRAY_LEN(SCHEMES)};
static const validation_string_one_of_params_t EMPTY_SET = {.values = nullptr, .count = 0};
static const validation_string_prefix_params_t DEV_PREFIX = {.prefix = "dev-"};
static const validation_string_prefix_one_of_params_t EMPTY_PREFIX_SET = {.prefixes = nullptr, .count = 0};
static const validation_string_prefix_params_t EMPTY_PREFIX = {.prefix = ""};

static bool check_int(validation_fn_t fn, const void* params, int32_t v) {
    validation_value_t value = validation_int(v);
    return fn(&value, params);
}

static bool check_str(validation_fn_t fn, const void* params, const char* s) {
    validation_value_t value = validation_string(s);
    return fn(&value, params);
}

// --- Builders ---

TEST_CASE("builders set the type and the payload", TEST_TAG) {
    validation_value_t i = validation_int(-5);
    TEST_ASSERT_EQUAL(VALIDATION_VALUE_INT, i.type);
    TEST_ASSERT_EQUAL_INT32(-5, i.int_value);

    validation_value_t s = validation_string("abc");
    TEST_ASSERT_EQUAL(VALIDATION_VALUE_STRING, s.type);
    TEST_ASSERT_EQUAL_STRING("abc", s.string_value);
}

// --- Integer range ---

TEST_CASE("int range accepts both bounds", TEST_TAG) {
    TEST_ASSERT_TRUE(check_int(validation_int_range, &QOS_RANGE, 0));
    TEST_ASSERT_TRUE(check_int(validation_int_range, &QOS_RANGE, 1));
    TEST_ASSERT_TRUE(check_int(validation_int_range, &QOS_RANGE, 2));
}

TEST_CASE("int range rejects values just outside the bounds", TEST_TAG) {
    TEST_ASSERT_FALSE(check_int(validation_int_range, &QOS_RANGE, -1));
    TEST_ASSERT_FALSE(check_int(validation_int_range, &QOS_RANGE, 3));
}

TEST_CASE("int range handles the extremes of int32", TEST_TAG) {
    const validation_int_range_params_t full = {.min = INT32_MIN, .max = INT32_MAX};
    TEST_ASSERT_TRUE(check_int(validation_int_range, &full, INT32_MIN));
    TEST_ASSERT_TRUE(check_int(validation_int_range, &full, INT32_MAX));
    TEST_ASSERT_FALSE(check_int(validation_int_range, &QOS_RANGE, INT32_MAX));
    TEST_ASSERT_FALSE(check_int(validation_int_range, &QOS_RANGE, INT32_MIN));
}

TEST_CASE("int range rejects null inputs and strings", TEST_TAG) {
    validation_value_t value = validation_int(1);
    TEST_ASSERT_FALSE(validation_int_range(nullptr, &QOS_RANGE));
    TEST_ASSERT_FALSE(validation_int_range(&value, nullptr));
    TEST_ASSERT_FALSE(check_str(validation_int_range, &QOS_RANGE, "1"));
}

// --- String length ---

TEST_CASE("string length accepts both bounds", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_string_length, &LEN_RANGE, "ab"));
    TEST_ASSERT_TRUE(check_str(validation_string_length, &LEN_RANGE, "abc"));
    TEST_ASSERT_TRUE(check_str(validation_string_length, &LEN_RANGE, "abcd"));
}

TEST_CASE("string length rejects values just outside the bounds", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_string_length, &LEN_RANGE, "a"));
    TEST_ASSERT_FALSE(check_str(validation_string_length, &LEN_RANGE, "abcde"));
}

TEST_CASE("string length handles the empty string", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_string_length, &LEN_EXACT_ZERO, ""));
    TEST_ASSERT_FALSE(check_str(validation_string_length, &LEN_RANGE, ""));
}

TEST_CASE("string length rejects null inputs and integers", TEST_TAG) {
    validation_value_t value = validation_string("abc");
    TEST_ASSERT_FALSE(validation_string_length(nullptr, &LEN_RANGE));
    TEST_ASSERT_FALSE(validation_string_length(&value, nullptr));
    TEST_ASSERT_FALSE(check_str(validation_string_length, &LEN_RANGE, nullptr));
    TEST_ASSERT_FALSE(check_int(validation_string_length, &LEN_RANGE, 3));
}

// --- Not null ---

TEST_CASE("not null accepts strings, including the empty one", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_not_null, nullptr, "a"));
    TEST_ASSERT_TRUE(check_str(validation_not_null, nullptr, ""));
}

TEST_CASE("not null accepts any integer", TEST_TAG) {
    TEST_ASSERT_TRUE(check_int(validation_not_null, nullptr, 0));
    TEST_ASSERT_TRUE(check_int(validation_not_null, nullptr, INT32_MIN));
}

TEST_CASE("not null rejects a null string and a null value", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_not_null, nullptr, nullptr));
    TEST_ASSERT_FALSE(validation_not_null(nullptr, nullptr));
}

// --- Not blank ---

TEST_CASE("not blank accepts text", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_string_not_blank, nullptr, "a"));
    TEST_ASSERT_TRUE(check_str(validation_string_not_blank, nullptr, "  a  "));
}

TEST_CASE("not blank rejects empty and whitespace only strings", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_string_not_blank, nullptr, ""));
    TEST_ASSERT_FALSE(check_str(validation_string_not_blank, nullptr, " "));
    TEST_ASSERT_FALSE(check_str(validation_string_not_blank, nullptr, " \t\r\n "));
}

TEST_CASE("not blank rejects null inputs and integers", TEST_TAG) {
    TEST_ASSERT_FALSE(validation_string_not_blank(nullptr, nullptr));
    TEST_ASSERT_FALSE(check_str(validation_string_not_blank, nullptr, nullptr));
    TEST_ASSERT_FALSE(check_int(validation_string_not_blank, nullptr, 1));
}

// --- One of ---

TEST_CASE("one of accepts every member of the set", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_string_one_of, &SCHEME_SET, "mqtt"));
    TEST_ASSERT_TRUE(check_str(validation_string_one_of, &SCHEME_SET, "mqtts"));
}

TEST_CASE("one of rejects non members, including prefixes and different case", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_string_one_of, &SCHEME_SET, "http"));
    TEST_ASSERT_FALSE(check_str(validation_string_one_of, &SCHEME_SET, "mqt"));
    TEST_ASSERT_FALSE(check_str(validation_string_one_of, &SCHEME_SET, "MQTT"));
    TEST_ASSERT_FALSE(check_str(validation_string_one_of, &SCHEME_SET, ""));
}

TEST_CASE("one of rejects everything with an empty set", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_string_one_of, &EMPTY_SET, "mqtt"));
}

TEST_CASE("one of rejects null inputs, a broken set and integers", TEST_TAG) {
    const validation_string_one_of_params_t broken = {.values = nullptr, .count = 2};
    validation_value_t value = validation_string("mqtt");
    TEST_ASSERT_FALSE(validation_string_one_of(nullptr, &SCHEME_SET));
    TEST_ASSERT_FALSE(validation_string_one_of(&value, nullptr));
    TEST_ASSERT_FALSE(validation_string_one_of(&value, &broken));
    TEST_ASSERT_FALSE(check_str(validation_string_one_of, &SCHEME_SET, nullptr));
    TEST_ASSERT_FALSE(check_int(validation_string_one_of, &SCHEME_SET, 1));
}

// --- Prefix ---

TEST_CASE("prefix accepts strings that start with it", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_string_prefix, &DEV_PREFIX, "dev-1"));
    TEST_ASSERT_TRUE(check_str(validation_string_prefix, &DEV_PREFIX, "dev-"));
}

TEST_CASE("prefix rejects other strings", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_string_prefix, &DEV_PREFIX, "dev"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix, &DEV_PREFIX, "xdev-1"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix, &DEV_PREFIX, "DEV-1"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix, &DEV_PREFIX, ""));
}

TEST_CASE("an empty prefix accepts any non-null string", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_string_prefix, &EMPTY_PREFIX, "anything"));
    TEST_ASSERT_TRUE(check_str(validation_string_prefix, &EMPTY_PREFIX, ""));
}

TEST_CASE("prefix rejects null inputs and integers", TEST_TAG) {
    const validation_string_prefix_params_t null_prefix = {.prefix = nullptr};
    validation_value_t value = validation_string("dev-1");
    TEST_ASSERT_FALSE(validation_string_prefix(nullptr, &DEV_PREFIX));
    TEST_ASSERT_FALSE(validation_string_prefix(&value, nullptr));
    TEST_ASSERT_FALSE(validation_string_prefix(&value, &null_prefix));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix, &DEV_PREFIX, nullptr));
    TEST_ASSERT_FALSE(check_int(validation_string_prefix, &DEV_PREFIX, 1));
}

// --- Prefix one of ---

static const char* const URL_PREFIXES[] = {"mqtt://", "mqtts://"};
static const validation_string_prefix_one_of_params_t URL_PREFIX_SET = {.prefixes = URL_PREFIXES,
                                                                        .count = ARRAY_LEN(URL_PREFIXES)};

TEST_CASE("prefix one of accepts a string that starts with any of the prefixes", TEST_TAG) {
    TEST_ASSERT_TRUE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, "mqtt://broker"));
    TEST_ASSERT_TRUE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, "mqtts://broker"));
    TEST_ASSERT_TRUE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, "mqtts://"));
}

TEST_CASE("prefix one of rejects other strings", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, "http://broker"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, "mqtt:/broker"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, "MQTT://broker"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, " mqtt://broker"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, ""));
}

TEST_CASE("prefix one of rejects everything with an empty set", TEST_TAG) {
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &EMPTY_PREFIX_SET, "mqtt://broker"));
}

TEST_CASE("prefix one of skips null entries and accepts with an empty prefix", TEST_TAG) {
    const char* const prefixes[] = {nullptr, ""};
    const validation_string_prefix_one_of_params_t set = {.prefixes = prefixes, .count = ARRAY_LEN(prefixes)};
    const char* const only_null[] = {nullptr};
    const validation_string_prefix_one_of_params_t null_set = {.prefixes = only_null, .count = 1};

    TEST_ASSERT_TRUE(check_str(validation_string_prefix_one_of, &set, "anything"));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &null_set, "anything"));
}

TEST_CASE("prefix one of rejects null inputs, a broken set and integers", TEST_TAG) {
    const validation_string_prefix_one_of_params_t broken = {.prefixes = nullptr, .count = 2};
    validation_value_t value = validation_string("mqtt://broker");
    TEST_ASSERT_FALSE(validation_string_prefix_one_of(nullptr, &URL_PREFIX_SET));
    TEST_ASSERT_FALSE(validation_string_prefix_one_of(&value, nullptr));
    TEST_ASSERT_FALSE(validation_string_prefix_one_of(&value, &broken));
    TEST_ASSERT_FALSE(check_str(validation_string_prefix_one_of, &URL_PREFIX_SET, nullptr));
    TEST_ASSERT_FALSE(check_int(validation_string_prefix_one_of, &URL_PREFIX_SET, 1));
}

// --- Composition and custom constraints ---

static const validation_constraint_t ID_CONSTRAINTS[] = {
    {.fn = validation_string_not_blank, .params = nullptr, .name = "not_blank"},
    {.fn = validation_string_length, .params = &LEN_RANGE, .name = "length"},
};
static const validation_rule_t ID_RULE = {.constraints = ID_CONSTRAINTS, .count = ARRAY_LEN(ID_CONSTRAINTS)};

TEST_CASE("check passes when every constraint passes", TEST_TAG) {
    validation_value_t value = validation_string("abc");
    validation_result_t result = validation_check(&ID_RULE, &value);
    TEST_ASSERT_TRUE(result.valid);
    TEST_ASSERT_NULL(result.violated);
}

TEST_CASE("check reports the first constraint that fails", TEST_TAG) {
    // Fails both constraints, the first one in the table is reported
    validation_value_t blank = validation_string("");
    validation_result_t result = validation_check(&ID_RULE, &blank);
    TEST_ASSERT_FALSE(result.valid);
    TEST_ASSERT_EQUAL_PTR(&ID_CONSTRAINTS[0], result.violated);
    TEST_ASSERT_EQUAL_STRING("not_blank", result.violated->name);

    // Only the second one fails
    validation_value_t too_long = validation_string("abcdef");
    result = validation_check(&ID_RULE, &too_long);
    TEST_ASSERT_FALSE(result.valid);
    TEST_ASSERT_EQUAL_PTR(&ID_CONSTRAINTS[1], result.violated);
}

TEST_CASE("check accepts a rule with no constraints", TEST_TAG) {
    const validation_rule_t empty = {.constraints = nullptr, .count = 0};
    validation_value_t value = validation_int(1);
    TEST_ASSERT_TRUE(validation_check(&empty, &value).valid);
}

TEST_CASE("check rejects null and malformed input", TEST_TAG) {
    validation_value_t value = validation_string("abc");
    const validation_rule_t broken = {.constraints = nullptr, .count = 1};

    validation_result_t result = validation_check(nullptr, &value);
    TEST_ASSERT_FALSE(result.valid);
    TEST_ASSERT_NULL(result.violated);

    result = validation_check(&ID_RULE, nullptr);
    TEST_ASSERT_FALSE(result.valid);
    TEST_ASSERT_NULL(result.violated);

    result = validation_check(&broken, &value);
    TEST_ASSERT_FALSE(result.valid);
    TEST_ASSERT_NULL(result.violated);
}

TEST_CASE("check reports a constraint without a function", TEST_TAG) {
    const validation_constraint_t constraints[] = {{.fn = nullptr, .params = nullptr, .name = "missing"}};
    const validation_rule_t rule = {.constraints = constraints, .count = 1};
    validation_value_t value = validation_int(1);

    validation_result_t result = validation_check(&rule, &value);
    TEST_ASSERT_FALSE(result.valid);
    TEST_ASSERT_EQUAL_PTR(&constraints[0], result.violated);
}

static bool is_even(const validation_value_t* value, const void* params) {
    (void) params;
    return value != nullptr && value->type == VALIDATION_VALUE_INT && value->int_value % 2 == 0;
}

TEST_CASE("check runs custom constraints next to built-in ones", TEST_TAG) {
    const validation_constraint_t constraints[] = {
        {.fn = validation_int_range, .params = &QOS_RANGE, .name = "range"},
        {.fn = is_even, .params = nullptr, .name = "even"},
    };
    const validation_rule_t rule = {.constraints = constraints, .count = ARRAY_LEN(constraints)};

    validation_value_t two = validation_int(2);
    validation_value_t one = validation_int(1);
    validation_value_t four = validation_int(4);

    TEST_ASSERT_TRUE(validation_check(&rule, &two).valid);
    TEST_ASSERT_EQUAL_STRING("even", validation_check(&rule, &one).violated->name);
    TEST_ASSERT_EQUAL_STRING("range", validation_check(&rule, &four).violated->name);
}
