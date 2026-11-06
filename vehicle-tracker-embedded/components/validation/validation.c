#include "validation.h"

#include <ctype.h>
#include <string.h>

/**
 * Get the string of a value, or null when the value is not a usable string.
 */
static const char* string_of(const validation_value_t* value) {
    if (value == nullptr || value->type != VALIDATION_VALUE_STRING) {
        return nullptr;
    }

    return value->string_value;
}

validation_result_t validation_check(const validation_rule_t* rule, const validation_value_t* value) {

    // 1. Invalid input is a violation, never a pass
    if (rule == nullptr || value == nullptr) {
        return (validation_result_t){.valid = false, .violated = nullptr};
    }

    if (rule->count > 0 && rule->constraints == nullptr) {
        return (validation_result_t){.valid = false, .violated = nullptr};
    }

    // 2. Every constraint must pass, the first failure is reported
    for (size_t i = 0; i < rule->count; i++) {

        const validation_constraint_t* constraint = &rule->constraints[i];

        if (constraint->fn == nullptr || !constraint->fn(value, constraint->params)) {
            return (validation_result_t){.valid = false, .violated = constraint};
        }

    }

    return (validation_result_t){.valid = true, .violated = nullptr};
}

validation_value_t validation_int(int32_t value) {
    return (validation_value_t){.type = VALIDATION_VALUE_INT, .int_value = value};
}

validation_value_t validation_string(const char* value) {
    return (validation_value_t){.type = VALIDATION_VALUE_STRING, .string_value = value};
}

bool validation_int_range(const validation_value_t* value, const void* params) {

    if (value == nullptr || value->type != VALIDATION_VALUE_INT || params == nullptr) {
        return false;
    }

    const validation_int_range_params_t* range = params;
    return value->int_value >= range->min && value->int_value <= range->max;
}

bool validation_string_length(const validation_value_t* value, const void* params) {

    const char* string = string_of(value);
    if (string == nullptr || params == nullptr) {
        return false;
    }

    const validation_string_length_params_t* length = params;
    const size_t len = strlen(string);
    return len >= length->min && len <= length->max;
}

bool validation_not_null(const validation_value_t* value, const void* params) {
    (void) params;

    if (value == nullptr) {
        return false;
    }

    return value->type != VALIDATION_VALUE_STRING || value->string_value != nullptr;
}

bool validation_string_not_blank(const validation_value_t* value, const void* params) {
    (void) params;

    const char* string = string_of(value);
    if (string == nullptr) {
        return false;
    }

    for (; *string != '\0'; string++) {
        if (!isspace((unsigned char) *string)) {
            return true;
        }
    }

    return false;
}

bool validation_string_one_of(const validation_value_t* value, const void* params) {

    const char* string = string_of(value);
    if (string == nullptr || params == nullptr) {
        return false;
    }

    const validation_string_one_of_params_t* set = params;
    if (set->count > 0 && set->values == nullptr) {
        return false;
    }

    for (size_t i = 0; i < set->count; i++) {
        if (set->values[i] != nullptr && strcmp(string, set->values[i]) == 0) {
            return true;
        }
    }

    return false;
}

bool validation_string_prefix(const validation_value_t* value, const void* params) {
    const char* string = string_of(value);
    if (string == nullptr || params == nullptr) {
        return false;
    }

    const validation_string_prefix_params_t* prefix = params;
    if (prefix->prefix == nullptr) {
        return false;
    }

    return strncmp(string, prefix->prefix, strlen(prefix->prefix)) == 0;
}

bool validation_string_prefix_one_of(const validation_value_t* value, const void* params) {
    const char* string = string_of(value);
    if (string == nullptr || params == nullptr) {
        return false;
    }

    const validation_string_prefix_one_of_params_t* set = params;
    if (set->count > 0 && set->prefixes == nullptr) {
        return false;
    }

    for (size_t i = 0; i < set->count; i++) {
        if (set->prefixes[i] != nullptr && strncmp(string, set->prefixes[i], strlen(set->prefixes[i])) == 0) {
            return true;
        }
    }

    return false;
}
