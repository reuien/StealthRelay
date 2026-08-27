package keyManagement;

import keyDerivation.KeyDerivationTree;
import keyDerivation.KeyDerivationTreeFactory;
import keyDerivation.SeedNode;

import java.math.BigInteger;
import java.util.ArrayList;

public class StreamKeyManager {

    private final KeyDerivationTree kdt;
    private final byte[] macKey;
    private final byte[] sharingKeystreamMasterKey;
    private boolean isMaster;

    public StreamKeyManager(byte[] streamMasterKey, int numKeysDepth) {
        KeyDerivationTree masterTree = KeyDerivationTreeFactory.getNewDefaultKDTree(streamMasterKey, 2);
        byte[] metadataEncryptionKey = masterTree.getSeed(1);
        kdt = KeyDerivationTreeFactory.getNewDefaultKDTree(metadataEncryptionKey, numKeysDepth);
        macKey = masterTree.getSeed(2);
        sharingKeystreamMasterKey = masterTree.getSeed(3);
        isMaster = true;
    }

    public StreamKeyManager(ArrayList<SeedNode> nodes, byte[] macKey, int numKeysDepth) {
        this.kdt = KeyDerivationTreeFactory.getNewDefaultKDTree(nodes, numKeysDepth);
        this.macKey = macKey;
        sharingKeystreamMasterKey = null;
        isMaster = false;
    }

    public BigInteger getMacKeyAsBigInteger() {
        return new BigInteger(macKey);
    }

    public KeyDerivationTree getKeyDerivationTree() {
        return this.kdt;
    }

    public byte[] getMacKey(){
        return this.macKey;
    }

    public byte[] getChunkEncryptionKey(long chunkId) {
        return KeyUtil.deriveCombinedKey(kdt.getPRG(),
                kdt.getSeed(chunkId),
                kdt.getSeed(chunkId + 1));
    }

    public byte[] getChunkEncryptionKey(long chunkId, CachedKeys keys) {
        if (!keys.containsKeys()) {
            keys.setK1(kdt.getSeed(chunkId));
            keys.setK2(kdt.getSeed(chunkId + 1));
        }
        return KeyUtil.deriveCombinedKey(kdt.getPRG(), keys.k1, keys.k2);
    }

    public KeyDerivationTree getSharingKeyRegression(int precision, int depth) {
        if (isMaster) {
            byte[] precisionMasterSecret = this.kdt.getPRG().apply(sharingKeystreamMasterKey, precision);
            return KeyDerivationTreeFactory.getNewDefaultKDTree(precisionMasterSecret, depth);
        } else {
            throw new RuntimeException("Non-owner is not able to share");
        }
    }

    public boolean isMaster() {
        return this.isMaster;
    }


}
