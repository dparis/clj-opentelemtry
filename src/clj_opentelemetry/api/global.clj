(ns clj-opentelemetry.api.global
  (:import
   (io.opentelemetry.api
    OpenTelemetry
    GlobalOpenTelemetry)
   (io.opentelemetry.api.trace
    Tracer
    TracerProvider)
   (io.opentelemetry.context.propagation
    ContextPropagators)))

(defn get-instance ^OpenTelemetry
  []
  (GlobalOpenTelemetry/get))

(defn set-instance!
  [^OpenTelemetry open-telemetry]
  (GlobalOpenTelemetry/set open-telemetry))

(defn reset-for-test!
  []
  (GlobalOpenTelemetry/resetForTest))
