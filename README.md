# ktor-metrics-extension

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

This ktor-metrics-extension is an extension library that exposes metrics collected
using [Kotor's MicroMeter](https://ktor.io/docs/micrometer-metrics.html) at the default Prometheus endpoint.

## Installation

Step 1. Add the [JitPack](https://jitpack.io/) repository to your build file

```kotlin
repositories {
    ...
    maven(url = "https://jitpack.io")
}
```

Step 2. Add the dependency

```kotlin
implementation("ch.qos.logback:logback-classic:$logbackVersion")
implementation("io.ktor:ktor-server-core:$ktorVersion")
implementation("io.ktor:ktor-metrics:$ktorVersion")
implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
implementation("io.prometheus:simpleclient_httpserver:$prometheusVersion")
implementation("com.github.otkmnb2783:ktor-metrics-extension:<this_library_current_version>")
```