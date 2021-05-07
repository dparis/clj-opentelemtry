(ns clj-opentelemetry.api
  (:import
   (io.opentelemetry.api
    OpenTelemetry)
   (io.opentelemetry.api.trace
    Tracer
    TracerProvider)
   (io.opentelemetry.context.propagation
    ContextPropagators)))

(defn get-propagators ^ContextPropagators
  [^OpenTelemetry ot]
  (.getPropagators ot))

(defn get-tracer ^Tracer
  ([^OpenTelemetry ot instrumentation-name]
   (.getTracer ot instrumentation-name))
  ([^OpenTelemetry ot instrumentation-name instrumentation-version]
   (.getTracer ot instrumentation-name instrumentation-version)))

(defn get-tracer-provider ^TracerProvider
  [^OpenTelemetry ot]
  (.getTracerProvider ot))
