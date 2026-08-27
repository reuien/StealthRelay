package crypto.DigestCrypto;

import keyDerivation.KeyDerivationTree;
import keyManagement.KeyUtil;
import keyManagement.StreamKeyManager;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AggrTest2 {

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
        int maxIterations = 1000000; // 最大迭代次数
        int step = 100000; // 每次增加的迭代次数
        String csvFile = "traffic_access_core/src/main/java/crypto/DigestCrypto/Aggr_performance2.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {
            // 写入CSV文件的表头
            writer.append("iterations,encrypt,encryptWithKeyCanceling,decryptWithKeyAggr,decryptWithKeyAggrWithKeyCanceling\n");
            for (int currentIterations = step; currentIterations <= maxIterations; currentIterations += step) {
                long totalEnc = 0;
                long totalEncWithKeyCanceling = 0;
                long totalDec = 0;
                long totalDecWithKeyCanceling = 0;
                long cipherAggr = 0;
                long cipherAggrWithKeyCanceling = 0;
                long keyAggr = 0;
                long keyAggrTime = 0;
                long keyAggrKCTime = 0;

                for (int i = 0; i < currentIterations; i++) {
//                    long key1 = deriveSingleKeyForId(kdt, i);
//                    long key2 = deriveSingleKeyForId(kdt, i + 1);
                    long msg = i;

                    // 记录加密的时间
                    long startEnc = System.nanoTime();
                    long key1 = deriveSingleKeyForId(kdt, i);//

                    long cipher = encrypt(msg, key1);
                    long endEnc = System.nanoTime();
                    totalEnc += (endEnc - startEnc);
                    cipherAggr += cipher;

                    long startEncWithKeyCanceling = System.nanoTime();
                    long key2 = deriveSingleKeyForId(kdt, i);//
                    long key3 = deriveSingleKeyForId(kdt, i + 1);//
                    long cipherWithKeyCanceling = encryptWithKeyCanceling(msg, key2, key3);
                    long endEncWithKeyCanceling = System.nanoTime();
                    totalEncWithKeyCanceling += (endEncWithKeyCanceling - startEncWithKeyCanceling);
                    cipherAggrWithKeyCanceling += cipherWithKeyCanceling;
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
                System.out.println("res: "+result);


                long startDecWithKeyCanceling = System.nanoTime();
                long key1 = deriveSingleKeyForId(kdt, 0);
                long key2 = deriveSingleKeyForId(kdt, currentIterations);
                long resultWithKeyCanceling = decryptWithKeyCanceling(cipherAggrWithKeyCanceling, key1, key2);
                long endDecWithKeyCanceling = System.nanoTime();
                totalDecWithKeyCanceling += (endDecWithKeyCanceling - startDecWithKeyCanceling);
                System.out.println("resultWithKeyCanceling: "+resultWithKeyCanceling);

                // 将结果写入CSV文件
                writer.append(Integer.toString(currentIterations)).append(",")
                        .append(Long.toString(totalEnc/* / 1000000*/)).append(",") // 转换为毫秒
                        .append(Long.toString(totalEncWithKeyCanceling/* / 1000000*/)).append(",")// 转换为毫秒
                        .append(Long.toString(totalDec/* / 1000000*/)).append(",") // 转换为毫秒
                        .append(Long.toString(totalDecWithKeyCanceling/* / 1000000*/)).append("\n"); // 转换为毫秒
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


