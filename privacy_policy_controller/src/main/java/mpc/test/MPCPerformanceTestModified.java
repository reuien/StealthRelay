package mpc.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyAgreement;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.util.ArrayList;

public class MPCPerformanceTestModified {

    public static void main(String[] args) {
        // 设置最大用户数量和步长
        int maxUsers = 10000; // 最大用户数量
        int step = 1000; // 每次增加的用户数量
        String fileName = "privacy_policy_controller/src/main/java/mpc/test/mpc_performance.csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            // 写入CSV文件的表头
            writer.append("Users,Time(ms),Total Bandwidth(KB),SecretKey Size (KB)\n");

            for (int currentUsers = step; currentUsers <= maxUsers; currentUsers += step) {
                // 创建用户列表
                ArrayList<UserModified> users = new ArrayList<>();
                for (int i = 0; i < currentUsers; i++) {
                    users.add(new UserModified("User" + (i + 1)));
                }

                // 测试 MPC 方法的时间消耗
                long startTime = System.currentTimeMillis();
                try {
                    DHKeyExchangeModified.MPC(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // 计算总通信开销和共享密钥大小
                int totalBandwidth = 0;
                int totalSharedKeySize = 0;
                for (UserModified user : users) {
                    totalBandwidth += user.getBandwidth();
                    totalSharedKeySize += user.getSharedKeySize(); // 累计共享密钥大小
                }

                // 将结果转换为KB
                double totalBandwidthKB = totalBandwidth / 1024.0;
                double totalSharedKeySizeKB = totalSharedKeySize / 1024.0;

                // 打印结果
                System.out.println("用户数量: " + currentUsers + " | 耗时: " + duration + " ms | 总通信开销: " + totalBandwidthKB + " KB | 总共享密钥大小: " + totalSharedKeySizeKB + " KB");

                // 写入CSV文件
                writer.append(currentUsers + "," + duration + "," + totalBandwidthKB + "," + totalSharedKeySizeKB + "\n");
            }

            System.out.println("测试完成，结果已保存至 " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class UserModified {
    private String name;
    private long key0;
    private long key1;
    private int bandwidth;
    private int sharedKeySize;

    public UserModified(String name) {
        this.name = name;
        this.bandwidth = 0;
        this.sharedKeySize = 0;
    }

    public String getName() {
        return name;
    }

    public long getKey0() {
        return key0;
    }

    public void setKey0(long key0) {
        this.key0 = key0;
    }

    public long getKey1() {
        return key1;
    }

    public void setKey1(long key1) {
        this.key1 = key1;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void addBandwidth(int bandwidth) {
        this.bandwidth += bandwidth;
    }

    public int getSharedKeySize() {
        return sharedKeySize;
    }

    public void addSharedKeySize(int size) {
        this.sharedKeySize += size;
    }
}

class DHKeyExchangeModified {
    static {
        // 添加Bouncy Castle提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void exchange(UserModified user1, UserModified user2) throws Exception {
        // 生成 Alice 的密钥对
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("X25519", "BC");
        keyPairGen.initialize(255);
        KeyPair aliceKeyPair = keyPairGen.generateKeyPair();
        PrivateKey alicePrivateKey = aliceKeyPair.getPrivate();
        PublicKey alicePublicKey = aliceKeyPair.getPublic();

        // 生成 Bob 的密钥对
        KeyPair bobKeyPair = keyPairGen.generateKeyPair();
        PrivateKey bobPrivateKey = bobKeyPair.getPrivate();
        PublicKey bobPublicKey = bobKeyPair.getPublic();

        // 模拟带宽开销: 计算公钥的大小
        int publicKeySize = alicePublicKey.getEncoded().length;

        // Alice 计算共享密钥
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("X25519", "BC");
        aliceKeyAgree.init(alicePrivateKey);
        aliceKeyAgree.doPhase(bobPublicKey, true);
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();

        // Bob 计算共享密钥
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("X25519", "BC");
        bobKeyAgree.init(bobPrivateKey);
        bobKeyAgree.doPhase(alicePublicKey, true);
        byte[] bobSharedSecret = bobKeyAgree.generateSecret();

        // 转换共享密钥为 long 值的64位
        long aliceSharedKey = toLong(aliceSharedSecret);
        long bobSharedKey = toLong(bobSharedSecret);

        // 设置用户的密钥
        user1.setKey1(aliceSharedKey);
        user2.setKey0(bobSharedKey);

        // 设置共享密钥大小
        user1.addSharedKeySize(aliceSharedSecret.length);
//        user2.addSharedKeySize(aliceSharedSecret.length);
        user2.addSharedKeySize(0);

        // 返回带宽开销
        user1.addBandwidth(publicKeySize);
        user2.addBandwidth(publicKeySize);
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

    public static void MPC(ArrayList<UserModified> users) throws Exception {
        int i = 0;
        while (i < users.size() - 1) {
            exchange(users.get(i), users.get(i + 1));
            i++;
        }
        exchange(users.get(users.size() - 1), users.get(0));
    }
}
