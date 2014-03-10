(ns bitcoin.test.deterministic
  (:use bitcoin.deterministic
        clojure.test)
  (:require [bitcoin.core :as btc]))


(defn same-key?
  ([a b]
   (same-key? a b serialize-pub))
  ([a b t]
   (is (= (t a) (t b)))))

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


(deftest hierarchies
  (let [mk (create-master-key)]
    (same-key? (derive-from-path mk "M") mk)
    (same-key? (derive-from-path mk "M/1") (derive-key mk 1))
    (same-key? (derive-from-path mk "M/2") (derive-key mk 2))
    (same-key? (derive-from-path mk "M/1/3") (derive-key (derive-key mk 1) 3))

    (let [pk (->pub-only mk)]
      (same-key? (derive-from-path mk "M") (derive-from-path pk "M"))
      (same-key? (derive-from-path mk "M/1") (derive-from-path pk "M/1"))
      (same-key? (derive-from-path mk "M/2") (derive-from-path pk "M/2"))
      (same-key? (derive-from-path mk "M/1/3") (derive-from-path pk "M/1/3")))))
