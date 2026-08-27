package crypto.HoMAC;

import keyDerivation.KeyDerivationTree;

import java.math.BigInteger;

public class HoMACme {

    public static final BigInteger M = new BigInteger(String.valueOf(2^128));

//    public static final BigInteger PRIME = new BigInteger("7544262592915928822870038649403183600314880665118415032727772606791399578674416579132695615545637011719840237519003753082782297052903996459797254482660267");
//
    private KeyDerivationTree tree;
    private BigInteger macKey;

    public HoMACme(KeyDerivationTree tree, BigInteger macKey) {
        this.tree = tree;
        this.macKey = macKey.mod(M);
    }

    public int getNumFieldBits() {
        return 128;
    }

    public BigInteger getMACKey() {
        return this.macKey;
    }

    public BigInteger getMAC(BigInteger msg, long id) {
        return getMAC(msg, tree.getKey(id, getNumFieldBits()), tree.getKey(id + 1, getNumFieldBits()));
    }

    public BigInteger getMAC(BigInteger msg, BigInteger key1, BigInteger key2) {
        System.out.println("mac1: "+key1);
        System.out.println("mac2: "+key2);
        BigInteger key = key1.subtract(key2).mod(M);
        BigInteger km = macKey.multiply(msg).mod(M);
        return km.add(key).mod(M);
    }

    public boolean checkMAC(BigInteger msg, BigInteger mac, long msgID, long msgTo) {
        mac = mac.mod(M);
        return checkMAC(msg, mac, tree.getKey(msgID, getNumFieldBits()), tree.getKey(msgTo + 1, getNumFieldBits()));
    }

    public boolean checkMAC(BigInteger msg, BigInteger mac, BigInteger key1, BigInteger key2) {
        BigInteger key = mac.subtract(key1).add(key2).mod(M);
        BigInteger comp = msg.multiply(macKey).mod(M);
        return key.compareTo(comp) == 0;
    }

    public KeyDerivationTree getTree() {
        return this.tree;
    }

    public BigInteger aggregateMAC(BigInteger mac1, BigInteger mac2) {
        return mac1.add(mac2).mod(M);
    }

}
