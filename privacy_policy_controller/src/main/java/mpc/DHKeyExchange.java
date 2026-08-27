package mpc;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.util.ArrayList;
import javax.crypto.KeyAgreement;

public class DHKeyExchange {
//
//static {
//     添加Bouncy Castle提供者
//    Security.addProvider(new BouncyCastleProvider());
//}
    public static void exchange(User user1, User user2) throws Exception {
        // 生成 Alice 的密钥对
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("X25519");
        keyPairGen.initialize(255);
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

        // Bob 计算共享密钥
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("X25519");
        bobKeyAgree.init(bobPrivateKey);
        bobKeyAgree.doPhase(alicePublicKey, true);
        byte[] bobSharedSecret = bobKeyAgree.generateSecret();

        // 转换共享密钥为 long 值的64位
        long aliceSharedKey = toLong(aliceSharedSecret);
        long bobSharedKey = toLong(bobSharedSecret);

        // 设置用户的密钥
        user1.setKey1(aliceSharedKey);
        user2.setKey0(bobSharedKey);

        // 打印共享密钥
        /*System.out.println("Alice's shared key: " + aliceSharedKey);
        System.out.println("Bob's shared key: " + bobSharedKey);*/
    }


//    public static void exchange(User user1, User user2) throws Exception {
//        // 生成 Alice 的密钥对
//        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("X25519", "BC");
//        KeyPair aliceKeyPair = keyPairGen.generateKeyPair();
//        PrivateKey alicePrivateKey = aliceKeyPair.getPrivate();
//        PublicKey alicePublicKey = aliceKeyPair.getPublic();
//
//        // 生成 Bob 的密钥对
//        KeyPair bobKeyPair = keyPairGen.generateKeyPair();
//        PrivateKey bobPrivateKey = bobKeyPair.getPrivate();
//        PublicKey bobPublicKey = bobKeyPair.getPublic();
//
//        // Alice 计算共享密钥
//        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("X25519", "BC");
//        aliceKeyAgree.init(alicePrivateKey);
//        aliceKeyAgree.doPhase(bobPublicKey, true);
//        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
//
//        // Bob 计算共享密钥
//        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("X25519", "BC");
//        bobKeyAgree.init(bobPrivateKey);
//        bobKeyAgree.doPhase(alicePublicKey, true);
//        byte[] bobSharedSecret = bobKeyAgree.generateSecret();
//
//        // 转换共享密钥为 long 值的64位
//        long aliceSharedKey = toLong(aliceSharedSecret);
//        long bobSharedKey = toLong(bobSharedSecret);
//
//        // 设置用户的密钥
//        user1.setKey1(aliceSharedKey);
//        user2.setKey0(bobSharedKey);
//
//        // 打印共享密钥
//    /*System.out.println("Alice's shared key: " + aliceSharedKey);
//    System.out.println("Bob's shared key: " + bobSharedKey);*/
//    }

    public static long toLong(byte[] bytes) {
        long result = 0;
        int length = Math.min(8, bytes.length); // 确保只取前8个字节
        for (int i = 0; i < length; i++) {
            result <<= 8;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }

    public static void MPC(ArrayList<User> users) throws Exception {
        int i = 0;
        while (i < users.size() - 1) {
            exchange(users.get(i), users.get(i + 1));
            i++;
        }
        exchange(users.get(users.size() - 1), users.get(0));
    }
}
