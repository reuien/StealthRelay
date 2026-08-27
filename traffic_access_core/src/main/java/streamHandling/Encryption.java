package streamHandling;

import crypto.StreamCrypto;
import keyManagement.CachedKeys;
import keyManagement.StreamKeyManager;

public class Encryption {
    private static final boolean HAS_MAC = true;

    public static EncryptedChunk encryptChunk(Chunk chunk, StreamKeyManager streamKeyManager,
                                              long streamId, long chunkId) throws Exception {

        return new EncryptedChunk(streamId, chunkId, chunk.encrypt(streamKeyManager));
    }

    public static CiphertextPairNew encryptChunkAndDigest(long streamId, long chunkId, Chunk chunk, StreamKeyManager skm) throws Exception {
        CiphertextPairNew pair = new CiphertextPairNew();
        CachedKeys keys = new CachedKeys();

        pair.encryptedDigest = getEncryptedDigest(chunk.calculateDigest(), skm, keys);

        pair.encryptedChunk = new EncryptedChunk(streamId, chunkId, chunk.encrypt(skm, keys));
        return pair;
    }

    public static CiphertextPairNew encryptChunkAndDigestNew(long streamId, long chunkId, Chunk chunk, StreamKeyManager skm) throws Exception {
        CiphertextPairNew pair = new CiphertextPairNew();
        CachedKeys keys = new CachedKeys();
        pair.encryptedDigest = getEncryptedDigestNew(chunk.calculateDigestNew(), skm, keys);
        pair.encryptedChunk = new EncryptedChunk(streamId, chunkId, chunk.encrypt(skm, keys));
        return pair;
    }

    public static Digest getEncryptedDigest(Digest digest, StreamKeyManager streamKeyManager, CachedKeys cachedkeys) {
        StreamCrypto sc = new StreamCrypto(streamKeyManager.getKeyDerivationTree(), streamKeyManager.getMacKeyAsBigInteger());
        if (HAS_MAC){
            StreamCrypto.Ciphertext sumEnc = sc.encryptDigestDataWithMAC(digest.getSum(), digest.getChunkIdFrom(), digest.getSumId(), cachedkeys);
            digest.setSum(sumEnc.getDigestDataCipher());
            digest.setSumMac(sumEnc.getHoMACTag());
            StreamCrypto.Ciphertext countEnc = sc.encryptDigestDataWithMAC(digest.getCount(), digest.getChunkIdFrom(), digest.getCountId(), cachedkeys);
            digest.setCount(countEnc.getDigestDataCipher());
            digest.setCountMac(countEnc.getHoMACTag());
            StreamCrypto.Ciphertext squareEnc = sc.encryptDigestDataWithMAC(digest.getSquare(), digest.getChunkIdFrom(), digest.getSquareId(), cachedkeys);
            digest.setSquare(squareEnc.getDigestDataCipher());
            digest.setSquareMac(squareEnc.getHoMACTag());
            digest.setEncrypted(true);
            digest.setHasMac(true);
        }else {
            long sumEnc = sc.encryptDigestData(digest.getSum(), digest.getChunkIdFrom(), digest.getSumId(), cachedkeys);
            digest.setSum(sumEnc);
            long countEnc = sc.encryptDigestData(digest.getCount(), digest.getChunkIdFrom(), digest.getCountId(), cachedkeys);
            digest.setCount(countEnc);
            long squareEnc = sc.encryptDigestData(digest.getSquare(), digest.getChunkIdFrom(), digest.getSquareId(), cachedkeys);
            digest.setSquare(squareEnc);
            digest.setEncrypted(true);
        }
        return digest;
    }

