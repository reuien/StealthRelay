package crypto;

import crypto.DigestCrypto.DigestEncryption;
import crypto.HoMAC.HoMAC;
import keyDerivation.KeyDerivationTree;
import keyManagement.CachedKeys;
import keyManagement.KeyUtil;

import java.math.BigInteger;

public class StreamCrypto {

    private DigestEncryption enc;
    private HoMAC homac;
    private KeyDerivationTree kdt;

    public StreamCrypto(KeyDerivationTree kdt, BigInteger macKey) {
        this.enc = new DigestEncryption(kdt);
        this.homac = new HoMAC(kdt, macKey);
        this.kdt = kdt;
    }

    private Ciphertext encryptWithMAC(long msg, byte[] seedForID1, byte[] seedForID2, long metadataID) {
        long digestDataCipher = enc.encrypt(msg,
                KeyUtil.deriveKey(kdt.getPRG(), seedForID1, true, metadataID),
                KeyUtil.deriveKey(kdt.getPRG(), seedForID2, true, metadataID));
        BigInteger hoMACTag = homac.getMAC(BigInteger.valueOf(msg),
                KeyUtil.deriveKeyBI(kdt.getPRG(), seedForID1, false, metadataID, homac.getNumFieldBits()),
                KeyUtil.deriveKeyBI(kdt.getPRG(), seedForID2, false, metadataID, homac.getNumFieldBits()));
//        System.out.println("msg:"+msg);
//        System.out.println("enclong:"+digestDataCipher);
//        System.out.println("mac:"+hoMACTag);
//        System.out.println("~~~");
        return new Ciphertext(digestDataCipher, hoMACTag);
    }
    private long encrypt(long msg, byte[] seedForID1, byte[] seedForID2, long metadataID) {
        long digestDataCipher = enc.encrypt(msg,
                KeyUtil.deriveKey(kdt.getPRG(), seedForID1, true, metadataID),
                KeyUtil.deriveKey(kdt.getPRG(), seedForID2, true, metadataID));
        return digestDataCipher;
    }

    private long decryptWithMAC(Ciphertext msg, byte[] seedForID1, byte[] seedForID2, long metadataID) throws MACCheckFailed {
        long plain = enc.decrypt(msg.DigestDataCipher,
                KeyUtil.deriveKey(kdt.getPRG(), seedForID1, true, metadataID),
                KeyUtil.deriveKey(kdt.getPRG(), seedForID2, true, metadataID));
        System.out.println(plain);
        boolean ok = homac.checkMAC(BigInteger.valueOf(plain), msg.HoMACTag,
                KeyUtil.deriveKeyBI(kdt.getPRG(), seedForID1, false, metadataID, homac.getNumFieldBits()),
                KeyUtil.deriveKeyBI(kdt.getPRG(), seedForID2, false, metadataID, homac.getNumFieldBits()));
        if (!ok) {
            throw new MACCheckFailed("Check failed", msg.HoMACTag);
        }
        return plain;
    }
    private long decrypt(long msg, byte[] seedForID1, byte[] seedForID2, long metadataID) throws MACCheckFailed {
        long plain = enc.decrypt(msg,
                KeyUtil.deriveKey(kdt.getPRG(), seedForID1, true, metadataID),
                KeyUtil.deriveKey(kdt.getPRG(), seedForID2, true, metadataID));
        return plain;
    }

    public Ciphertext encryptDigestDataWithMAC(long msg, long timeID, long metadataID) {
        byte[] seedForID1 = kdt.getSeed(timeID);
        byte[] seedForID2 = kdt.getSeed(timeID + 1);
        return encryptWithMAC(msg, seedForID1, seedForID2, metadataID);
    }

    public Ciphertext encryptDigestDataWithMAC(long msg, long timeID, long metadataID, CachedKeys cachedKeys) {
        if (!cachedKeys.containsKeys()) {
            cachedKeys.setK1(kdt.getSeed(timeID));
            cachedKeys.setK2(kdt.getSeed(timeID + 1));
        }
        return encryptWithMAC(msg, cachedKeys.getK1(), cachedKeys.getK2(), metadataID);
    }

