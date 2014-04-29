# bitcljoin

Start of a Clojure wrapper for BitCoinJ http://www.bitcoinj.org/

This is absolutely a work in progress. Including documentation (and my understanding of bitcoinj). The API is under a lot of flux. Hope to stabilize it somewhat by 1.0.

If you're interested please email me.

## Installing bitcoinj

You first need a copy of bitcoinj in your local maven repository. Unfortunately google doesn't offer it on a public maven service.

So until then you need to clone their repo and install it locally using these instructions from their [Using Maven](http://code.google.com/p/bitcoinj/wiki/UsingMaven) page:

```bash
git clone https://code.google.com/p/bitcoinj/ bitcoinj
cd bitcoinj
git reset --hard 410d4547a7dd20745f637313ed54d04d08d28687    # Force yourself to the 0.11 release
mvn install
```

## Usage

Add the following to your project.clj

```clojure
[bitcljoin "0.4.3"]
```

Use library:

```clojure
(use 'bitcoin.core)
```

Create a keypair:

```clojure
(create-keypair)
```

Create an address from a keypair:

```clojure
(->address (create-keypair))
```

Create an address from a String:

```clojure
(->address "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
```

Load a private key using the BitCoin privkey dumpformat:

```clojure
(->kp "5KQ3SHTDHfU6XhBp7sSCbUoMpiZQKfKc7jVjAb6rHiegq1m2VWq")
```

Export a keypair to BitCoin privkey dump format:

```clojure
(->private kp)
```

Create or load a wallet from a file. If creating it creates a keypair by default:

```clojure
(open-wallet "./mywallet.wallet")
```

Get the keychain of a wallet:

```clojure
(keychain (wallet "./mywallet.wallet"))
```

Create an in memory wallet for a single keypair:

```clojure
(kp->wallet kp)
```

Send coins:

```clojure
(send-coins wallet "16mJ5mGvj3xdmQhspPxFLp8ScYjirDoKxN" 1000) ;; Use nano coins TODO use BigDec
```

Register when a payment is received:

```clojure
(on-coins-received wallet (fn [tx prev-balance new-balance] (prn tx)))
```

Download block-chain and query it:

```clojure
(start)
(download-block-chain)

;; a sequence of blocks starting with the current head and going backwards
(take 10 (stored-blocks))
```

## Full or regular block chain

Traditionally BitcoinJ downloads a simpler quicker version of the Block Chain and only stores transactions related to your wallet. If you're using this to create and manage payments stick with the regular block chain as shown above.

If you're interested in bitcoin as a whole say for analytics. BitcoinJ now supports a new more complete blockchain download.

You can start this using start full which also returns the block chain.

```clojure
(start-full)
```

## Channels

[Lamina](https://github.com/ztellman/lamina) is a great new library for managing asyncronous communications. One of the key abstractions
is the [Channel](https://github.com/ztellman/lamina/wiki/Channels-new) which can be thought of as a pub/sub system that looks a bit like a sequence.

This is all experimental but I think this is a much better way of doing analytics on the BitCoin economy.

If you were using this pre 0.3 please note that this has changed a bit. In particular we have a new namespace <tt>bitcoin.channels</tt>. Channels are also not created automatically.


```clojure
(require '[bitcoin.core :as btc])
(use 'bitcoin.channels)
(require '[lamina.core :as l])

(btc/start-full) ; Starts the full block chain downloader which gives a more complete view of the bitcoin economy

(def txs (broadcast-txs)) ;; A channel of all new transactions. Returns Transaction objects straight from BitcoinJ
(def clj-txs (txs->maps txs)) ;; A Channel of all transactions but returned as clojure maps
(def dice (txs-for "1dice8EMZmqKvrGE4Qc9bUFf9PX3xaYDp"))
(def new-blocks (blocks))


(lamina.core/take* 1 txs) ;; get the first transaction off the channel
(lamina.core/take* 1 new-blocks) ;; get the first block off the channel

; Lamina allows us to create a new channel using map* which works exactly like clojure's regular map except on channels
(def block-time (lamina.core/map* #(.getTime %) blocks)) ; Create a new channel containing all the timestamps of the blocks

;; It is good practice to combine filter* and map* to process the data in the way you want

;; e.g. to find all the addresses who receive the coinbase
(->> (broadcast-txs)
      (lamina.core/filter* btc/coin-base?) ;; Filter channel to only contain coinbase transactions
      (lamina.core/map* #(btc/output->address (first (.getOutputs %))))) ;; Find the address of the first output
```

## Deterministic Keys

As of 0.4 we now have a clojure wrapper around BitcoinJ's Deterministic Key support. Deterministic Keys are based on [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki) and allows you to recreate a key hierarchy based on a single secret and public key pair.

```clojure
(use 'bitcoin.deterministic)

(def mk (create-master-key)) ;; Creates the master key pair.

;; To recreate the master key with the private key (For spending purposes) you need to save the private key bytes as well as the chain code.

(def priv (->priv-bytes mk)) ;; Save this securely
(def cc (->chain-code mk)) ;; Save this. It is required to create both private and public keys

(recreate-master-key priv cc) ;; Recreate the actual master key

;; You can create a public key version of the master key. This allows you to create addresses in the hierarchy but not spend outputs for it.

(def pub (->pub-bytes mk))


(recreate-master-pub-key pub cc) ;; Recreate the actual master key but only the public key


;; Derive a key

(def child (derive-key mk 2))

;; Show the path of the hierarchy
(->path child)
# "M/2"

;; Add a grand child
(def grand-child (derive-key child 1))

;; Show the path of the hierarchy
(->path grand-child)
# "M/2/1"

;; Find the key for a particular path
(derive-from-path mk "M/1/3")

;; Return the EC Keypair for use in signing of a Deterministic key
(dk->kp child)

;; Return bitcoin address for deterministic key

(require '[bitcoin.core :as btc])

(btc/->address child)

```


## TODO:

* Transactions
* Create transactions


## License

Copyright (C) 2012 Pelle Braendgaard http://stakeventures.com

Distributed under the Eclipse Public License, the same as Clojure.
