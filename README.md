# ktor-metrics-extension

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Jitpack](https://jitpack.io/v/otkmnb2783/ktor-metrics-extension.svg)](https://jitpack.io/#otkmnb2783/ktor-metrics-extension)

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

## Usage

```kotlin

fun main(args: Array<String>): Unit = EngineMain.main(args)

@OptIn(KtorExperimentalLocationsAPI::class)
@Suppress("unused", "UNUSED_PARAMETER")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(Locations) {
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = prometheus
        distributionStatisticConfig = DistributionStatisticConfig.DEFAULT
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics()
        )
    }

    install(PrometheusExporterServer) {
        registry = prometheus
        runServer = true
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
    }
}
```

```bash
curl -XGET http://localhost:8080/metrics
curl -XGET http://localhost:9090/metrics
```
