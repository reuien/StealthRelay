package keyManagement;

import prg.IPRG;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class KeyUtil {
    private static final byte[] macDefault = new byte[16];
    public static byte[] createInputForEncKeyDerivation(long id) {
        byte[] encDefault = new byte[16];
        for (int i = 0; i < encDefault.length / 2; i++) {
            encDefault[i] |= 0xFF;
        }
        for (int shift = 0; shift < 8; shift++) {
            encDefault[15 - shift] = (byte) ((id >> (shift * 8)) & 0xFF);
        }
        return encDefault;
    }

    public static byte[] createInputForMacKeyDerivation(long id) {
        byte[] macDefault = new byte[16];
        for (int i = macDefault.length / 2; i < macDefault.length; i++) {
            macDefault[i] |= 0xFF;
        }
        for (int shift = 0; shift < 8; shift++) {
            macDefault[7 - shift] = (byte) ((id >> (shift * 8)) & 0xFF);
        }
        return macDefault;
    }

    public static long deriveKey(IPRG prg, byte[] seed, boolean forEnc, long metaID) {
        if (forEnc)
            return deriveKey(prg, seed, createInputForEncKeyDerivation(metaID));
        return deriveKey(prg, seed, createInputForMacKeyDerivation(metaID));
    }

    public static long deriveKey(IPRG prg, byte[] seed, boolean forEnc) {
        if (forEnc)
            return deriveKey(prg, seed, createInputForEncKeyDerivation(0));
        return deriveKey(prg, seed, createInputForMacKeyDerivation(0));
    }

    public static long deriveKey(IPRG prg, byte[] seed, byte[] input) {
        byte[] key = prg.apply(seed, input);
        byte[] out = new byte[8];
        System.arraycopy(key, 0, out, 0, out.length);
        for (int i = out.length; i < key.length; i++) {
            out[i - out.length] ^= key[i];
        }
        return bytesToLong(out);
    }

    private static long bytesToLong(byte[] in) {
        ByteBuffer buffer = ByteBuffer.allocate(in.length);
        buffer.put(in);
        buffer.flip();
        return buffer.getLong();
    }

    public static byte[] deriveCombinedKey(IPRG prg, byte[] key1, byte[] key2) {
        if (key1.length != key2.length)
            throw new  RuntimeException("Cannot create a combined key from keys with different length");
        byte[] inputKey = new byte[key1.length];
        for (int iter = 0; iter < key1.length; iter++) {
            inputKey[iter] = (byte) (key1[iter] ^ key2[iter]);
        }
        return prg.apply(inputKey, createInputForEncKeyDerivation(0));
    }

    public static BigInteger deriveKeyBI(IPRG prg, byte[] seed, boolean forEnc, long metaID, int bits) {
        if (forEnc)
            return deriveKeyBI(prg, seed, createInputForEncKeyDerivation(metaID), bits);
        return deriveKeyBI(prg, seed, createInputForMacKeyDerivation(metaID), bits);
    }

    public static BigInteger deriveKeyBI(IPRG prg, byte[] seed, byte[] input, int bits) {
        byte[] key = prg.apply(seed, input);
        if ((key.length * 8) % bits != 0) {
            throw new IllegalArgumentException("Key cannot be created with that seed");
        }
        int numPartitions = key.length * 8 / bits;

        BigInteger curInt;
        byte[] partition = new byte[bits / 8];
        if (numPartitions < 2) {
            return new BigInteger(1, key);
        } else {
            System.arraycopy(key, 0, partition, 0, partition.length);
            curInt = new BigInteger(1, partition);
        }
        for (int i = 1; i < numPartitions; i++) {
            partition = new byte[bits / 8];
            System.arraycopy(key, i * partition.length, partition, 0, partition.length);
            curInt = curInt.xor(new BigInteger(1, partition));
        }
        return curInt;
    }

    public static BigInteger deriveKeyBI(IPRG prg, byte[] seed, int bits) {
        byte[] tmp = new byte[16];
        for (int i = 0; i < tmp.length; i++)
            tmp[i] = (byte) (tmp[i] | 0xFF);
        return deriveKeyBI(prg, seed, tmp, bits);
    }

    public static BigInteger generateMACKey(int numBits, BigInteger fieldPrime, SecureRandom random) {
        return new BigInteger(numBits, random).mod(fieldPrime);
    }

    public static byte[] generateKey(int numBytes, SecureRandom random) {
        byte[] key = new byte[numBytes];
        random.nextBytes(key);
        return key;
    }

}
