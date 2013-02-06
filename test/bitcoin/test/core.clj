(ns bitcoin.test.core
  (:use [bitcoin.core])
  (:use [clojure.test])
  (:use [clojure.java.io :only [as-file delete-file]]))


(deftest should-have-default-net
  (is (= (.getId (net)) "org.bitcoin.production") "Should default to prodNet"))

(deftest should-create-key 
  (let [kp (create-keypair)]
    (is (instance? com.google.bitcoin.core.ECKey kp) "Should create key")))

(deftest should-decode-key
  (let [kp (decode-private "5KQ3SHTDHfU6XhBp7sSCbUoMpiZQKfKc7jVjAb6rHiegq1m2VWq")]
    (is (instance? com.google.bitcoin.core.ECKey kp) "Should create key")
    (is (= "5KQ3SHTDHfU6XhBp7sSCbUoMpiZQKfKc7jVjAb6rHiegq1m2VWq" (encode-private kp)))))

(deftest should-create-address
  (is (instance? com.google.bitcoin.core.Address (to-address (create-keypair) (prodNet))) "Should create address for keypair and network")
  (is (instance? com.google.bitcoin.core.Address (to-address (create-keypair))) "Should create address for keypair default prod network")
  (is (instance? com.google.bitcoin.core.Address (to-address "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL" (prodNet))) "Should create address from string and network")
  (is (instance? com.google.bitcoin.core.Address (to-address "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")) "Should create address from string"))

(deftest should-create-wallet
  (let [wal (create-wallet)]
    (is (instance? com.google.bitcoin.core.Wallet wal))
    (let [kc (keychain wal)
          kp (first kc)]
      (is (= 1 (count kc)))
      (is (instance? com.google.bitcoin.core.ECKey kp)))))
  

(deftest should-create-and-load-wallet
  (let [ filename "./test.wallet"
         _ (delete-file (as-file filename) true)
         wal (wallet filename)]
    (is (instance? com.google.bitcoin.core.Wallet wal))
    (let [kc (keychain wal)
          kp (first kc)]
      (is (= 1 (count kc)))
      (is (instance? com.google.bitcoin.core.ECKey kp)
      (is (= (str kp) (str (first (keychain (wallet filename))))))))))
