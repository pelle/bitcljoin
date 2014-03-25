(ns bitcoin.channels
  (:use clojure.stacktrace)
  (:require [lamina.core :as lamina]
            [bitcoin.core :as btc]))

(defn txs->maps
  [ch]
  (lamina/filter* btc/tx->map ch))

(defn blocks->maps
  [ch]
  (lamina/filter* btc/block->map ch))

(defn broadcast-txs
  "Returns a of new transactions being broadcast (not yet in the blockchain)"
  ([pg]
   (let [ch (lamina/channel)]
     (btc/on-tx-broadcast pg (fn [_ tx] (lamina/enqueue ch tx)))
     ch))
  ([] (broadcast-txs @btc/current-pg)))

(defn txs-for
  "Creates a channel for transactions sent to a specific address

  In it's single parameter form it sets up a broadcast-tx-channel of unconfirmed transactions.

  for confirmed transactions pass the channel in as the first argument"

  ([channel address]
    (lamina/filter* #((btc/to-addresses %) address) channel))
  ([address]
   (txs-for (broadcast-txs) address)))

(defn wallet-txs
  "Creates a channel for transactions sent to a specific address

  In it's single parameter form it sets up a broadcast-tx-channel of unconfirmed transactions.

  for confirmed transactions pass the channel in as the first argument"

  ([channel wallet]
    (lamina/filter* #(btc/for-me? % wallet) channel))
  ([wallet]
   (wallet-txs (broadcast-txs) wallet)))


(defn confirmed-txs
  "Returns a of new transactions entering the blockchain"
  ([bc]
   (let [ch (lamina/channel)]
     (.addListener bc (proxy
                        [com.google.bitcoin.core.BlockChainListener][]
                        (isTransactionRelevant [tx] true)
                        (receiveFromBlock [tx _ _ _] (lamina/enqueue ch tx))
                        (notifyNewBestBlock [block] nil)))
     ch))
  ([] (confirmed-txs @btc/current-bc)))

(defn blocks
  "Returns a of new blocks entering the blockchain"
  ([bc]
   (let [ch (lamina/channel)]
     (.addListener bc (proxy
                        [com.google.bitcoin.core.BlockChainListener][]
                        (isTransactionRelevant [tx] true)
                        (receiveFromBlock [tx _ _ _] nil)
                        (notifyNewBestBlock [block] (lamina/enqueue ch block))))
     ch))
  ([] (blocks @btc/current-bc)))

(defn txs->maps
  [ch]
  (lamina/map* btc/tx->map ch))

(defn blocks->maps
  [ch]
  (lamina/map* btc/block->map ch))

(defn log-errors
  "Log all errors"
  [ch]
  (lamina/on-error ch #(print-cause-trace %)))

(defn prn-all
  "prn content of channel"
  [ch]
  (lamina/receive-all ch prn))
