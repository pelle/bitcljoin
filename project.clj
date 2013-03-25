(defproject bitcljoin "0.1.0"
  :description "BitCoin library for Clojure"
  :dependencies [
    [org.clojure/clojure "1.5.0"]
    [com.google/bitcoinj "0.7.3"]
    [com.h2database/h2 "1.3.170"]
    [lamina "0.5.0-beta10"]
    [bux "0.2.1"]]
  :jvm-opts ["-Xmx1g"]
  ;; :profiles { :dev {:dependencies [[ch.qos.logback/logback-classic "1.0.7"]]}}
  )
