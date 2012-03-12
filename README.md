# bitcljoin

Start of a Clojure wrapper for BitCoinJ http://www.bitcoinj.org/

## Usage

Create a keypair:

    (create-keypair)

Create an address from a keypair:

    (to-address (create-keypair))

Create an address from a String:

    (to-address "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")

Create or load a wallet from a file. If creating it creates a keypair by default:

    (wallet "./mywallet.wallet")

Get the keychain of a wallet:

    (keychain (wallet "./mywallet.wallet"))

TODO:

* Blockchains
* PeerGroups
* Transactions
* Create transactions

## License

Copyright (C) 2012 Pelle Braendgaard http://stakeventures.com

Distributed under the Eclipse Public License, the same as Clojure.
