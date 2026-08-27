//package mpc;
//
//import java.math.BigInteger;
//import java.security.SecureRandom;
//import java.util.ArrayList;
//
//import static mpc.until.RandomPrimeGenerator;
//
//public class DHKeyExchange1 {
//
//    public static void exchange(User user1, User user2) {
//        // DH 参数
//        BigInteger p = RandomPrimeGenerator(1024); // 素数 p
//        BigInteger g = new BigInteger("7"); // 生成元 g
//
//        // Alice 生成私钥和公钥
//        BigInteger a = new BigInteger(16, new SecureRandom()); // 随机生成私钥 a
//        BigInteger A = g.modPow(a, p); // 计算公钥 A
//
//        // Bob 生成私钥和公钥
//        BigInteger b = new BigInteger(16, new SecureRandom()); // 随机生成私钥 b
//        BigInteger B = g.modPow(b, p); // 计算公钥 B
//
//        // Alice 计算共享密钥
//        BigInteger aliceSharedKey = B.modPow(a, p);
//
//        // Bob 计算共享密钥
//        BigInteger bobSharedKey = A.modPow(b, p);
//
//        user1.setKey1(getLast14Digits(aliceSharedKey.longValue()));
//        user2.setKey0(getLast14Digits(bobSharedKey.longValue()));
//        user1.setP();
//        user2.setP();
//        // 打印共享密钥
//        /*System.out.println("Alice's shared key: " + aliceSharedKey.longValue());
//        System.out.println("Bob's shared key: " + bobSharedKey.longValue());*/
//    }
//
//    public static long getLast14Digits(long num) {
//        return num % 10000000000000L; // 取模10000000000000L即为取后14位
//    }
//
//    public static void MPC(ArrayList<User> users){
//        int i = 0;
//        while( i < users.size()-1){
//            exchange(users.get(i),users.get(i+1));
//            i++;
//        }
//        exchange(users.get(users.size()-1),users.get(0));
//    }
//
//}
//
