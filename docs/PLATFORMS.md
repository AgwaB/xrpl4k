# Supported Platforms

| Platform | Target | Engine |
|----------|--------|--------|
| **JVM 11+** | `jvm` | OkHttp (HTTP), OkHttp (WebSocket) |
| JS / Node.js | `js` | Ktor JS engine |
| iOS | `iosArm64`, `iosSimulatorArm64` | Ktor Darwin engine |
| macOS | `macosArm64` | Ktor Darwin engine |
| Linux | `linuxX64` | Ktor CIO engine |

> JVM is the primary target with full test coverage. Other platforms compile and link but have limited integration testing.
