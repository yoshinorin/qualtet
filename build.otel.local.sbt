// OpenTelemetry configuration for development environment only
// These javaOptions are applied only during 'sbt run' and not in production
// For production deployment, set these as JVM arguments when running the JAR:
// java -Dotel.service.name=$QUALTET_OTEL_SERVICE_NAME -jar qualtet.jar
javaOptions += s"-Dotel.java.global-autoconfigure.enabled=${sys.env.getOrElse("QUALTET_OTEL_JAVA_GLOBAL_AUTOCONFIGURE_ENABLED", "true")}"
javaOptions += s"-Dotel.service.name=${sys.env.getOrElse("QUALTET_OTEL_SERVICE_NAME", "qualtet")}"
javaOptions += s"-Dotel.service.namespace=${sys.env.getOrElse("QUALTET_OTEL_SERVICE_NAMESPACE", "qualtet")}"
javaOptions += s"-Dotel.exporter.otlp.endpoint=${sys.env.getOrElse("QUALTET_OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317")}"
javaOptions += s"-Dotel.propagators=${sys.env.getOrElse("QUALTET_OTEL_PROPAGATORS", "tracecontext")}"
