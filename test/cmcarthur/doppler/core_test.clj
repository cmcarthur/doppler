(ns cmcarthur.doppler.core-test
  (:use midje.sweet)
  (:require [cmcarthur.doppler.core :refer :all]
            [cmcarthur.doppler.cloudwatch :as cloudwatch]))

(facts "generate-metric-results"
  (prerequisites
    (cloudwatch/get-metric 1) => [{:average 1 :sum 1 :time nil}]
    (cloudwatch/get-metric 2) => [{:average 2 :sum 2 :time nil}]
    (cloudwatch/push-metric anything anything) => nil)
  (fact "uses map-reduce"
    (generate-metric-results {:mapper (comp :average first)
                              :reducer +
                              :metrics [1 2]
                              :output nil}) => 3)
  (fact "maintains ordering"
    (generate-metric-results {:mapper (comp :average first)
                              :reducer -
                              :metrics [1 2]
                              :output nil}) => -1)
  (fact "can handle more than two metrics"
    (generate-metric-results {:mapper (comp :average first)
                              :reducer -
                              :metrics [1 2 2]
                              :output nil}) => -3)
  (fact "works with custom reducers"
    (generate-metric-results {:mapper (comp :average first)
                              :reducer (fn [x y] (+ x y))
                              :metrics [1 2 2]
                              :output nil}) => 5))
