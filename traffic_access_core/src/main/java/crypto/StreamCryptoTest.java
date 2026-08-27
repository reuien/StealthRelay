package crypto;

import crypto.StreamCrypto;
import crypto.StreamCrypto.Ciphertext;
import keyDerivation.KeyDerivationTree;
import keyDerivation.KeyDerivationTreeFactory;
import prg.PRGFactory;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.io.FileWriter;
import java.io.IOException;

public class StreamCryptoTest {
    private static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) {
        try {
            // 设置测试参数
            byte[] rootSeed = new byte[16];
            new SecureRandom().nextBytes(rootSeed);
            int depth = 31;
            long msg = 123456789L;
            long metadataID = 1L;
            int maxIterations = 100000; // 最大迭代次数
            int step = 10000; // 每次增加的迭代次数
            String csvFile = "traffic_access_core/src/main/java/crypto/stream_crypto_performance.csv";

            // 生成KeyDerivationTree
            KeyDerivationTree kdt = KeyDerivationTreeFactory.getNewDefaultKDTree(rootSeed, depth);

            // 初始化StreamCrypto
            StreamCrypto streamCrypto = new StreamCrypto(kdt, BigInteger.valueOf(987654321L));

            // 使用反射获取私有方法 decryptWithMAC
            Method decryptWithMACMethod = StreamCrypto.class.getDeclaredMethod("decryptWithMAC", Ciphertext.class, byte[].class, byte[].class, long.class);
            decryptWithMACMethod.setAccessible(true);

            try (FileWriter writer = new FileWriter(csvFile)) {
                // 写入CSV文件的表头
                writer.append("iterations,encryptWithMAC,decryptWithMAC,encrypt,decrypt\n");

                for (int currentIterations = step; currentIterations <= maxIterations; currentIterations += step) {
                    long totalEncryptWithMACTime = 0;
                    long totalDecryptWithMACTime = 0;
                    long totalEncryptTime = 0;
                    long totalDecryptTime = 0;

                    for (int i = 0; i < currentIterations; i++) {
                        long timeIDFrom = 1000;
                        long timeIDTo = timeIDFrom + 1000; // 增加1秒

                        // 生成种子

                        // 测试encryptWithMAC方法
                        long startEncryptWithMAC = System.nanoTime();
                        byte[] seedForID1 = kdt.getSeed(timeIDFrom);
                        byte[] seedForID2 = kdt.getSeed(timeIDTo);
                        Ciphertext ciphertext = streamCrypto.encryptWithMAC(msg, seedForID1, seedForID2, metadataID);
                        long endEncryptWithMAC = System.nanoTime();
                        totalEncryptWithMACTime += (endEncryptWithMAC - startEncryptWithMAC);

                        // 调用私有方法 decryptWithMAC
                        long startDecryptWithMAC = System.nanoTime();
                        seedForID1 = kdt.getSeed(timeIDFrom);
                        seedForID2 = kdt.getSeed(timeIDTo);
                        long decryptedDigestMsg = (long) decryptWithMACMethod.invoke(streamCrypto, ciphertext, seedForID1, seedForID2, metadataID);
                        long endDecryptWithMAC = System.nanoTime();
                        totalDecryptWithMACTime += (endDecryptWithMAC - startDecryptWithMAC);

                        // 测试encrypt方法
                        long startEncrypt = System.nanoTime();
                        seedForID1 = kdt.getSeed(timeIDFrom);
                        seedForID2 = kdt.getSeed(timeIDTo);
                        long encryptedMsg = streamCrypto.encrypt(msg, seedForID1, seedForID2, metadataID);
                        long endEncrypt = System.nanoTime();
                        totalEncryptTime += (endEncrypt - startEncrypt);

                        // 测试decrypt方法
                        long startDecrypt = System.nanoTime();
                        seedForID1 = kdt.getSeed(timeIDFrom);
                        seedForID2 = kdt.getSeed(timeIDTo);
                        long decryptedMsg = streamCrypto.decrypt(encryptedMsg, seedForID1, seedForID2, metadataID);
                        long endDecrypt = System.nanoTime();
                        totalDecryptTime += (endDecrypt - startDecrypt);

                        // 检查解密后的消息是否与原始消息一致
                        assert msg == decryptedDigestMsg : "Decryption with MAC check failed for encrypted message!";
                        assert msg == decryptedMsg : "Decryption failed for encrypted message!";
                    }

                    // 计算每个方法的平均时间
                    long avgEncryptWithMACTime = totalEncryptWithMACTime ;
                    long avgDecryptWithMACTime = totalDecryptWithMACTime  ;
                    long avgEncryptTime = totalEncryptTime  ;
                    long avgDecryptTime = totalDecryptTime  ;

                    // 将结果写入CSV文件
                    writer.append(currentIterations + "," + avgEncryptWithMACTime  + "," + avgDecryptWithMACTime  + "," + avgEncryptTime  + "," + avgDecryptTime  + "\n");
                }

                System.out.println("Results saved to " + csvFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
