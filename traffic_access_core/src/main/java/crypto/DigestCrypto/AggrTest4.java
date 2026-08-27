package crypto.DigestCrypto;

import keyDerivation.KeyDerivationTree;
import keyManagement.KeyUtil;
import keyManagement.StreamKeyManager;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.plaf.ColorUIResource;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AggrTest4 {

    private static final SecureRandom random = new SecureRandom();

    public static StreamKeyManager getStreamKeyManager() throws NoSuchAlgorithmException {
        int keyTreeDepth = 31;
        SecretKey streamMasterKey;
        streamMasterKey = KeyGenerator.getInstance("AES").generateKey();
        StreamKeyManager skm = new StreamKeyManager(streamMasterKey.getEncoded(), keyTreeDepth);
        return skm;
    }

    public static long deriveSingleKeyForId(KeyDerivationTree kdt, long id) {
        return KeyUtil.deriveKey(kdt.getPRG(), kdt.getSeed(id), true);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyDerivationTree kdt = getStreamKeyManager().getKeyDerivationTree();
        int maxIterations = 100000; // 最大迭代次数
        int step = 10000; // 每次增加的迭代次数
        String csvFile = "traffic_access_core/src/main/java/crypto/DigestCrypto/Aggr_performance4.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {
            // 写入CSV文件的表头
            writer.append("iterations,encrypt,encryptWithKeyCanceling,decryptWithKeyAggr,decryptWithKeyAggrWithKeyCanceling,plainTextAdd,plainTextSub\n");
            for (int currentIterations = step; currentIterations <= maxIterations; currentIterations += step) {
                long totalEnc = 0;
                long totalEncWithKeyCanceling = 0;
                long totalDec = 0;
                long totalDecWithKeyCanceling = 0;
                long totalPlainTextAdd = 0;
                long totalPlainTextSub = 0;
                long cipherAggr = 0;
                long cipherAggrWithKeyCanceling = 0;
                long plainTextAdd = 0;
                long plainTextSub = 0;
                long keyAggr = 0;
                long keyAggrTime = 0;
                long keyAggrKCTime = 0;


                for (int i = 0; i < currentIterations; i++) {
                    long msg = i;

                    // 记录加密的时间
                    long startEnc = System.nanoTime();
                    long key1 = deriveSingleKeyForId(kdt, i);
                    long cipher = encrypt(msg, key1);
                    long endEnc = System.nanoTime();
                    totalEnc += (endEnc - startEnc);
                    cipherAggr += cipher;

                    long startEncWithKeyCanceling = System.nanoTime();
                    long key2 = deriveSingleKeyForId(kdt, i);
                    long key3 = deriveSingleKeyForId(kdt, i + 1);
                    long cipherWithKeyCanceling = encryptWithKeyCanceling(msg, key2, key3);
                    long endEncWithKeyCanceling = System.nanoTime();
                    totalEncWithKeyCanceling += (endEncWithKeyCanceling - startEncWithKeyCanceling);
                    cipherAggrWithKeyCanceling += cipherWithKeyCanceling;

                    // 记录明文直接相加的时间
                    long startPlainTextAdd = System.nanoTime();
                    plainTextAdd += msg;
                    long endPlainTextAdd = System.nanoTime();
                    totalPlainTextAdd += (endPlainTextAdd - startPlainTextAdd);
                }

                // 记录解密的时间
                long startDec = System.nanoTime();
                for (int j = 0; j < currentIterations; j++) {
                    long key = deriveSingleKeyForId(kdt, j);
                    keyAggr += key;
                }
                long result = decrypt(cipherAggr, keyAggr);
                long endDec = System.nanoTime();
                totalDec = endDec - startDec;
                System.out.println("res: " + result);

                long startDecWithKeyCanceling = System.nanoTime();
                long key1 = deriveSingleKeyForId(kdt, 0);
                long key2 = deriveSingleKeyForId(kdt, currentIterations);
                long resultWithKeyCanceling = decryptWithKeyCanceling(cipherAggrWithKeyCanceling, key1, key2);
                long endDecWithKeyCanceling = System.nanoTime();
                totalDecWithKeyCanceling += (endDecWithKeyCanceling - startDecWithKeyCanceling);
                System.out.println("resultWithKeyCanceling: " + resultWithKeyCanceling);

                // 记录明文直接相减的时间
                for (int i = 0; i < currentIterations; i++) {
                    long startPlainTextSub = System.nanoTime();
                    plainTextSub = plainTextAdd - plainTextAdd;
                    long endPlainTextSub = System.nanoTime();
                    totalPlainTextSub += (endPlainTextSub - startPlainTextSub);
                }

                // 将结果写入CSV文件
                writer.append(Integer.toString(currentIterations)).append(",")
                        .append(Long.toString(totalEnc)).append(",")
                        .append(Long.toString(totalEncWithKeyCanceling)).append(",")
                        .append(Long.toString(totalDec)).append(",")
                        .append(Long.toString(totalDecWithKeyCanceling)).append(",")
                        .append(Long.toString(totalPlainTextAdd)).append(",")
                        .append(Long.toString(totalPlainTextSub)).append("\n");
            }

            System.out.println("Results saved to " + csvFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long encryptWithKeyCanceling(long msg, long key1, long key2) {
        return msg + key1 - key2;
    }

    public static long decryptWithKeyCanceling(long ciphertext, long key1, long key2) {
        return ciphertext - key1 + key2;
    }

    public static long encrypt(long msg, long key) {
        return msg + key;
    }

    public static long decrypt(long ciphertext, long key) {
        return ciphertext - key;
    }
}
