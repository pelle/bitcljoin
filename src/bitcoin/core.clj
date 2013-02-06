(ns bitcoin.core
  (:use [clojure.java.io :only [as-file]]
        [bux.currencies :only [BTC]])
  (:require [lamina.core :as lamina]))


(defn prodNet []  (com.google.bitcoin.core.NetworkParameters/prodNet))
(defn testNet []  (com.google.bitcoin.core.NetworkParameters/testNet))
(def network)

(defn net []
  (if (bound? (var network))
    network
    (do
      (def network (prodNet))
      network)))

(def current-bc (atom nil))
(def current-pg (atom nil))

(defn create-keypair []
  (com.google.bitcoin.core.ECKey. ))

(defn ->private
  "encodes private keys in the form used by the Bitcoin dumpprivkey command"
  [kp]
  (str (.getPrivateKeyEncoded kp network)))

(defn ->kp
  "decodes private keys in the form used by the Bitcoin dumpprivkey command"
  [s]
  (.getKey (com.google.bitcoin.core.DumpedPrivateKey. network s)))

(defn keychain [w]
  (.keychain w))


(defn create-wallet
  ([] (create-wallet (net)))
  ([network] (com.google.bitcoin.core.Wallet. network)))

(defn add-keypair [wallet kp]
  (.add (keychain wallet) kp)
  wallet)

(defn open-wallet [filename]
  (let [file (as-file filename)]
      (try
        (com.google.bitcoin.core.Wallet/loadFromFile file)
        (catch java.io.FileNotFoundException e
          (let
              [w (create-wallet)
               kp (create-keypair) ]
            (add-keypair w kp)
            (.saveToFile w file)
            w)))))

(defn register-wallet
  "Register the wallet with the blockchain and peergroup"
  ([wallet] (register-wallet wallet @current-bc @current-pg))
  ([wallet bc pg]
     (.addWallet bc wallet)
     (.addWallet pg wallet)
     wallet))

(defn kp->wallet
  "Create and register a wallet for the keypair"
  ([kp]
     (-> (create-wallet)
         (add-keypair kp)
         (register-wallet))))


(defprotocol Addressable
  (->address [k] [k network]))

(extend-type com.google.bitcoin.core.ECKey Addressable
  (->address
    ([keypair] (->address keypair (net)))
    ([keypair network] (.toAddress keypair network))))

(extend-type String Addressable
  (->address
    ([keypair] (->address keypair (net)))
    ([keypair network] (new com.google.bitcoin.core.Address network keypair))))


(defn memory-block-store
  ([] (memory-block-store (net)))
  ([network] (com.google.bitcoin.store.MemoryBlockStore. network)))

(defn file-block-store
  ([] (file-block-store "bitcljoin"))
  ([name] (file-block-store (net) name))
  ([network name] (com.google.bitcoin.store.BoundedOverheadBlockStore. network (java.io.File. (str name ".blockchain")))))

(defn h2-full-block-store
  ([] (h2-full-block-store "bitcljoin"))
  ([name] (h2-full-block-store (net) name))
  ([network name] (com.google.bitcoin.store.H2FullPrunedBlockStore. network name 300000)))

(defn block-chain
  ([] (block-chain (file-block-store)))
  ([block-store] (com.google.bitcoin.core.BlockChain. (net) block-store)))

(defn full-block-chain
  ([] (full-block-chain (h2-full-block-store)))
  ([block-store] (com.google.bitcoin.core.FullPrunedBlockChain. (net) block-store)))

(defn peer-group
  ([] (peer-group (net) @current-bc))
  ([chain] (peer-group (net) chain))
  ([network chain]
    (let [ group (com.google.bitcoin.core.PeerGroup. network chain)]
      (.setUserAgent group "BitCljoin" "0.7")
      (.addPeerDiscovery group (com.google.bitcoin.discovery.SeedPeers. (net)))
      group)))

(defn init
  "setup a block chain and peer-group using default settings. This should work well for e-commerce and general purpose payments"
  []
  (reset! current-bc (block-chain))
  (reset! current-pg (peer-group @current-bc)))

(defn init-full
  "setup a full block chain and peer-group using default settings. Use this if you're more interested in analyzing the block chain."
  []
  (reset! current-bc (full-block-chain))
  (reset! current-pg (peer-group @current-bc)))

