package crypto.HoMAC;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class MACTest {

    private static final SecureRandom random = new SecureRandom();
    private static final int BIT_LENGTH = 128; // 假设 256 位的素数
    private static final BigInteger prime = new BigInteger(BIT_LENGTH, 100, random);
    private static final BigInteger macKey = new BigInteger(BIT_LENGTH, random).mod(prime);
    private static final BigInteger macKeyInv = macKey.modInverse(prime);

    public static void main(String[] args) {
        int maxIterations = 1000000; // 最大迭代次数
        int step = 100000; // 每次增加的迭代次数
        String csvFile = "traffic_access_core/src/main/java/crypto/HoMAC/mac_performance.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {
            // 写入CSV文件的表头
            writer.append("iterations,getMACDuration,checkMACDuration\n");

            for (int currentIterations = step; currentIterations <= maxIterations; currentIterations += step) {
                long totalGetMACDuration = 0;
                long totalCheckMACDuration = 0;

                for (int i = 0; i < currentIterations; i++) {
                    BigInteger key1 = new BigInteger(BIT_LENGTH, random).mod(prime);
                    BigInteger key2 = new BigInteger(BIT_LENGTH, random).mod(prime);
                    BigInteger msg = new BigInteger(64, random).mod(prime);

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