    public long encryptDigestData(long msg, long timeID, long metadataID) {
        byte[] seedForID1 = kdt.getSeed(timeID);
        byte[] seedForID2 = kdt.getSeed(timeID + 1);
        return encrypt(msg, seedForID1, seedForID2, metadataID);
    }

    public long encryptDigestData(long msg, long timeID, long metadataID, CachedKeys cachedKeys) {
        if (!cachedKeys.containsKeys()) {
            cachedKeys.setK1(kdt.getSeed(timeID));
            cachedKeys.setK2(kdt.getSeed(timeID + 1));
        }
        return encrypt(msg, cachedKeys.getK1(), cachedKeys.getK2(), metadataID);
    }

    public long decryptDigestDataWithMAC(Ciphertext msg, long timeIDFrom, long timeIDTo, long metadataID) throws MACCheckFailed {
        byte[] seedForID1 = kdt.getSeed(timeIDFrom);
        byte[] seedForID2 = kdt.getSeed(timeIDTo + 1);
        return decryptWithMAC(msg, seedForID1, seedForID2, metadataID);
    }

    public long decryptDigestDataWithMAC(Ciphertext msg, long timeIDFrom, long timeIDTo, long metadataID, CachedKeys cachedKeys) throws MACCheckFailed {
        if (!cachedKeys.containsKeys()) {
            cachedKeys.setK1(kdt.getSeed(timeIDFrom));
            cachedKeys.setK2(kdt.getSeed(timeIDTo + 1));
        }
        return decryptWithMAC(msg, cachedKeys.getK1(), cachedKeys.getK2(), metadataID);
    }
    public long decryptDigestData(long msg, long timeIDFrom, long timeIDTo, long metadataID, CachedKeys cachedKeys) throws MACCheckFailed {
        if (!cachedKeys.containsKeys()) {
            cachedKeys.setK1(kdt.getSeed(timeIDFrom));
            cachedKeys.setK2(kdt.getSeed(timeIDTo + 1));
        }
        return decrypt(msg, cachedKeys.getK1(), cachedKeys.getK2(), metadataID);
    }

    public Ciphertext[] encryptDigest(long[] msgs, long timeID, long[] metadataIDs) {
        byte[] seedForID1 = kdt.getSeed(timeID);
        byte[] seedForID2 = kdt.getSeed(timeID + 1);
        Ciphertext[] out = new Ciphertext[msgs.length];
        for (int idx = 0; idx < msgs.length; idx++) {
            out[idx] = encryptWithMAC(msgs[idx], seedForID1, seedForID2, metadataIDs[idx]);
        }
        return out;
    }

    public long[] decryptDigest(Ciphertext[] msgs, long timeIDFrom, long timeIDTo, long[] metadataIDs) throws MACCheckFailed {
        byte[] seedForID1 = kdt.getSeed(timeIDFrom);
        byte[] seedForID2 = kdt.getSeed(timeIDTo + 1);
        long[] out = new long[msgs.length];
        for (int idx = 0; idx < msgs.length; idx++) {
            out[idx] = decryptWithMAC(msgs[idx], seedForID1, seedForID2, metadataIDs[idx]);
        }
        return out;
    }

    public static class Ciphertext {
        public long DigestDataCipher;
        public BigInteger HoMACTag;

        public Ciphertext(long DigestDataCipher, BigInteger HoMACTag) {
            this.DigestDataCipher = DigestDataCipher;
            this.HoMACTag = HoMACTag;
        }

        public long getDigestDataCipher() {
            return DigestDataCipher;
        }

        public BigInteger getHoMACTag() {
            return HoMACTag;
        }

        public Ciphertext add(Ciphertext other) {
            // Would be possible to add MAC modulo
            return new Ciphertext(other.DigestDataCipher + this.DigestDataCipher,
                    other.HoMACTag.add(this.HoMACTag));
        }

        public void addMerge(Ciphertext other) {
            // Would be possible to add MAC modulo
            this.DigestDataCipher += other.DigestDataCipher;
            this.HoMACTag = this.HoMACTag.add(other.HoMACTag);
        }
    }
}
