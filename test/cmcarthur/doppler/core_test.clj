(ns cmcarthur.doppler.core-test
  (:use midje.sweet)
  (:require [cmcarthur.doppler.core :refer :all]
            [cmcarthur.doppler.cloudwatch :as cloudwatch]
            [overtone.at-at :as scheduler]))

(facts "generate-metric-results"
  (prerequisites
      (cloudwatch/get-metric 1) => [{:average 1 :sum 1 :time nil}]
      (cloudwatch/get-metric 2) => [{:average 2 :sum 2 :time nil}]
      (cloudwatch/push-metric anything anything) => nil)
  (let [default-config {:mapper (comp :average first)
                        :reducer +
                        :metrics [1 2]
                        :output nil}]
    (fact "uses map-reduce"
      (generate-metric-results default-config) => 3)
    (fact "maintains ordering"
      (generate-metric-results (assoc default-config :reducer -)) => -1)
    (fact "can handle more than two metrics"
      (generate-metric-results (assoc default-config :reducer -
                                                     :metrics [1 2 2])) => -3)
    (fact "works with custom reducers"
      (generate-metric-results (assoc default-config :reducer (fn [x y] (+ x y))
                                                     :metrics [1 2 2])) => 5)))

(facts "run-scheduler"
  (fact "runs for the given duration and interval"
    (run-scheduler 1 2.5) => nil?
    (provided (generate-metrics-from-config anything) => nil :times 3))
  (fact "fractional intervals"
    (run-scheduler 0.1 0.55) => nil?
    (provided (generate-metrics-from-config anything) => nil :times 6)))
