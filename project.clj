(defproject clj-opentelemetry "0.1.0-SNAPSHOT"
  :description "A Clojure library designed to wrap OpenTelemetry Java API"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :managed-dependencies [[funcool/cuerdas "2021.05.02-0"]
                         [io.grpc/grpc-api "1.36.1"]
                         [io.grpc/grpc-core "1.36.1"]
                         [io.grpc/grpc-netty-shaded "1.36.1"]
                         [io.grpc/grpc-protobuf "1.36.1"]
                         [io.opentelemetry/opentelemetry-api "1.1.0"]
                         [io.opentelemetry/opentelemetry-sdk "1.1.0"]
                         [io.opentelemetry/opentelemetry-exporter-jaeger "1.1.0"]
                         [io.opentelemetry/opentelemetry-exporter-zipkin "1.1.0"]]

  :dependencies [[org.clojure/clojure "1.10.1"]

                 [funcool/cuerdas]
                 [io.opentelemetry/opentelemetry-api]
                 [io.opentelemetry/opentelemetry-sdk]
                 [io.opentelemetry/opentelemetry-exporter-jaeger]
                 [io.opentelemetry/opentelemetry-exporter-zipkin]]

  :repl-options {:init-ns clj-opentelemetry.core})
