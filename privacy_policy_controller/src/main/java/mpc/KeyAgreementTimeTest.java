package mpc;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyAgreement;
import java.security.*;

public class KeyAgreementTimeTest {
    static {
        // 添加Bouncy Castle提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        int testIterations = 1000; // 可以根据需要调整测试次数

        KeyAgreementTimeTest test = new KeyAgreementTimeTest();

        double ecdhAvgTime = test.testECDH(testIterations);
        System.out.println("ECDH平均密钥协商时间: " + ecdhAvgTime + " 毫秒");

        double x25519AvgTime = test.testX25519(testIterations);
        System.out.println("X25519平均密钥协商时间: " + x25519AvgTime + " 毫秒");

        double aliceBobX25519AvgTime = test.testAliceBobX25519(testIterations);
        System.out.println("Alice 和 Bob 的 X25519 平均密钥协商时间: " + aliceBobX25519AvgTime + " 毫秒");
    }

    public double testECDH(int iterations) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);

        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();

            // 生成 Alice 的密钥对
            KeyPair aliceKeyPair = keyPairGenerator.generateKeyPair();
            PrivateKey alicePrivateKey = aliceKeyPair.getPrivate();
            PublicKey alicePublicKey = aliceKeyPair.getPublic();

            // 生成 Bob 的密钥对
            KeyPair bobKeyPair = keyPairGenerator.generateKeyPair();
            PrivateKey bobPrivateKey = bobKeyPair.getPrivate();
            PublicKey bobPublicKey = bobKeyPair.getPublic();

            // Alice 计算共享密钥
            KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("ECDH");
            aliceKeyAgree.init(alicePrivateKey);
            aliceKeyAgree.doPhase(bobPublicKey, true);
            byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();

            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        return (totalTime / iterations) / 1_000_000.0; // 转换为毫秒
    }

    public double testX25519(int iterations) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519", "BC");

        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();

            // 生成 Alice 的密钥对
            KeyPair aliceKeyPair = keyPairGenerator.generateKeyPair();
            PrivateKey alicePrivateKey = aliceKeyPair.getPrivate();
            PublicKey alicePublicKey = aliceKeyPair.getPublic();

            // 生成 Bob 的密钥对
            KeyPair bobKeyPair = keyPairGenerator.generateKeyPair();
            PrivateKey bobPrivateKey = bobKeyPair.getPrivate();
            PublicKey bobPublicKey = bobKeyPair.getPublic();

            // Alice 计算共享密钥
            KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("X25519", "BC");
            aliceKeyAgree.init(alicePrivateKey);
            aliceKeyAgree.doPhase(bobPublicKey, true);
            byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }
        return (totalTime / iterations) / 1_000_000.0; // 转换为毫秒
    }

    public double testAliceBobX25519(int iterations) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("X25519");
        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            // 生成 Alice 的密钥对
            KeyPair aliceKeyPair = keyPairGen.generateKeyPair();
            PrivateKey alicePrivateKey = aliceKeyPair.getPrivate();
            PublicKey alicePublicKey = aliceKeyPair.getPublic();
            // 生成 Bob 的密钥对
            KeyPair bobKeyPair = keyPairGen.generateKeyPair();
            PrivateKey bobPrivateKey = bobKeyPair.getPrivate();
            PublicKey bobPublicKey = bobKeyPair.getPublic();
            // Alice 计算共享密钥
            KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("X25519");
            aliceKeyAgree.init(alicePrivateKey);
            aliceKeyAgree.doPhase(bobPublicKey, true);
            byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }
        return (totalTime / iterations) / 1_000_000.0; // 转换为毫秒
    }
}
