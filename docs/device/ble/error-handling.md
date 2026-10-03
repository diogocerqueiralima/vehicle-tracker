# Error Handling

The device answers failed requests with the following error codes. Each outcome from the configuration layer maps to one of them, and the mapping is the same for every service, so a new service reuses it instead of defining its own. Most are defined by the Bluetooth specification; the application-specific ones are taken from the `0x80`-`0x9F` range the specification reserves for the higher-layer profile.

| Code | Name | Source | Meaning |
|---|---|---|---|
| `0x0D` | Invalid Attribute Value Length | Bluetooth specification | A write is malformed, including a [file characteristic](characteristics/file-characteristic.md) chunk shorter than the 8-byte header or an empty scalar characteristic write. |
| `0x0E` | Unlikely Error | Bluetooth specification | A read failed for a reason other than the setting being unconfigured, such as a storage failure. Because this code differs from `0x90`, a client can tell an unconfigured setting apart from a misbehaving device. |
| `0x13` | Value Not Allowed | Bluetooth specification | A submitted value is invalid, including a [file characteristic](characteristics/file-characteristic.md) chunk sequence, declared size, or complete value, or a scalar setting that fails validation. |
| `0x90` | Not Configured | Application | The characteristic holds no value on the device yet, and has no [default](../config/overview.md#default-values) to fall back to. This is normal on an unconfigured device, and the client should offer to write a value instead of reporting a failure. |

> **Note**: `0x90` sits in the upper half of the reserved range because Android's Bluetooth stack reuses `0x80`-`0x8F` for errors of its own, which a client could not tell apart from an error sent by the device.