(defn bc->store
  "returns the block store used by a block chain"
  [bc]
  (.getBlockStore bc))

(defn sha256hash
  "Note this doesn't perform a hash. It just wraps a string within the Sha256Hash class"
  [hash-string]
  (com.google.bitcoin.core.Sha256Hash. hash-string))

(defn genesis-hash []
  (sha256hash "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"))

(defn header
  "Actual block header from a stored block"
  [stored-block]
  (.getHeader stored-block))

(defn stored-blocks
  "Lazy sequence of stored blocks from the head and back"
  ([] (stored-blocks (bc->store @current-bc)))
  ([bs] (stored-blocks bs (.getChainHead bs)))
  ([bs sb] (if sb (cons sb (lazy-seq (stored-blocks bs (.getPrev sb bs)))))))

(defn block-headers
  "Lazy sequence of block headers from the head and back"
  [bs] (map header (stored-blocks bs)))

(defn coin-base? [tx]
  (.isCoinBase tx))

(defn tx-inputs [tx]
  (.getInputs tx))

(defn sender
  "Returns address of first input of transaction"
  [tx]
  (.getFromaddress (first (tx-inputs tx))))

(defn balance [w]
  (.getBalance w))

(defn amount-received [tx w]
  (.getValueSentToMe tx w))

(defn regular-inputs
  "non coinbase inputs"
  [tx]
  (filter #(not (coin-base? %)) (tx-inputs tx)))

(defn input-addresses
  "Get the from addresses for a transaction"
  [tx]
  (map #(.getFromAddress %) (regular-inputs tx)))

(defn tx-outputs [tx]
  (.getOutputs tx))

(defn output-from-addresses
  "Get the from addresses for a transaction"
  [tx]
  (map #(.getFromAddress %) (regular-inputs tx)))

(defn output-to-addresses
  "Get the from addresses for a transaction"
  [tx]
  (map #(.getToAddress %) (regular-inputs tx)))


(def tx-channel
  (lamina/channel))

(def block-channel
  (lamina/channel))

(def coin-base-channel
  (lamina/filter* coin-base? tx-channel))


(defn peer-listener
  [pg]
  (.addEventListener pg
    (com.google.bitcoin.core.DownloadListener.)))

(defn block-chain-listener []
  (proxy
      [com.google.bitcoin.core.BlockChainListener][]
      (isTransactionRelevant [tx] true)
      (notifyNewBestBlock [block] (lamina/enqueue block-channel block))
      (receiveFromBlock [tx block block-type] (lamina/enqueue tx-channel tx))
      ))

(defn on-coins-received
  "calls f with the transaction prev balance and new balance"
  [wallet f]
  (.addEventListener wallet
                     (proxy
                         [com.google.bitcoin.core.AbstractWalletEventListener][]
                       (onCoinsReceived [w tx prev-balance new-balance]
                         (if (= wallet w)
                           (f tx prev-balance new-balance))))))

(defn send-coins
  "Send a value to a single recipient. To should be a big integer"
  [wallet to amount]
  (.sendCoins wallet @current-pg (->address to) amount))


(defn ping-service
  "receive coins on address and return them immediately"
  ([kp]
     (ping-service (kp->wallet kp) kp))
  ([wallet kp]
     (println "starting ping service on address: " (->address kp) )
     (on-coins-received wallet
                        (fn [tx prev new]
                          (prn tx)
                          (let [from   (sender tx)
                                amount (amount-received tx wallet)]
                            (let [t2 (send-coins wallet from amount)]
                              (print "Sent to: " (->address from))
                              (prn t2)))))))

(defn listen-to-block-chain []
  (.addListener @current-bc (block-chain-listener)))

(defn download-block-chain
  "download block chain"
  ( [] (download-block-chain @current-pg))
  ( [pg]
      (future (.downloadBlockChain pg))))

(defn start
  "start downloading regular block chain"
  ([]
     (if (nil? @current-pg) (init))
     (start (peer-group)))
  ([pg]
     (.start pg)))


(defn start-full
  "start downloading full block chain"
  []
  (if (nil? @current-pg) (init-full))
  (start @current-pg))

