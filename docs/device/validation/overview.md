# Validation Overview

The `validation` component offers declarative, reusable validators for the firmware. It has no dependency on any other project component and never allocates memory.

## Concepts

- **Value**: an integer or a string, referenced and never copied.
- **Constraint**: a function, its parameters and a name. Lives in a `static const` table.
- **Rule**: a list of constraints that must all pass.
- **Result**: says whether the value is valid and, if not, which constraint failed. It never contains the value.

## Built-in constraints

| Constraint | Applies to | Parameters |
|---|---|---|
| `validation_int_range` | integer | min, max (inclusive) |
| `validation_string_length` | string | min, max (inclusive) |
| `validation_not_null` | integer, string | none |
| `validation_string_not_blank` | string | none |
| `validation_string_one_of` | string | accepted values |
| `validation_string_prefix` | string | prefix |
| `validation_string_prefix_one_of` | string | accepted prefixes |

`validation_not_null` rejects a null string but accepts an empty one, and an integer always passes. Strings are measured in bytes. `validation_string_not_blank` treats the C locale whitespace characters as blank.

## Defining and using rules

Define the constraints and the rule once, as `static const` data owned by the caller:

```c
#include "validation.h"

static const validation_int_range_params_t QOS_RANGE = {.min = 0, .max = 2};

static const validation_constraint_t QOS_CONSTRAINTS[] = {
    {.fn = validation_int_range, .params = &QOS_RANGE, .name = "range"},
};

static const validation_rule_t QOS_RULE = {
    .constraints = QOS_CONSTRAINTS,
    .count = sizeof(QOS_CONSTRAINTS) / sizeof(QOS_CONSTRAINTS[0]),
};
```

Wrap the value, check it and use the result:

```c
validation_value_t value = validation_int(qos);
validation_result_t result = validation_check(&QOS_RULE, &value);

if (!result.valid)
{
    // violated is null only when the rule or the value passed in was null
    ESP_LOGW(TAG, "qos rejected by constraint '%s'", result.violated ? result.violated->name : "input");
    return ESP_ERR_INVALID_ARG;
}
```

Notes:

- Constraints run in table order and checking stops at the first failure, which is the one reported.
- A rule with no constraints accepts any value.
- A constraint applied to the wrong value type, a null string or null parameters fails. Nothing invalid is reported as a pass.
- The result holds the name of the failed constraint, never the value, so it is safe to log even for credentials.
- Strings and parameters are referenced, not copied, so they must outlive the check.

## Custom constraints

Any function with the `validation_fn_t` signature can be a constraint. It must tolerate a null `value` and check `value->type` itself:

```c
static bool is_even(const validation_value_t* value, const void* params)
{
    (void) params;
    return value != nullptr && value->type == VALIDATION_VALUE_INT && value->int_value % 2 == 0;
}

static const validation_constraint_t CONSTRAINTS[] = {
    {.fn = validation_int_range, .params = &QOS_RANGE, .name = "range"},
    {.fn = is_even, .params = nullptr, .name = "even"},
};
```

## Tests

Tests live in `main-test/main/validation/validation_test.c` and cover every constraint with boundary values, null inputs and composition.
