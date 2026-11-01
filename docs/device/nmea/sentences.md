# NMEA Sentences

The GNSS module outputs position data as NMEA 0183 sentences. Each sentence is an ASCII line with the format:

```
$<Talker><Type>,<field>,<field>,...*<checksum>
```

- **Talker**: two letters identifying the satellite system: `GP` (GPS), `GL` (GLONASS), `GA` (Galileo), `GB` (BeiDou) or `GN` (combined).
- **Type**: three letters identifying the sentence, such as `GGA` or `RMC`.
- **Checksum**: two hex digits, the XOR of all characters between `$` and `*`.

Empty fields mean the value is not available. Latitude uses the format `ddmm.mmmm` and longitude uses `dddmm.mmmm`.

## GGA

Fix data: position, fix quality, satellites used, HDOP and altitude.

```
$GPGGA,123519.00,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*69
```

| # | Field | Example | Description |
|---|---|---|---|
| 1 | UTC time | `123519.00` | `hhmmss.ss` |
| 2 | Latitude | `4807.038` | `ddmm.mmmm` |
| 3 | N/S | `N` | `N` or `S` |
| 4 | Longitude | `01131.000` | `dddmm.mmmm` |
| 5 | E/W | `E` | `E` or `W` |
| 6 | Fix quality | `1` | see [Fix quality](#fix-quality) |
| 7 | Satellites used | `08` | Number of satellites used in the fix |
| 8 | HDOP | `0.9` | Horizontal dilution of precision, see [Dilution of precision](#dilution-of-precision) |
| 9 | Altitude | `545.4` | Above mean sea level |
| 10 | Altitude unit | `M` | Meters |
| 11 | Geoid separation | `46.9` | Geoid height above the WGS84 ellipsoid |
| 12 | Geoid unit | `M` | Meters |
| 13 | DGPS age | | Seconds since the last DGPS update |
| 14 | DGPS station ID | | Reference station ID |

## RMC

Recommended minimum data: time, date, position, speed and course.

```
$GPRMC,123519.00,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,A*29
```

| # | Field | Example | Description |
|---|---|---|---|
| 1 | UTC time | `123519.00` | `hhmmss.ss` |
| 2 | Status | `A` | `A` valid, `V` warning (no valid fix) |
| 3 | Latitude | `4807.038` | `ddmm.mmmm` |
| 4 | N/S | `N` | `N` or `S` |
| 5 | Longitude | `01131.000` | `dddmm.mmmm` |
| 6 | E/W | `E` | `E` or `W` |
| 7 | Speed over ground | `022.4` | Knots |
| 8 | Course over ground | `084.4` | Degrees true |
| 9 | Date | `230394` | `ddmmyy` |
| 10 | Magnetic variation | `003.1` | Degrees |
| 11 | Variation direction | `W` | `E` or `W` |
| 12 | Mode | `A` | see [Mode indicator](#mode-indicator) |

## GSA

DOP and active satellites: fix type and dilution of precision values.

```
$GPGSA,A,3,04,05,09,12,24,,,,,,,,2.5,1.3,2.1*39
```

| # | Field | Example | Description |
|---|---|---|---|
| 1 | Selection mode | `A` | see [Selection mode](#selection-mode) |
| 2 | Fix type | `3` | see [Fix type](#fix-type) |
| 3-14 | Satellite IDs | `04,05,09,12,24` | Up to 12 satellites used in the fix |
| 15 | PDOP | `2.5` | Position dilution of precision |
| 16 | HDOP | `1.3` | Horizontal dilution of precision |
| 17 | VDOP | `2.1` | Vertical dilution of precision |

## GSV

Satellites in view. One sentence carries up to 4 satellites, so several sentences are sent per cycle for each constellation.

```
$GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74
```

| # | Field | Example | Description |
|---|---|---|---|
| 1 | Total sentences | `3` | Number of GSV sentences in this cycle |
| 2 | Sentence number | `1` | Index of this sentence (1 to total) |
| 3 | Satellites in view | `11` | Total satellites in view |
| 4 | Satellite ID | `03` | PRN of the satellite |
| 5 | Elevation | `03` | Degrees, 0 to 90 |
| 6 | Azimuth | `111` | Degrees true, 0 to 359 |
| 7 | SNR | `00` | Signal strength in dB-Hz, empty when not tracked |
| 8+ | Repeat 4 to 7 | | Up to 3 more satellites in the same sentence |

## VTG

Course and speed over ground.

```
$GPVTG,084.4,T,081.3,M,022.4,N,041.5,K,A*25
```

| # | Field | Example | Description |
|---|---|---|---|
| 1 | Course (true) | `084.4` | Degrees |
| 2 | True indicator | `T` | Fixed `T` |
| 3 | Course (magnetic) | `081.3` | Degrees |
| 4 | Magnetic indicator | `M` | Fixed `M` |
| 5 | Speed | `022.4` | Knots |
| 6 | Knots indicator | `N` | Fixed `N` |
| 7 | Speed | `041.5` | Kilometers per hour |
| 8 | Km/h indicator | `K` | Fixed `K` |
| 9 | Mode | `A` | see [Mode indicator](#mode-indicator) |

## GLL

Geographic position: latitude, longitude and time.

```
$GPGLL,4807.038,N,01131.000,E,123519.00,A,A*66
```

| # | Field | Example | Description |
|---|---|---|---|
| 1 | Latitude | `4807.038` | `ddmm.mmmm` |
| 2 | N/S | `N` | `N` or `S` |
| 3 | Longitude | `01131.000` | `dddmm.mmmm` |
| 4 | E/W | `E` | `E` or `W` |
| 5 | UTC time | `123519.00` | `hhmmss.ss` |
| 6 | Status | `A` | `A` valid, `V` invalid |
| 7 | Mode | `A` | see [Mode indicator](#mode-indicator) |

## Mode indicator

Positioning mode letter used by RMC, VTG and GLL.

| Value | Meaning | Description |
|---|---|---|
| `N` | Invalid | No usable fix |
| `A` | Autonomous | Basic GNSS fix |
| `D` | Differential | Differential corrections applied |
| `P` | Precise | Precise service |
| `R` | RTK fixed | Fixed ambiguity |
| `F` | RTK float | Float ambiguity |
| `E` | Estimated | Dead reckoning |
| `M` | Manual input | Manually entered |
| `S` | Simulator | Test mode |

## Fix

How the receiver reports the state and quality of its position fix.

### Fix quality

Reported in field 6 of [GGA](#gga).

| Value | Meaning | Description |
|---|---|---|
| `0` | Invalid | No usable fix |
| `1` | GNSS fix | Autonomous, basic GNSS fix |
| `2` | Differential | Differential corrections applied |
| `3` | PPS | Precise service |
| `4` | RTK fixed | Fixed ambiguity |
| `5` | RTK float | Float ambiguity |
| `6` | Estimated | Dead reckoning |
| `7` | Manual input | Manually entered |
| `8` | Simulator | Test mode |

### Selection mode

Reported in field 1 of [GSA](#gsa) (Mode 1). Parse it, and the fix type below, before the satellite ID list.

| Value | Meaning | Description |
|---|---|---|
| `M` | Manual | Receiver forced to operate in 2D or 3D |
| `A` | Automatic | Receiver switches between 2D and 3D on its own |

### Fix type

Reported in field 2 of [GSA](#gsa) (Mode 2).

| Value | Meaning | Description |
|---|---|---|
| `1` | No fix | Not enough satellites |
| `2` | 2D | Latitude and longitude only (3 satellites) |
| `3` | 3D | Latitude, longitude and altitude (4 or more satellites) |

### Fix status

Reported in [RMC](#rmc) (field 2) and [GLL](#gll) (field 6).

| Value | Meaning | Description |
|---|---|---|
| `A` | Valid | Position can be used |
| `V` | Void | Receiver warning, position must be discarded |

## Dilution of precision

Dilution of precision (DOP) says how much the satellite geometry magnifies the receiver's measurement error. Well spread satellites give a low DOP and a more accurate fix. Satellites bunched together or on one side of the sky give a high DOP. It is a unitless factor, not an error in meters.

| Value | Name | Describes |
|---|---|---|
| HDOP | Horizontal DOP | Accuracy of latitude and longitude |
| VDOP | Vertical DOP | Accuracy of altitude, usually larger than HDOP |
| PDOP | Position DOP | Accuracy of the full 3D position, roughly `PDOP² = HDOP² + VDOP²` |

Reported in [GSA](#gsa) (fields 15 to 17). [GGA](#gga) also carries HDOP (field 8).

| DOP | Quality |
|---|---|
| < 1 | Ideal |
| 1 - 2 | Excellent |
| 2 - 5 | Good |
| 5 - 10 | Moderate |
| 10 - 20 | Fair |
| > 20 | Poor, discard the fix |

## Sentences not covered

- **ZDA**: UTC date and time with local time zone offset.
- **GNS**: Fix data for multi-constellation receivers (replaces GGA).
- **GBS**: Satellite fault detection (RAIM).
- **GST**: Pseudorange error statistics.
- **GRS**: Range residuals.
- **DTM**: Datum reference.
- **TXT**: Free text messages, such as antenna status.
- **HDT**, **HDG**, **THS**: True or magnetic heading, which need a compass.
- **VBW**, **VLW**, **VHW**: Water speed and distance, which are marine sentences.
- **RMB**, **BOD**, **WPL**, **RTE**, **XTE**, **APB**: Waypoint, route and cross-track navigation.
- **Proprietary sentences**: Manufacturer specific sentences starting with `$P`, such as `PUBX`.
