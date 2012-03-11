(ns bitcljoin.core)


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

(defn create-wallet 
  ([] (create-wallet (net)))
  ([network] (new com.google.bitcoin.core.Wallet network)))

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
