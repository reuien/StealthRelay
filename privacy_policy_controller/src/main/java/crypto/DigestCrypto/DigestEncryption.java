package crypto.DigestCrypto;

import keyManagement.KeyUtil;
import keyDerivation.KeyDerivationTree;

public class DigestEncryption {

    private KeyDerivationTree kdTree;

    public DigestEncryption(KeyDerivationTree kdTree) {
        this.kdTree = kdTree;
    }

    public long encrypt(long msg, long key1, long key2) {
        System.out.println("key1: "+key1);
        System.out.println("key2: "+key2);
        return msg + key1 - key2;
    }

    public long decrypt(long ciphertext, long key1, long key2) {
        return ciphertext - key1 + key2;
    }

    public long deriveSingleKeyForId(long id) {
        return KeyUtil.deriveKey(kdTree.getPRG(), kdTree.getSeed(id), true);
    }

    public long encryptSingleMsgWithId(long msg, long id) {
        return encrypt(msg, deriveSingleKeyForId(id), deriveSingleKeyForId(id + 1));
    }

    public long decryptSingleMsgWithId(long ciphertext, long msgID) {
        return ciphertext - deriveSingleKeyForId(msgID) + deriveSingleKeyForId(msgID + 1);
    }

    public long decryptSingleMsgAggr(long ciphertext, long msgFrom, long msgTo) {
        return decrypt(ciphertext, deriveSingleKeyForId(msgFrom), deriveSingleKeyForId(msgTo + 1));
    }

    public int getNumMBits() {
        return 64;
    }

}
