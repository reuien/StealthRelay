package crypto.DigestCrypto;

import java.math.BigInteger;

public class BigIntegerAddTimeTest {
    public static void main(String[] args) {
        BigInteger a = new BigInteger("123890");
        BigInteger b = new BigInteger("909210");

        long startTime = System.nanoTime();
        BigInteger result = a.add(b);
        long endTime = System.nanoTime();

        long duration = endTime - startTime; // nanoseconds
        System.out.println("Time taken for addition: " + duration + " nanoseconds");
        System.out.println("Result: " + result);
    }
}
