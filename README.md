# bitcljoin

Start of a Clojure wrapper for BitCoinJ http://www.bitcoinj.org/

This is absolutely a work in progress. Including documentation (and my understanding of bitcoinj). The API is under a lot of flux. Hope to stabilize it somewhat by 0.1.

If you're interested please email me.

## Installing bitcoinj

You first need a copy of bitcoinj in your local maven repository. Unfortunately google doesn't offer it on a public maven service.

So until then you need to clone their repo and install it locally using these instructions from their [Using Maven](http://code.google.com/p/bitcoinj/wiki/UsingMaven) page:

```bash
git clone https://code.google.com/p/bitcoinj/ bitcoinj
cd bitcoinj
git reset --hard a9bd8631b904     # Force yourself to the 0.7 release
mvn install
```

## Usage

Add the following to your project.clj

```clojure
[bitcljoin "0.1.0-SNAPSHOT"]
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

## Lamina Channels

[Lamina](https://github.com/ztellman/lamina is a great new library for managing asyncronous communications. One of the key abstractions
is the [Channel](https://github.com/ztellman/lamina/wiki/Channels-new) which can be thought of as a pub/sub system that looks a bit like a sequence.

This is all experimental but I think this is a much better way of doing analytics on the BitCoin economy.

We now create a lamina channel for both blocks and transactions if you start the full block chain downloader:

```clojure
(start-full) ; Starts the full block chain downloader which gives a more complete view of the bitcoin economy

(lamina.core/take* 1 tx-channel) ;; get the first transaction off the channel
(lamina.core/take* 1 block-channel) ;; get the first block off the channel

; Lamina allows us to create a new channel using map* which works exactly like clojure's regular map except on channels
(def block-time (lamina.core/map* #(.getTime %) block-channel)) ; Create a new channel containing all the timestamps of the blocks
```



TODO:

* Transactions
* Create transactions


## License

Copyright (C) 2012 Pelle Braendgaard http://stakeventures.com

Distributed under the Eclipse Public License, the same as Clojure.
