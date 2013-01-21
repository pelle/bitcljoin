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
  ([] (peer-group (net) (block-chain)))
  ([chain] (peer-group (net) chain))
  ([network chain]
    (let [ group (com.google.bitcoin.core.PeerGroup. network chain)]
      (.setUserAgent group "BitCljoin" "0.7")
      (.addPeerDiscovery group (com.google.bitcoin.discovery.SeedPeers. (net)))
      group)))

(defn bc->store
  "returns the block store used by a block chain"
  [bc]
  (.getStore bc))

(defn sha256hash
  "Note this doesn't perform a hash. It just wraps a string within the Sha256Hash class"
  [hash-string]
  (com.google.bitcoin.core.Sha256Hash. hash-string))

(defn header
  "Actual block header from a stored block"
  [stored-block]
  (.getHeader stored-block))

(defn stored-blocks
  "Lazy sequence of stored blocks from the head and back"
  ([bs] (stored-blocks bs (.getChainHead bs)))
  ([bs sb] (if sb (cons sb (lazy-seq (stored-blocks bs (.getPrev sb bs)))))))

(defn block-headers
  "Lazy sequence of block headers from the head and back"
  [bs] (map header (stored-blocks bs)))

(defn peer-listener
  [pg]
  (.addEventListener pg
    (com.google.bitcoin.core.DownloadListener.)))

(defn block-chain-listener []
  (proxy
      [com.google.bitcoin.core.BlockChainListener][]
      (isTransactionRelevant [tx] true)))

(defn start
  "start downloading regular block chain"
  ([] (start (peer-group)))
  ([pg]
    (peer-listener pg)
    (future (do
      (.start pg)
      (.downloadBlockChain pg)))
    ))


(defn start-full
  "start downloading full block chain"
  [] (start (peer-group (full-block-chain))))