# HytaleOne Query Plugin

A lightweight UDP query protocol plugin for Hytale servers. Query your server status without the overhead of HTTP — just like Minecraft's query protocol, but designed specifically for Hytale.

## Why UDP Query?

Other query plugins use HTTP, which means:
- Extra port to expose and manage
- HTTP overhead for simple status checks
- Additional dependencies

**HytaleOne Query** takes a different approach:
- **Same port** as game server (5520) — no extra ports needed
- **UDP protocol** — minimal overhead, instant responses
- **Binary format** — compact and efficient
- **Zero dependencies** — works standalone
- **Magic byte detection** — query packets are cleanly separated from game traffic

## Features

- **Basic Query**: Server name, MOTD, player count, max players, port, version, protocol info
- **Full Query**: Everything above + player list (names & UUIDs) + plugin list
- **Server List Registration**: Automatically register with [hytale.one](https://hytale.one/) on startup

## Server List

Register your server on **[hytale.one](https://hytale.one/)** — the community server list for Hytale. When enabled, this plugin automatically announces your server on startup, making it discoverable to players looking for servers to join.

## Installation

1. Download the latest release from [Releases](../../releases)
2. Place `hytaleone-query-x.x.x.jar` in your server's `plugins` directory
3. Restart the server

## Configuration

Configuration is stored in your server's config under the `HytaleOneQuery` module:

```json
{
  "HytaleOneQuery": {
    "RegisterOnStartup": true
  }
}
```

| Option | Default | Description |
|--------|---------|-------------|
| `RegisterOnStartup` | `true` | Register with hytale.one server list on startup |

## Protocol Specification

### Request Format

```
Offset  Size  Field
0       8     Magic: "HYQUERY\0" (ASCII)
8       1     Type: 0x00 = Basic, 0x01 = Full
```

### Response Format

All integers are little-endian. Strings are length-prefixed (2-byte LE length + UTF-8 bytes).

**Basic Response (Type 0x00):**
```
Offset  Size     Field
0       8        Magic: "HYREPLY\0" (ASCII)
8       1        Type: 0x00
9       2+N      Server Name (length-prefixed string)
...     2+N      MOTD (length-prefixed string)
...     4        Current Players (int32 LE)
...     4        Max Players (int32 LE)
...     2        Host Port (uint16 LE)
...     2+N      Version (length-prefixed string)
...     4        Protocol Version (int32 LE)
...     2+N      Protocol Hash (length-prefixed string)
```

**Full Response (Type 0x01):** Basic fields + Player List + Plugin List
```
[Basic fields...]
...     4        Player Count (int32 LE)
        [for each player:]
...     2+N      Player Name (length-prefixed string)
...     16       Player UUID (8 bytes MSB + 8 bytes LSB)

...     4        Plugin Count (int32 LE)
        [for each plugin:]
...     2+N      Plugin Identifier (length-prefixed string, e.g. "HytaleOne:Query")
...     2+N      Plugin Version (length-prefixed string)
...     1        Enabled (boolean)
```

## Client Libraries

Query servers from your application using these client libraries:

| Language | Package | Status |
|----------|---------|--------|
| Node.js / TypeScript | [@hytaleone/query](https://github.com/HytaleOne/query-js) | ✅ Available |
| Python | - | 🚧 TODO |
| Go | - | 🚧 TODO |
| Rust | - | 🚧 TODO |
| C# / .NET | - | 🚧 TODO |
| Java | - | 🚧 TODO |

Want to contribute a client library? PRs welcome!

## Building from Source

```bash
# Requires HytaleServer.jar in libs/ directory
mvn clean package
```

Output: `target/hytaleone-query-x.x.x.jar`

## License

MIT License - see [LICENSE](LICENSE) for details.

---

**[hytale.one](https://hytale.one/)** — Discover Hytale Servers
