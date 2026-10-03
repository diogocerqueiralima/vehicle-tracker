# File Characteristic

Some characteristics hold a **file**-typed value: one that can be larger than a single ATT packet, so it is transferred in chunks instead of a single read/write. Every chunk, in either direction, is framed as:

```
[total_len: uint32 LE][offset: uint32 LE][payload: total_len - offset bytes, capped to what fits the negotiated MTU]
```

- `total_len` is the size in bytes of the complete value being transferred.
- `offset` is where this chunk's `payload` starts within that value.
- `payload` is that chunk's slice of the value. It is always prefixed with the 8-byte `[total_len][offset]` header above, never sent on its own.

**Reading** a file characteristic returns one chunk per read, starting at `offset` 0. The device keeps the value cached for the duration of the sequence and serves the next `offset` on each subsequent read from the same connection. Reading it again from a fresh connection, or before a previous sequence finished, restarts the sequence from `offset` 0. The client keeps reading until `offset + len(payload)` reaches `total_len`.

**Writing** a file characteristic works the same way in reverse: the first chunk (`offset` 0) declares the `total_len` of the value being sent and starts a new write, discarding any write already in progress on that characteristic. Every following chunk must continue that same write - same connection, same `total_len`, and `offset` equal to the number of bytes already received - or the device rejects it and the client must restart from `offset` 0. The device only validates and persists the value once the last chunk arrives (`offset + len(payload) == total_len`)