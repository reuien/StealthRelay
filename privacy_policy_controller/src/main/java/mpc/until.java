package mpc;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class until {

    public static byte[] getMainKey(String stream_owner_id) {
        //生成秘钥
        byte[] b = new byte[16];
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            // kg.init(128);//要生成多少位，只需要修改这里即可128, 192或256
            //SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以生成的秘钥就一样。
            kg.init(128, new SecureRandom(stream_owner_id.getBytes()));
            SecretKey sk = kg.generateKey();
            b = sk.getEncoded();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("没有此算法。");
        }
        return b;
    }

    public static long bytesToLong(byte[] in) {
        ByteBuffer buffer = ByteBuffer.allocate(in.length);
        buffer.put(in);
        buffer.flip();
        return buffer.getLong();
    }

    public static BigInteger RandomPrimeGenerator (int bitLength ) {
        SecureRandom random = new SecureRandom();
        BigInteger prime = BigInteger.probablePrime(bitLength, random);
        return  prime;
    }
}
