# doppler

Doppler is a Clojure service for creating composite Cloudwatch metrics and alarms.

Cloudwatch is a great tool for performing ad-hoc analysis. Its alarms are crippled by single metric reporting only. At [RJMetrics](http://rjmetrics.com), we have run into a few scenarios where composite Cloudwatch metrics come in handy. For example:

 - When an SQS queue is backed up, but the consumers are not de-queueing messages;
 - Verifying that the number of messages through various services match up exactly;
 - Or, creating aggregate traffic metrics across horizontally scaled clusters.

## Usage

Build an uberjar with `lein uberjar`. Right now you need to provide AWS credentials in the local environment, like:

```bash
$ AWS_ACCESS_KEY_ID=<access key id> AWS_SECRET_ACCESS_KEY=<secret access key> java -jar doppler.jar
```

## Config File

You'll need to customize the config file in order to generate your own metrics. Here's an example config:

```clojure
;; list of metric inputs
{:metrics [{:namespace "AWS/SQS"
            :name "NumberOfMessagesReceived"
            :period 300
            :dimensions [{:name "QueueName" :value "my-queue"}]
            :start-time (time/minus (time/now) (time/hours 1))
            :end-time (time/now)}

            ;; :namespace is the Amazon namespace for the input metric, usually
            ;; begins with "AWS/".
           {:namespace "AWS/SQS"
            ;; :name is the name of the metric in Cloudwatch.
            :name "NumberOfMessagesSent"
            ;; :period is the period, in seconds, for which you'd like to pull
            ;; this metric. must be a multiple of 60.
            :period 300
            ;; :dimensions apply filters to the metrics that you pull.
            :dimensions [{:name "QueueName" :value "my-queue"}]
            ;; :start-time is a joda-time object representing the query start time.
            :start-time (time/minus (time/now) (time/hours 1))
            ;; :end-time is a joda-time object representing the query end time.
            :end-time (time/now)}]
            
 ;; :mapper is a Clojure function that is applied to the initially pulled Cloudwatch
 ;; data. The inputs to this function look like:
 ;;
 ;;    {:time <time>
 ;;     :average <average value>
 ;;     :sum <sum value>}
 ;;
 ;; This mapper will take the sum value from the most recent data point.
 :mapper (fn [i] (-> i last :sum))
 
 ;; :reducer is a Clojure function that is applied to the outputs of :mapper. This
 ;; could be a multi-arity Clojure function, or a 2 input function called by `reduce`.
 :reducer -

 ;; :output defines metadata for the output metric. These values are equivalent to the
 ;; values provided in the :metrics collection above.
 :output {:namespace "SQS"
          :name "ReceivedMinusSent"
          :unit "Count"
          :dimensions [{:name "QueueName" :value "my-queue"}]}
```

## License

http://www.apache.org/licenses/LICENSE-2.0.txt


