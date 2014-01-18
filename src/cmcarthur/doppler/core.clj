(ns cmcarthur.doppler.core
  (:require [cmcarthur.doppler.cloudwatch :as cloudwatch]
            [cmcarthur.doppler.config :as config]
            [clj-time.core :as time]
            [clj-time.periodic :as periodic]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :refer [info error]]
            [overtone.at-at :as scheduler])
  (:gen-class))

(def scheduler-pool (scheduler/mk-pool))

(defn generate-metric-results
  [config]
  (let [mapper (:mapper config)
        reducer (:reducer config)
        metrics (:metrics config)
        output (:output config)
        value-to-push (reduce reducer (map (comp mapper cloudwatch/get-metric) metrics))]
    (cloudwatch/push-metric output value-to-push)
    value-to-push))

(defn generate-metrics-from-config
  [config]
  (dorun (map generate-metric-results config)))

(defn run-scheduler
  []
  (scheduler/every (* 5 60 1000)
                   (fn []
                     (info "Pushing composites to Cloudwatch")
                     (dorun (generate-metrics-from-config (config/get-config))))
                   scheduler-pool))

(defn -main
  [& args]
  (do
    (run-scheduler)
    (info "Started Scheduler.")))
