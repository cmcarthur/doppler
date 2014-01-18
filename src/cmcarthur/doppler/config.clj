(ns cmcarthur.doppler.config
  (:require [clj-time.core :as time]))

(defn get-config
  []

  [{:metrics [
    {:namespace "AWS/SQS"
     :name "NumberOfMessagesReceived"
     :period 300
     :dimensions [{:name "QueueName" :value "my-queue"}]
     :start-time (time/minus (time/now) (time/hours 1))
     :end-time (time/now)}

    {:namespace "AWS/SQS"
     :name "NumberOfMessagesSent"
     :period 300
     :dimensions [{:name "QueueName" :value "my-queue"}]
     :start-time (time/minus (time/now) (time/hours 1))
     :end-time (time/now)}]

   :mapper (fn [i] (-> i last :sum))
   :reducer -

   :output
     {:namespace "SQS"
      :name "ReceivedMinusSent"
      :unit "Count"
      :dimensions [{:name "QueueName" :value "my-queue"}]}}])