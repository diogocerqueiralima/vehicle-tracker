# Errors

## Invalid configuration

The configuration is validated when the module starts. If a value is missing or invalid, for example a locations file or a route file that does not exist, a location with a latitude outside the range of -90 to 90, or a route event with an unknown type, a waypoint index outside the route or on the last waypoint, two events on the same waypoint, a negative duration, or a waypoint without `speed_knots` or with a speed that is not positive, the module logs which setting is wrong and exits with a non-zero code. No report is published.

## Broker errors

- If the module cannot connect to the broker at startup, it logs the error and exits with a non-zero code.
- If the connection is lost while running, the module logs the error and tries to reconnect. Reports are not queued during the outage.
- If publishing a report fails, the module logs the device id and the `timestamp` of that report and continues with the next one.

## Route events

A `silence` event is intentional, so it is not an error. The module logs the start and the end of the event, with the device id, as information.

## Logs

Errors are written to the standard output. Each message includes the device id when it relates to a device. See [Publishing](publishing.md) for the `timestamp`.
