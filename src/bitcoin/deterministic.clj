(ns bitcoin.deterministic
  (:require [bitcoin.core :as btc])
  (:import (com.google.bitcoin.crypto HDKeyDerivation HDUtils DeterministicKey)
           (com.google.bitcoin.core Base58)
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
  (.getPubKeyBytes dk))

(defn ->chain-code
  "Get the chain key used for given deterministic key"
  [dk]
  (.getChainCode dk))

(defn dk->kp [dk]
  (.toECKey dk))

(extend-type com.google.bitcoin.crypto.DeterministicKey btc/Addressable
  (btc/->address
    ([dk] (btc/->address dk (btc/net)))
    ([dk network] (.toAddress (dk->kp dk) network))))

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

(defn ->pub-only
  "Returns a derived key with only the public key part"
  [dk]
  (.getPubOnly dk))

(defn ->path
  [dk]
  (.getPath dk))
