package dev.ohtake.ktor.metrics_ext

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import io.micrometer.prometheus.*
import io.prometheus.client.exporter.*
import io.prometheus.client.exporter.common.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

private const val DEFAULT_PROMETHEUS_METRICS_PATH = "/metrics"

class PrometheusExporterServer(
    private val registry: PrometheusMeterRegistry,
    private val runServer: Boolean,
    port: Int,
    private val endpoint: String?
) {

    private val logger = LoggerFactory.getLogger(PrometheusExporterServer::class.java.simpleName)
    private lateinit var internalServer: HTTPServer

    init {
        if (runServer && !::internalServer.isInitialized) {
            logger.info("Responding at http://0.0.0.0:$port")
            internalServer = HTTPServer(InetSocketAddress(port), registry.prometheusRegistry)
        }
    }

    class Configuration {
        lateinit var registry: PrometheusMeterRegistry
        var runServer: Boolean = false
        var port: Int = 9090
        var endpoint: String? = DEFAULT_PROMETHEUS_METRICS_PATH
    }

    private fun hasEndpoint(): Boolean = endpoint?.isNotEmpty() ?: false

    private suspend fun intercept(context: PipelineContext<Unit, ApplicationCall>) {
        if (hasEndpoint() && context.call.request.path() == endpoint) {
            context.call.respond(capture(registry.scrape()))
            context.finish()
            return
        }
        context.proceed()
    }

    companion object Feature : ApplicationFeature<Application, Configuration, PrometheusExporterServer> {

        override val key: AttributeKey<PrometheusExporterServer> = AttributeKey(PrometheusExporterServer::class.java.simpleName)

        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): PrometheusExporterServer {
            val config = Configuration().apply(configure)
            val feature = PrometheusExporterServer(
                registry = config.registry,
                runServer = config.runServer,
                port = config.port,
                endpoint = config.endpoint
            )
            pipeline.environment.monitor.subscribe(ApplicationStopping) {
                if (!feature.runServer || !feature::internalServer.isInitialized) return@subscribe
                feature.internalServer.stop()
            }
            pipeline.intercept(ApplicationCallPipeline.Call) {
                feature.intercept(this)
            }
            return feature
        }
    }
}

@OptIn(KtorExperimentalAPI::class)
fun capture(response: String) = object : OutgoingContent.WriteChannelContent() {

    override val contentType = ContentType.parse(TextFormat.CONTENT_TYPE_004)

    override suspend fun writeTo(channel: ByteWriteChannel) {
        channel.bufferedWriter(
            charset = contentType.charset() ?: Charsets.UTF_8
        ).use {
            it.write(response)
        }
    }
}
