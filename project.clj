(defproject bitcljoin "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
    [org.clojure/clojure "1.4.0"]
    [com.google/bitcoinj "0.7-SNAPSHOT"]
    [com.h2database/h2 "1.3.170"]
    [lamina "0.5.0-beta8"]
    [bux "0.2.1"]
    ; [com.google.guava/guava "11.0.2"]
    ]
  :jvm-opts ["-Xmx1g"]
  :repositories {"bitcoinj-release" "http://nexus.bitcoinj.org/content/repositories/releases"
                  "bitcoinj-snapshot" "http://nexus.bitcoinj.org/content/repositories/snapshots"})