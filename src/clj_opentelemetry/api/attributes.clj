(ns clj-opentelemetry.api.attributes
  (:require
   [clojure.set :as c.set])
  (:import
   (io.opentelemetry.api.common
    Attributes
    AttributeKey)))

(defn ^:private attribute-key
  [attr-name attr-type-kw]
  (case attr-type-kw
    :string        (AttributeKey/stringKey attr-name)
    :boolean       (AttributeKey/booleanKey attr-name)
    :long          (AttributeKey/longKey attr-name)
    :double        (AttributeKey/doubleKey attr-name)
    :string-array  (AttributeKey/stringArrayKey attr-name)
    :boolean-array (AttributeKey/booleanArrayKey attr-name)
    :long-array    (AttributeKey/longArrayKey attr-name)
    :double-array  (AttributeKey/doubleArrayKey attr-name)))

(defn attribute-registry
  [attribute-definitions]
  (reduce
   (fn [registry [attr-name attr-type-kw]]
     (assoc registry attr-name (attribute-key attr-name attr-type-kw)))
   {}
   attribute-definitions))

(def ^:private ^:dynamic *registered-attributes*
  {})

(defmacro with-registered-attributes
  [registry & body]
  `(binding [*registered-attributes* ~registry]
     ~@body))

(defn ^:private attribute-value-type-kw
  [v]
  (cond
    (string? v)                         :string
    (boolean? v)                        :boolean
    (float? v)                          :double
    (int? v)                            :long
    (and (coll? v) (every? string? v))  :string-array
    (and (coll? v) (every? boolean? v)) :boolean-array
    (and (coll? v) (every? float? v))   :double-array
    (and (coll? v) (every? int? v))     :long-array

    :else
    (throw (ex-info "Invalid attribute value" {:v v}))))

(defmulti ^:private coerce-attribute-value
  attribute-value-type-kw)

(defmethod ^:private coerce-attribute-value :string
  [v]
  (str v))

(defmethod ^:private coerce-attribute-value :boolean
  [v]
  v)

(defmethod ^:private coerce-attribute-value :double
  [v]
  (double v))

(defmethod ^:private coerce-attribute-value :long
  [v]
  (long v))

(defmethod ^:private coerce-attribute-value :string-array
  [v]
  (to-array (mapv str v)))

(defmethod ^:private coerce-attribute-value :boolean-array
  [v]
  (to-array v))

(defmethod ^:private coerce-attribute-value :double-array
  [v]
  (to-array (mapv double v)))

(defmethod ^:private coerce-attribute-value :long-array
  [v]
  (to-array (mapv long v)))

(defn map->attributes ^Attributes
  [m]
  (let [registered-attributes *registered-attributes*
        builder               (Attributes/builder)]

    (when (seq registered-attributes)
      (when-not (c.set/subset? (set (keys m))
                               (set (keys registered-attributes)))
        (throw (ex-info "Unregistered attribute"
                        {:map                   m
                         :registered-attributes registered-attributes}))))

    (loop [map-kvs (seq m)]
      (if-let [[k v] (first map-kvs)]
        (let [attr-key (get registered-attributes k
                            (->> (attribute-value-type-kw v)
                                 (attribute-key k)))]
          (.put builder attr-key (coerce-attribute-value v))
          (recur (rest map-kvs)))
        (.build builder)))))

(defn attributes->map
  [^Attributes attributes]
  (reduce
   (fn [m [k v]]
     (assoc m (str k) v))
   {}
   (.asMap attributes)))

(defn attributes->attribute-key-vals
  [^Attributes attributes]
  (map (juxt key val) (.asMap attributes)))
