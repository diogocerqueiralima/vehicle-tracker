# Modes

The module has two modes that decide how positions are produced. The mode is set in the [configuration](configuration.md).

## Replay

The module does not generate positions. It publishes the locations listed in a file that we produce ourselves.

- The file contains a list of locations. Each location has the attributes in [Location Data](../device/data/location-data.md), such as `latitude`, `longitude`, `altitude`, `speed`, `heading` and `fix_type`.
- The module publishes one location per reporting interval, in the order of the file. The report `timestamp` is set by the module from the simulated clock, as described in [Publishing](publishing.md).
- When the last location is published, the module stops, or starts again from the first location, depending on the configuration.

The locations have no relation to each other, so this mode is not for tracking a vehicle. It is for publishing known values, for example to test one specific location, the edges of the valid ranges, or a large number of reports. Because the file is fixed, every run publishes the same data.

See [replay.json](examples/replay.json) for an example. It has a normal location, a moving one, a weak 2D fix and a `NO_FIX` location. The `timestamp` is not in the file, because the module sets it.

## Route

The module simulates a vehicle moving once along a complete route.

- The route is an ordered list of waypoints, loaded from a file. See [Configuration](configuration.md).
- Every waypoint except the first has a `speed_knots`, which is the speed of the vehicle on the segment from the previous waypoint to that one. For example, the speed on the segment from waypoint A to waypoint B is defined on B.
- The simulated vehicle starts at the first waypoint and moves along each segment at the speed of that segment.
- Each position is calculated from the distance travelled since the previous report. It lies on the line between two waypoints.
- `heading` is the bearing of the current segment, and `speed` is the speed of the current segment.
- The vehicle is stationary at the start and at the end of the route.
- When it reaches the last waypoint, the route ends. It can then stop, or start again from the first waypoint, depending on the configuration.

The timestamp advances by the reporting interval on every report, so a route does not have to run in real time. See [Publishing](publishing.md).

### Route events

A real device does not report at every interval during a whole route. To simulate this, the route file can contain optional events. Each event is tied to a waypoint and has a duration in seconds. A route without events reports at every interval from start to end.

| Event | What the module does |
|---|---|
| `stop` | The vehicle stays at the waypoint for the duration. The device is stationary, so it reports at the `report_interval_seconds` of the event (3600 seconds, or 1 hour, by default) instead of the moving interval. |
| `no_fix` | The vehicle keeps moving and reports are still published, but with `fix_type` set to `NO_FIX`. This simulates a tunnel or an underground car park. |
| `silence` | The vehicle keeps moving but nothing is published for the duration. This simulates a network outage. |

During a `silence` event the simulated `timestamp` keeps advancing and the reports are lost. The backend sees a gap between the `timestamp` of the last report before the event and the first one after it.

The events are for testing how the backend handles missing data, gaps between timestamps and devices that go offline. See [Configuration](configuration.md).

An event starts after the module publishes the report of its waypoint, so that report is always sent. The `waypoint` is the index of the waypoint in the list, starting at 0. An event on the last waypoint has no effect, because the route ends there, so it should be placed on an earlier waypoint.

#### Event fields

Each entry in the `events` list of the route file has these fields:

| Field | Description | Used by | Required | Default |
|---|---|---|---|---|
| `type` | `stop`, `no_fix` or `silence`. | all | yes | none |
| `waypoint` | Index of the waypoint after which the event starts, starting at 0. It cannot be the last waypoint. | all | yes | none |
| `duration_seconds` | How long the event lasts, in seconds. It must not be negative. | all | yes | none |
| `report_interval_seconds` | Interval between two reports while the vehicle is stopped. | `stop` | no | `3600` |

Only one event can start at the same waypoint.

### Example

See [route.json](examples/route.json) for an example. It is a short route in Lisbon with five waypoints:

- The vehicle goes from waypoint 0 to waypoint 1 at 18 knots, then to waypoint 2 at 25 knots, to waypoint 3 at 30 knots and to waypoint 4 at 20 knots.
- After the report of waypoint 1 it has no fix for 60 seconds, like in a tunnel.
- After the report of waypoint 2 it stops for 600 seconds and reports every 300 seconds.
- After the report of waypoint 3 nothing is published for 120 seconds, while the vehicle keeps moving to waypoint 4.
