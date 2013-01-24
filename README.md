# bitcljoin

Start of a Clojure wrapper for BitCoinJ http://www.bitcoinj.org/

This is absolutely a work in progress. Including documentation (and my understanding of bitcoinj).

If you're interested please email me.

## Usage

Add the following to your project.clj

```clojure
[bitcljoin "0.1.0"]
```

Create a keypair:

```clojure
(create-keypair)
```

Create an address from a keypair:

```clojure
(to-address (create-keypair))
```

Create an address from a String:

```clojure
(to-address "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
```

Create or load a wallet from a file. If creating it creates a keypair by default:

```clojure
(wallet "./mywallet.wallet")
```

Get the keychain of a wallet:

```clojure
(keychain (wallet "./mywallet.wallet"))
```

Download block-chain and query it:

```clojure
(def bs (file-block-store))
(def bc (block-chain bs))
(def pg (peer-group bc))
(start pg) ;; start downloading

;; a sequence of blocks starting with the current head and going backwards
(take 10 (stored-blocks bs))
```

## Full or regular block chain

Traditionally BitcoinJ downloads a simpler quicker version of the Block Chain and only stores transactions related to your wallet. If you're using this to create and manage payments stick with the regular block chain as shown above.

If you're interested in bitcoin as a whole say for analytics. BitcoinJ now supports a new more complete blockchain download.

You can start this using start full which also returns the block chain.

```clojure
(def bc (start-full))
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
