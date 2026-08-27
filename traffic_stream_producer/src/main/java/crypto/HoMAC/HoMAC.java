package crypto.HoMAC;

import keyDerivation.KeyDerivationTree;
import keyManagement.KeyUtil;

import java.math.BigInteger;
import java.security.SecureRandom;

public class HoMAC {

    public static final BigInteger PRIME = new BigInteger("340282366920938463463374607431768211297");

    //    public static final BigInteger PRIME = new BigInteger("7544262592915928822870038649403183600314880665118415032727772606791399578674416579132695615545637011719840237519003753082782297052903996459797254482660267");
//
    private KeyDerivationTree tree;
    private BigInteger prime;
    private BigInteger macKey;
    private BigInteger macKeyInv;

    public HoMAC(KeyDerivationTree tree, BigInteger macKey, BigInteger prime) {
        this.tree = tree;
        this.prime = prime;
        this.macKey = macKey.mod(prime);
        this.macKeyInv = this.macKey.modInverse(prime);
    }

    public HoMAC(KeyDerivationTree tree, SecureRandom rand, BigInteger prime) {
        this(tree, new BigInteger(prime.bitLength(), rand), prime);
    }

    public HoMAC(KeyDerivationTree tree, BigInteger macKey) {
        this(tree, macKey, PRIME);
    }

    public HoMAC(KeyDerivationTree tree, SecureRandom random) {
        this(tree, random, PRIME);
    }

    public int getNumFieldBits() {
        return prime.bitLength();
    }

    public BigInteger getPrime() {
        return prime;
    }

    public BigInteger getMACKey() {
        return this.macKey;
    }

    public BigInteger getMAC(BigInteger msg, long id) {
        return getMAC(msg, tree.getKey(id, getNumFieldBits()), tree.getKey(id + 1, getNumFieldBits()));
    }

    public BigInteger getMAC(BigInteger msg, BigInteger key1, BigInteger key2) {
//        System.out.println("mac1: "+key1);
//        System.out.println("mac2: "+key2);
        BigInteger key = key1.subtract(key2).mod(prime);
        return key.subtract(msg).multiply(macKeyInv).mod(prime);
    }

    public boolean checkMACaggr(BigInteger msg, BigInteger mac, long metaID, long msgID, long msgTo) {
        mac = mac.mod(prime);
        return checkMAC(msg, mac,
                KeyUtil.deriveKeyBI(tree.getPRG(), tree.getSeed(msgID), false, metaID, getNumFieldBits()),
                KeyUtil.deriveKeyBI(tree.getPRG(), tree.getSeed(msgTo+1), false, metaID, getNumFieldBits()));
    }

    public boolean checkMAC(BigInteger msg, BigInteger mac, BigInteger key1, BigInteger key2) {
        BigInteger key = key1.subtract(key2).mod(prime);
        BigInteger comp = mac.multiply(macKey).mod(prime).add(msg).mod(prime);
//        System.out.println("checkMAC:  "+mac);
//        System.out.println(key);
//        System.out.println(comp);
        return key.compareTo(comp) == 0;
    }

    public KeyDerivationTree getTree() {
        return this.tree;
    }

    public BigInteger aggregateMAC(BigInteger mac1, BigInteger mac2) {
        return mac1.add(mac2).mod(prime);
    }
}
