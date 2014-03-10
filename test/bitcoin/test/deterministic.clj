(ns bitcoin.test.deterministic
  (:use bitcoin.deterministic
        clojure.test)
  (:require [bitcoin.core :as btc]))


(deftest creating-master-key
  (let [dk (create-master-key)
        cc (->chain-code dk)
        priv (->priv-bytes dk)
        pub (->pub-bytes dk)]
    (is dk)
    (is (= (serialize-pub (recreate-master-key priv cc))
           (serialize-pub dk)))
    (is (= (serialize-priv (recreate-master-key priv cc))
           (serialize-priv dk)))
    (is (= (serialize-pub (recreate-master-pub-key pub cc))
           (serialize-pub dk)))
    (is (= (btc/->address (recreate-master-pub-key pub cc))
           (btc/->address dk)))))