    public static Digest getEncryptedDigestNew(Digest digest, StreamKeyManager streamKeyManager, CachedKeys cachedkeys) {
        StreamCrypto sc = new StreamCrypto(streamKeyManager.getKeyDerivationTree(), streamKeyManager.getMacKeyAsBigInteger());
        if (HAS_MAC){
            StreamCrypto.Ciphertext sumEnc = sc.encryptDigestDataWithMAC(digest.getSum(), digest.getChunkIdFrom(), digest.getSumId(), cachedkeys);
            digest.setSum(sumEnc.getDigestDataCipher());
            digest.setSumMac(sumEnc.getHoMACTag());
            StreamCrypto.Ciphertext countEnc = sc.encryptDigestDataWithMAC(digest.getCount(), digest.getChunkIdFrom(), digest.getCountId(), cachedkeys);
            digest.setCount(countEnc.getDigestDataCipher());
            digest.setCountMac(countEnc.getHoMACTag());
            StreamCrypto.Ciphertext squareEnc = sc.encryptDigestDataWithMAC(digest.getSquare(), digest.getChunkIdFrom(), digest.getSquareId(), cachedkeys);
            digest.setSquare(squareEnc.getDigestDataCipher());
            digest.setSquareMac(squareEnc.getHoMACTag());
            StreamCrypto.Ciphertext count1Enc = sc.encryptDigestDataWithMAC(digest.getCount1(), digest.getChunkIdFrom(), digest.getCount1Id(), cachedkeys);
            digest.setCount1(count1Enc.getDigestDataCipher());
            digest.setCount1Mac(count1Enc.getHoMACTag());
            StreamCrypto.Ciphertext count2Enc = sc.encryptDigestDataWithMAC(digest.getCount2(), digest.getChunkIdFrom(), digest.getCount2Id(), cachedkeys);
            digest.setCount2(count2Enc.getDigestDataCipher());
            digest.setCount2Mac(count2Enc.getHoMACTag());
            StreamCrypto.Ciphertext count3Enc = sc.encryptDigestDataWithMAC(digest.getCount3(), digest.getChunkIdFrom(), digest.getCount3Id(), cachedkeys);
            digest.setCount3(count3Enc.getDigestDataCipher());
            digest.setCount3Mac(count3Enc.getHoMACTag());
            StreamCrypto.Ciphertext count4Enc = sc.encryptDigestDataWithMAC(digest.getCount4(), digest.getChunkIdFrom(), digest.getCount4Id(), cachedkeys);
            digest.setCount4(count4Enc.getDigestDataCipher());
            digest.setCount4Mac(count4Enc.getHoMACTag());
            StreamCrypto.Ciphertext count5Enc = sc.encryptDigestDataWithMAC(digest.getCount5(), digest.getChunkIdFrom(), digest.getCount5Id(), cachedkeys);
            digest.setCount5(count5Enc.getDigestDataCipher());
            digest.setCount5Mac(count5Enc.getHoMACTag());
            StreamCrypto.Ciphertext count6Enc = sc.encryptDigestDataWithMAC(digest.getCount6(), digest.getChunkIdFrom(), digest.getCount6Id(), cachedkeys);
            digest.setCount6(count6Enc.getDigestDataCipher());
            digest.setCount6Mac(count6Enc.getHoMACTag());
            digest.setEncrypted(true);
            digest.setHasMac(true);
        }else {
            long sumEnc = sc.encryptDigestData(digest.getSum(), digest.getChunkIdFrom(), digest.getSumId(), cachedkeys);
            digest.setSum(sumEnc);
            long countEnc = sc.encryptDigestData(digest.getCount(), digest.getChunkIdFrom(), digest.getCountId(), cachedkeys);
            digest.setCount(countEnc);
            long squareEnc = sc.encryptDigestData(digest.getSquare(), digest.getChunkIdFrom(), digest.getSquareId(), cachedkeys);
            digest.setSquare(squareEnc);
            long count1Enc = sc.encryptDigestData(digest.getCount1(), digest.getChunkIdFrom(), digest.getCount1Id(), cachedkeys);
            digest.setCount1(count1Enc);
            long count2Enc = sc.encryptDigestData(digest.getCount2(), digest.getChunkIdFrom(), digest.getCount2Id(), cachedkeys);
            digest.setCount2(count2Enc);
            long count3Enc = sc.encryptDigestData(digest.getCount3(), digest.getChunkIdFrom(), digest.getCount3Id(), cachedkeys);
            digest.setCount3(count3Enc);
            long count4Enc = sc.encryptDigestData(digest.getCount4(), digest.getChunkIdFrom(), digest.getCount4Id(), cachedkeys);
            digest.setCount4(count4Enc);
            long count5Enc = sc.encryptDigestData(digest.getCount5(), digest.getChunkIdFrom(), digest.getCount5Id(), cachedkeys);
            digest.setCount5(count5Enc);
            long count6Enc = sc.encryptDigestData(digest.getCount6(), digest.getChunkIdFrom(), digest.getCount6Id(), cachedkeys);
            digest.setCount6(count6Enc);
            digest.setEncrypted(true);
        }
        return digest;
    }

    public static class CiphertextPairNew {
        public Digest encryptedDigest;
        public EncryptedChunk encryptedChunk;
    }

}
