(defproject doppler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.cli "0.3.1"]
                 [com.amazonaws/aws-java-sdk "1.6.3"]
                 [overtone/at-at "1.2.0"]
                 [clj-time "0.6.0"]]
  :main cmcarthur.doppler.core)
