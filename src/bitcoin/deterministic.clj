(ns bitcoin.deterministic
  (:require [bitcoin.core :as btc])
  (:import (com.google.bitcoin.crypto HDKeyDerivation HDUtils DeterministicKey)
           (java.security SecureRandom)
           (bitcoin.core Addressable)))


(defn secure-random
  []
  (SecureRandom.))

(defonce prng (atom (secure-random)))


(defn create-master-key
  "Create a deterministic master"
  ([]
   (create-master-key (.generateSeed @prng 128)))
  ([seed]
   (HDKeyDerivation/createMasterPrivateKey seed)))

(defn ->priv-bytes
  [dk]
  (.getPrivKeyBytes dk))

(defn ->pub-bytes
  [dk]
  (.getPubKey dk))

(defn ->chain-code
  "Get the chain key used for given deterministic key"
  [dk]
  (.getChainCode dk))

(defn
  ^{:deprecated "DeterministicKey is now a subclass of ECKey so this is no longer needed"}
  dk->kp [dk]
  dk)

(defn serialize-pub [dk]
  (.serializePubB58 dk))

(defn serialize-priv [dk]
  (.serializePrivB58 dk))

(defn recreate-master-key
  "Create the master key from the public key and chain code"
  [priv chain]
  (HDKeyDerivation/createMasterPrivKeyFromBytes priv chain))

(defn recreate-master-pub-key
  "Create the master key from the public key and chain code"
  [pub chain]
  (HDKeyDerivation/createMasterPubKeyFromBytes pub chain))

(defn derive-key
  "Derive a key for the derived key"
  [dk i]
  (HDKeyDerivation/deriveChildKey dk (.intValue i)))

(defn derive-from-path
  "Derive a key from a master key given a path"
  [mk path]
  (if (and path mk)
    (let [items (clojure.string/split path #"/")]
      (if (< (count items) 2)
        mk
        (reduce #(derive-key %1 (Long/parseLong %2)) mk (rest items))))))

(defn ->pub-only
  "Returns a derived key with only the public key part"
  [dk]
  (.getPubOnly dk))

(defn ->path
  "Returns the path from the master key to the given key"
  [dk]
  (.getPath dk))
