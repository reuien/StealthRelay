package mpc;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.KeyAgreement;

public class X25519KeyExchangeTest {

    public static void main(String[] args) {
        int maxIterations = 10000; // 最大迭代次数
        int step = 1000; // 每次增加的迭代次数
        String csvFile = "privacy_policy_controller/src/main/java/mpc/key_exchange_performance.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {
            // 写入CSV文件的表头
            writer.append("iterations,keyPairGenTime,keyAgreementTime\n");

            for (int currentIterations = step; currentIterations <= maxIterations; currentIterations += step) {
                long totalKeyPairGenTime = 0;
                long totalKeyAgreementTime = 0;

                for (int i = 0; i < currentIterations; i++) {
                    // 测量生成密钥对的时间
                    long keyPairGenStart = System.nanoTime();
                    KeyPair aliceKeyPair = generateKeyPair();
                    KeyPair bobKeyPair = generateKeyPair();
                    long keyPairGenEnd = System.nanoTime();
                    totalKeyPairGenTime += (keyPairGenEnd - keyPairGenStart);

                    // 测量密钥协商的时间
                    long keyAgreementStart = System.nanoTime();
                    byte[] aliceSharedSecret = generateSharedSecret(aliceKeyPair.getPrivate(), bobKeyPair.getPublic());
                    byte[] bobSharedSecret = generateSharedSecret(bobKeyPair.getPrivate(), aliceKeyPair.getPublic());
                    long keyAgreementEnd = System.nanoTime();
                    totalKeyAgreementTime += (keyAgreementEnd - keyAgreementStart);

                    // 验证共享密钥是否相同
                    boolean keysMatch = toLong(aliceSharedSecret) == toLong(bobSharedSecret);
                    if (!keysMatch) {
                        System.err.println("Error: Shared keys do not match!");
                    }
                }

                // 将结果写入CSV文件
                writer.append(Integer.toString(currentIterations)).append(",")
                        .append(Long.toString(totalKeyPairGenTime / 1000000)).append(",") // 转换为毫秒
                        .append(Long.toString(totalKeyAgreementTime / 1000000)).append("\n"); // 转换为毫秒

                System.out.println("Iterations: " + currentIterations + " - Key Pair Generation Time: "
                        + (totalKeyPairGenTime / 1000000) + " ms, Key Agreement Time: "
                        + (totalKeyAgreementTime / 1000000) + " ms");
            }

            System.out.println("Results saved to " + csvFile);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("X25519");
        keyPairGen.initialize(255);
        return keyPairGen.generateKeyPair();
    }

    public static byte[] generateSharedSecret(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        KeyAgreement keyAgree = KeyAgreement.getInstance("X25519");
        keyAgree.init(privateKey);
        keyAgree.doPhase(publicKey, true);
        return keyAgree.generateSecret();
    }

    public static long toLong(byte[] bytes) {
        long result = 0;
        int length = Math.min(8, bytes.length); // 确保只取前8个字节
        for (int i = 0; i < length; i++) {
            result <<= 8;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }
}
