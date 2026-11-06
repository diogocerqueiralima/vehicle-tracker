#ifndef VALIDATION_H

#define VALIDATION_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

/**
 * @brief Kind of value a constraint is applied to.
 */
typedef enum
{
    /** A signed 32 bit integer. */
    VALIDATION_VALUE_INT,
    /** A null terminated string. */
    VALIDATION_VALUE_STRING
} validation_value_type_t;

/**
 * @brief A value to validate. It only references the data, it never copies it.
 */
typedef struct
{
    /** Which member of the union is in use. */
    validation_value_type_t type;
    union
    {
        /** Used when @c type is VALIDATION_VALUE_INT. */
        int32_t int_value;
        /** Used when @c type is VALIDATION_VALUE_STRING. May be null, which every built-in constraint rejects. */
        const char* string_value;
    };
} validation_value_t;

/**
 * @brief Signature shared by every constraint, built-in or custom.
 *
 * @param value Value to check. Never null when called through validation_check().
 * @param params Constraint specific parameters, as stored in validation_constraint_t. May be null for constraints that
 * take none.
 * @return true when the value satisfies the constraint.
 */
typedef bool (*validation_fn_t)(const validation_value_t* value, const void* params);

/**
 * @brief A single check: a function plus the parameters it runs with. Meant to live in a @c static @c const table.
 */
typedef struct
{
    /** Function that performs the check. */
    validation_fn_t fn;
    /** Parameters handed to @c fn. Must outlive the constraint. */
    const void* params;
    /** Short static name used to report a violation, for example "range". Never the value. */
    const char* name;
} validation_constraint_t;

/**
 * @brief A set of constraints that must all pass.
 */
typedef struct
{
    /** Constraints of the rule. */
    const validation_constraint_t* constraints;
    /** Number of entries in @c constraints. */
    size_t count;
} validation_rule_t;

/**
 * @brief Outcome of a validation. Holds no copy of the validated value.
 */
typedef struct
{
    /** True when every constraint passed. */
    bool valid;
    /**
     * First constraint that failed, or null when valid or when the input itself was unusable (null rule or value).
     * Points into the rule, so it stays valid as long as the rule.
     */
    const validation_constraint_t* violated;
} validation_result_t;

/** Parameters of validation_int_range(). Both bounds are inclusive. */
typedef struct
{
    int32_t min;
    int32_t max;
} validation_int_range_params_t;

/** Parameters of validation_string_length(). Both bounds are inclusive and count bytes, without the terminator. */
typedef struct
{
    size_t min;
    size_t max;
} validation_string_length_params_t;

/** Parameters of validation_string_one_of(). */
typedef struct
{
    /** Accepted strings. */
    const char* const* values;
    /** Number of entries in @c values. */
    size_t count;
} validation_string_one_of_params_t;

/** Parameters of validation_string_prefix(). */
typedef struct
{
    /** Required prefix, null terminated. */
    const char* prefix;
} validation_string_prefix_params_t;

/** Parameters of validation_string_prefix_one_of(). */
typedef struct
{
    /** Accepted prefixes, each null terminated. */
    const char* const* prefixes;
    /** Number of entries in @c prefixes. */
    size_t count;
} validation_string_prefix_one_of_params_t;

/**
 * @brief Check a value against every constraint of a rule, stopping at the first failure.
 *
 * @param rule Rule to apply.
 * @param value Value to check.
 * @return A result that is valid when every constraint passed, which includes a rule with no constraints. A null
 * @p rule or @p value is reported as invalid with a null violation, and a null constraint function is reported as
 * invalid with that constraint as the violation. Nothing invalid is ever reported as a pass.
 */
validation_result_t validation_check(const validation_rule_t* rule, const validation_value_t* value);

/**
 * @brief Build an integer value.
 *
 * @param value Integer to wrap.
 * @return The wrapped value.
 */
validation_value_t validation_int(int32_t value);

/**
 * @brief Build a string value. The string is referenced, not copied.
 *
 * @param value String to wrap. May be null.
 * @return The wrapped value.
 */
validation_value_t validation_string(const char* value);

/**
 * @brief Constraint: integer within an inclusive range. Params: validation_int_range_params_t.
 *
 * @param value Value to check, must be an integer.
 * @param params A validation_int_range_params_t.
 * @return true when min <= value <= max.
 */
bool validation_int_range(const validation_value_t* value, const void* params);

/**
 * @brief Constraint: string length within an inclusive range. Params: validation_string_length_params_t.
 *
 * @param value Value to check, must be a non-null string.
 * @param params A validation_string_length_params_t.
 * @return true when min <= length <= max.
 */
bool validation_string_length(const validation_value_t* value, const void* params);

/**
 * @brief Constraint: value is present. Params: none.
 *
 * A string must not be null, an empty string is accepted (use validation_string_not_blank() to reject it). An integer
 * is always present.
 *
 * @param value Value to check.
 * @param params Ignored.
 * @return true when @p value is not null and, for a string, its pointer is not null.
 */
bool validation_not_null(const validation_value_t* value, const void* params);

/**
 * @brief Constraint: string is not null, not empty and not only whitespace. Params: none.
 *
 * @param value Value to check, must be a string.
 * @param params Ignored.
 * @return true when the string has at least one non-whitespace character.
 */
bool validation_string_not_blank(const validation_value_t* value, const void* params);

/**
 * @brief Constraint: string equals one of a fixed set. Params: validation_string_one_of_params_t.
 *
 * @param value Value to check, must be a non-null string.
 * @param params A validation_string_one_of_params_t.
 * @return true when the string matches one of the accepted values.
 */
bool validation_string_one_of(const validation_value_t* value, const void* params);

/**
 * @brief Constraint: string starts with a prefix. Params: validation_string_prefix_params_t.
 *
 * @param value Value to check, must be a non-null string.
 * @param params A validation_string_prefix_params_t.
 * @return true when the string starts with the prefix.
 */
bool validation_string_prefix(const validation_value_t* value, const void* params);

/**
 * @brief Constraint: string starts with at least one of several prefixes. Params:
 * validation_string_prefix_one_of_params_t.
 *
 * Null entries are skipped, and an empty prefix matches any non-null string.
 *
 * @param value Value to check, must be a non-null string.
 * @param params A validation_string_prefix_one_of_params_t.
 * @return true when the string starts with one of the prefixes. Always false for an empty set.
 */
bool validation_string_prefix_one_of(const validation_value_t* value, const void* params);

#endif
