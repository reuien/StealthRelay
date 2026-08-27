package crypto.HoMAC;

import keyDerivation.KeyDerivationTree;
import keyDerivation.SeedNode;
import keyManagement.KeyUtil;
import prg.IPRG;
import prg.PRGFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class HoMACTest {

    public static void main(String[] args) {
        // 初始化参数
        int depth = 31; // 树的深度
        int kFactor = 2; // k因子 (二叉树)
        boolean isOwner = true; // 数据拥有者
        SecureRandom random = new SecureRandom();
        IPRG prg = PRGFactory.getDefaultPRG(); // 替换为实际的 IPRG 实现

        // 为 relevantSeeds 生成示例种子
        ArrayList<SeedNode> relevantSeeds = new ArrayList<>();
        for (int i = 0; i <= depth; i++) {
            byte[] seed = new byte[16]; // 假设种子大小为128位
            random.nextBytes(seed);
            relevantSeeds.add(new SeedNode(seed, i, i));
        }

        KeyDerivationTree tree = new KeyDerivationTree(isOwner, prg, relevantSeeds, depth, kFactor);
        BigInteger prime = HoMAC.PRIME;
        HoMAC homac = new HoMAC(tree, random, prime);

        // 记录结果的CSV文件
        String fileName = "traffic_access_core/src/main/java/crypto/HoMAC/mac_performance.csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            // 写入CSV文件的表头
            writer.append("Number of MACs,MAC Generation Time (ms),MAC Verification Time (ms)\n");

            // 测试不同数量的MAC
            for (int numTests = 100000; numTests <= 5000000; numTests += 100000) {
                BigInteger[] messages = new BigInteger[numTests];
                BigInteger[] macs = new BigInteger[numTests];
                BigInteger[] keys1 = new BigInteger[numTests];
                BigInteger[] keys2 = new BigInteger[numTests];

                // 生成测试数据
                for (int i = 0; i < numTests; i++) {
                    messages[i] = new BigInteger(prime.bitLength(), random);
                    keys1[i] = new BigInteger(prime.bitLength(), random);
                    keys2[i] = new BigInteger(prime.bitLength(), random);
                }

                // 测试生成 MAC 的时间
                long startGetMAC = System.nanoTime();
                for (int i = 0; i < numTests; i++) {
                    macs[i] = homac.getMAC(messages[i], keys1[i], keys2[i]);
                }
                long endGetMAC = System.nanoTime();
                long durationGetMAC = (endGetMAC - startGetMAC) / 1_000_000; // 转换为毫秒

                // 测试验证 MAC 的时间
                long startCheckMAC = System.nanoTime();
                for (int i = 0; i < numTests; i++) {
                    boolean isValid = homac.checkMAC(messages[i], macs[i], keys1[i], keys2[i]);
                    if (!isValid) {
                        System.out.println("MAC 校验失败，测试编号: " + i);
                    }
                }
                long endCheckMAC = System.nanoTime();
                long durationCheckMAC = (endCheckMAC - startCheckMAC) / 1_000_000; // 转换为毫秒

                // 写入CSV文件
                writer.append(numTests + "," + durationGetMAC + "," + durationCheckMAC + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
