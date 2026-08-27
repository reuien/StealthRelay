package mpc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MPCPerformanceTest {

    public static void main(String[] args) {
        // 设置最大用户数量和步长
        int maxUsers = 10000; // 最大用户数量
        int step = 1000; // 每次增加的用户数量
        String fileName = "privacy_policy_controller/src/main/java/mpc/mpc_performance.csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            // 写入CSV文件的表头
            writer.append("Number of Users,Time (ms)\n");

            for (int currentUsers = step; currentUsers <= maxUsers; currentUsers += step) {
                // 创建用户列表
                ArrayList<User> users = new ArrayList<>();
                for (int i = 0; i < currentUsers; i++) {
                    users.add(new User());
                }

                // 测试 MPC 方法的时间消耗
                long startTime = System.currentTimeMillis();
                try {
                    DHKeyExchange.MPC(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // 打印结果
                System.out.println("用户数量: " + currentUsers + " | 耗时: " + duration + " ms");

                // 写入CSV文件
                writer.append(currentUsers + "," + duration + "\n");
            }

            System.out.println("测试完成，结果已保存至 " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
