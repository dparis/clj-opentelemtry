(ns clj-opentelemetry.api.span
  (:require
   [clj-opentelemetry.api.attributes :as co.a.attributes]
   [cuerdas.core :as str])
  (:import
   (io.opentelemetry.api.common
    Attributes)
   (io.opentelemetry.api.trace
    Span
    SpanKind
    SpanBuilder
    SpanContext
    StatusCode
    Tracer)
   (io.opentelemetry.context
    Context)
   (java.time
    Instant)))


(defn from-current-context ^Span
  []
  (Span/current))

(defn from-context ^Span
  [^Context context]
  (Span/fromContextOrNull context))

(defn span-context ^SpanContext
  [^Span span]
  (.getSpanContext span))

(defn is-recording?
  [^Span span]
  (.isRecording span))

(defn ^:private coerce-into-attributes ^Attributes
  [x]
  (cond
    (instance? Attributes x) x
    (map? x)                 (co.a.attributes/map->attributes x)))

(defn record-exception ^Span
  ([span ^Throwable ex]
   (.recordException span ex))
  ([span ^Throwable ex attributes]
   (.recordException span ex (coerce-into-attributes attributes))))

(defmulti ^:private coerce-span-time->instant ^Instant
  (fn [x]
    (cond
      (and (map? x) (contains? x :timestamp)) :timestamp-unit-map
      (and (map? x) (contains? x :instant))   :instant-map
      (instance? Instant x)                   :instant)))

(defmethod ^:private coerce-span-time->instant :timestamp-unit-map
  [_]
  ;; TODO:  Unclear how to build Instant from timestamp+unit,
  ;;        implement later maybe
  (throw
   (IllegalArgumentException. "Timestamp + unit map not currently supported")))

(defmethod ^:private coerce-span-time->instant :instant-map
  [x]
  (:instant x))

(defmethod ^:private coerce-span-time->instant :instant
  [x]
  x)

(defn add-event ^Span
  [^Span span span-name & {:keys [attributes time]}]
  (cond
    (and (nil? attributes) (nil? time))
    (.addEvent span span-name)

    (nil? time)
    (.addEvent span span-name (coerce-into-attributes attributes))

    (nil? attributes)
    (.addEvent span span-name (coerce-span-time->instant time))

    :else
    (.addEvent span
               span-name
               (coerce-into-attributes attributes)
               (coerce-span-time->instant time))))

(defn set-attributes! ^Span
  [^Span span attributes-or-map]
  (let [attributes (coerce-into-attributes attributes-or-map)
        key-vals   (co.a.attributes/attributes->attribute-key-vals attributes)]
    (loop [remaining-key-vals key-vals
           updated-span       span]
      (if-let [[k v] (first remaining-key-vals)]
        (recur (rest remaining-key-vals)
               (.setAttribute updated-span k v))
        updated-span))))

(defn set-attribute! ^Span
  [^Span span attr-key attr-value]
  (set-attributes span {attr-key attr-value}))

(defn ^:private enum->kw
  [enum]
  (-> (str enum)
      (str/kebab)
      (keyword)))

(def status-codes
  (reduce
   (fn [m enum]
     (assoc m (enum->kw enum) enum))
   {}
   (StatusCode/values)))

(defn set-status ^Span
  ([^Span span status-code]
   (.setStatus span (get status-codes status-code)))
  ([^Span span status-code description]
   (.setStatus span (get status-codes status-code) description)))

(defn store-in-context ^Context
  [^Span span ^Context context]
  (.storeInContext span context))

(defn update-name ^Span
  [^Span span span-name]
  (.updateName span span-name))

(defn ^:private add-link-to-span!
  [^SpanBuilder builder link]
  (if (instance? Context link)
    (.addLink builder link)
    (.addLink (first link) (coerce-into-attributes (second link)))))

(defn set-attributes-on-builder! ^Span
  [^SpanBuilder span-builder attributes-or-map]
  (let [attributes (coerce-into-attributes attributes-or-map)
        key-vals   (co.a.attributes/attributes->attribute-key-vals attributes)]
    (loop [remaining-key-vals key-vals
           updated-builder span-builder]
      (if-let [[k v] (first remaining-key-vals)]
        (recur (rest remaining-key-vals)
               (.setAttribute updated-builder k v))
        updated-builder))))

(def span-kinds
  (reduce
   (fn [m enum]
     (assoc m (enum->kw enum) enum))
   {}
   (SpanKind/values)))

(defn start-span ^Span
  [^Tracer tracer span-name & {:keys [attributes kind links parent start-time]}]
  (let [builder ^SpanBuilder (.spanBuilder tracer span-name)]
    ;; Handle attributes if any
    (when attributes
      (set-attributes-on-builder! builder attributes))

    ;; Handle kind if specified
    (when kind
      (.setSpanKind builder (get span-kinds kind)))

    ;; Handle links if any
    (when links
      (run! (partial add-link-to-span! builder) links))

    ;; Handle parent if specified
    (if parent
      (.setParent builder ^Context parent)
      (.setNoParent builder))

    ;; Handle start-time if specified
    (when start-time
      (.setStartTimestamp builder start-time))

    (.startSpan builder)))

(defn end
  ([^Span span]
   (.end span))
  ([^Span span time]
   (.end span (coerce-span-time->instant time))))
