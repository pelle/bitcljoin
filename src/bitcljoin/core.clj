(ns bitcljoin.core
  (:use [clojure.java.io :only [as-file]]))


(defn prodNet [] (com.google.bitcoin.core.NetworkParameters/prodNet))
(defn testNet [] (com.google.bitcoin.core.NetworkParameters/testNet))
(def network )

(defn net []
  (if (bound? (var network))
    network
    (do 
      (def network (prodNet))
      network)))

(defn create-keypair []
  (new com.google.bitcoin.core.ECKey))


(defn keychain [w]
  (.keychain w))


(defn create-wallet 
  ([] (create-wallet (net)))
  ([network] (let [ w (new com.google.bitcoin.core.Wallet network)
                    kp (create-keypair) ]
                    (.add (keychain w) kp)
                    w)))

(defn wallet [filename]
  (let [file (as-file filename)]
      (try 
        (com.google.bitcoin.core.Wallet/loadFromFile file)
        (catch java.io.FileNotFoundException e
          (let
            [w (create-wallet)]
            (.saveToFile w file)
            w)))))

(defprotocol Addressable
  (to-address [k] [k network]))

(extend-type com.google.bitcoin.core.ECKey Addressable
  (to-address 
    ([keypair] (to-address keypair (net)))
    ([keypair network] (.toAddress keypair network))))

(extend-type String Addressable
  (to-address 
    ([keypair] (to-address keypair (net)))
    ([keypair network] (new com.google.bitcoin.core.Address network keypair))))
