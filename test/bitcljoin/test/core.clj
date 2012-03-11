(ns bitcljoin.test.core
  (:use [bitcljoin.core])
  (:use [clojure.test]))


(deftest should-have-default-net
  (is (= (.getId (net)) "org.bitcoin.production") "Should default to prodNet"))

(deftest should-create-key 
  (is (instance? com.google.bitcoin.core.ECKey (create-keypair)) "Should create key"))

(deftest should-create-address
  (is (instance? com.google.bitcoin.core.Address (to-address (create-keypair) (prodNet))) "Should create address for keypair and network")
  (is (instance? com.google.bitcoin.core.Address (to-address (create-keypair))) "Should create address for keypair default prod network")
  (is (instance? com.google.bitcoin.core.Address (to-address "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL" (prodNet))) "Should create address from string and network")
  (is (instance? com.google.bitcoin.core.Address (to-address "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")) "Should create address from string"))

(deftest should-create-wallet
  (is (instance? com.google.bitcoin.core.Wallet (create-wallet))))
