(ns cmcarthur.doppler.cloudwatch
  (:require [clojure.tools.logging :refer [info error]]
            [clj-time.core :as time]
            [clj-time.coerce :as timec]))

(defn- create-dimension
  "Wrapper for the dimension class provided by the AWS Cloudwatch SDK."
  ([{:keys [name value]}]
   (create-dimension name value))
  ([name value]
   (-> (com.amazonaws.services.cloudwatch.model.Dimension.)
       (.withName name)
       (.withValue value))))

(defn- create-push-metric-request
  "Wrapper for the PutMetricDataRequest class provided by the AWS Cloudwatch SDK."
  [namespace metric-data]
  (-> (com.amazonaws.services.cloudwatch.model.PutMetricDataRequest.)
      (.withNamespace namespace)
      (.withMetricData metric-data)))

(defn- create-metric-datum
  "Wrapper for the MetricDatum class provided by the AWS Cloudwatch SDK."
  [dimensions name unit value]
  (-> (com.amazonaws.services.cloudwatch.model.MetricDatum.)
      (.withDimensions (map create-dimension dimensions))
      (.withMetricName name)
      (.withUnit unit)
      (.withValue value)))

(defn push-metric
  "Given a namespace and a metric data collection, this function will push
  the metric data to Cloudwatch."
  ([{:keys [namespace name dimensions unit]} value]
    (push-metric namespace name dimensions unit value))
  ([namespace name dimensions unit value]
   (do
     (info "Pushing metric" name "to namespace" namespace)
     (let [metric-data [(create-metric-datum dimensions name unit value)]
           push-request (create-push-metric-request namespace metric-data)
           client (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.)]
       (.putMetricData client push-request)))))

(defn- create-get-metric-request
  "Wrapper for the GetMetricStatisticsRequest class provided by the AWS Cloudwatch SDK."
  ([{:keys [namespace name period dimensions start-time end-time]}]
   (create-get-metric-request namespace name period dimensions start-time end-time))
  ([namespace name period dimensions start-time end-time]
   (-> (com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest.)
       (.withNamespace namespace)
       (.withMetricName name)
       (.withPeriod (int period))
       (.withStatistics ["Sum" "Average"])
       (.withDimensions (map create-dimension dimensions))
       (.withStartTime (timec/to-date start-time))
       (.withEndTime (timec/to-date end-time))
       (.withUnit "Count"))))

(defn- parse-datapoint
  [datapoint]
  {:time (timec/from-date (.getTimestamp datapoint))
   :average (.getAverage datapoint)
   :sum (.getSum datapoint)})

(defn get-metric
  [config]
  (let [request (create-get-metric-request config)
        client (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.)
        result (.getMetricStatistics client request)]
    (info "Retrieving metric" (:name config) "from namespace" (:namespace config))
    (map parse-datapoint (.getDatapoints result))))
