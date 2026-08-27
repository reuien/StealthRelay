package crypto.HoMAC;

import keyDerivation.KeyDerivationTree;
import keyManagement.KeyUtil;
import keyManagement.StreamKeyManager;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MACTestNew {

    private static final SecureRandom random = new SecureRandom();
    private static final int BIT_LENGTH = 128; // 假设 256 位的素数
    public static final BigInteger prime = new BigInteger("340282366920938463463374607431768211297");
    private static final BigInteger macKey = new BigInteger(BIT_LENGTH, random).mod(prime);
    private static final BigInteger macKeyInv = macKey.modInverse(prime);

    public static StreamKeyManager getStreamKeyManager() throws NoSuchAlgorithmException {
        int keyTreeDepth = 31;
        SecretKey streamMasterKey;
        streamMasterKey = KeyGenerator.getInstance("AES").generateKey();
        StreamKeyManager skm = new StreamKeyManager(streamMasterKey.getEncoded(), keyTreeDepth);
        return skm;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {

        StreamKeyManager streamKeyManager = getStreamKeyManager();
        KeyDerivationTree kdt = streamKeyManager.getKeyDerivationTree();
        //HoMAC homac = new HoMAC(streamKeyManager.getKeyDerivationTree(), streamKeyManager.getMacKeyAsBigInteger());
        int maxIterations = 1000000; // 最大迭代次数
        int step = 100000; // 每次增加的迭代次数
        String csvFile = "traffic_access_core/src/main/java/crypto/HoMAC/mac_performance_new.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {
            // 写入CSV文件的表头
            writer.append("iterations,getMACDuration,checkMACDuration\n");

            for (int currentIterations = step; currentIterations <= maxIterations; currentIterations += step) {
                long totalGetMACDuration = 0;
                long totalCheckMACDuration = 0;

                for (int i = 0; i < currentIterations; i++) {
                    BigInteger key1 = KeyUtil.deriveKeyBI(kdt.getPRG(), kdt.getSeed(i), false, 0, 128);
                    BigInteger key2 = KeyUtil.deriveKeyBI(kdt.getPRG(), kdt.getSeed(i+1), false, 0, 128);
                    BigInteger msg = BigInteger.valueOf(i);

                    // 记录生成MAC的时间
                    long startGetMAC = System.nanoTime();
                    BigInteger mac = getMAC(msg, key1, key2);
                    long endGetMAC = System.nanoTime();
                    totalGetMACDuration += (endGetMAC - startGetMAC);

                    // 记录检查MAC的时间
                    long startCheckMAC = System.nanoTime();
                    boolean result = checkMAC(msg, mac, key1, key2);
                    long endCheckMAC = System.nanoTime();
                    totalCheckMACDuration += (endCheckMAC - startCheckMAC);
                }

                // 将结果写入CSV文件
                writer.append(Integer.toString(currentIterations)).append(",")
                        .append(Long.toString(totalGetMACDuration / 1000000)).append(",") // 转换为毫秒
                        .append(Long.toString(totalCheckMACDuration / 1000000)).append("\n"); // 转换为毫秒

                System.out.println("Iterations: " + currentIterations + " - getMAC total duration: "
                        + (totalGetMACDuration / 1000000) + " ms, checkMAC total duration: "
                        + (totalCheckMACDuration / 1000000) + " ms");
            }

            System.out.println("Results saved to " + csvFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BigInteger getMAC(BigInteger msg, BigInteger key1, BigInteger key2) {
        BigInteger key = key1.subtract(key2).mod(prime);
        return key.subtract(msg).multiply(macKeyInv).mod(prime);
    }

    public static boolean checkMAC(BigInteger msg, BigInteger mac, BigInteger key1, BigInteger key2) {
        BigInteger key = key1.subtract(key2).mod(prime);
        BigInteger comp = mac.multiply(macKey).mod(prime).add(msg).mod(prime);
        return key.compareTo(comp) == 0;
    }
}
