package crypto;

import java.math.BigInteger;

public class MACCheckFailed extends Exception {
    private BigInteger tag;
    private BigInteger match;

    public MACCheckFailed(String message, BigInteger tag) {
        super(message);
        this.tag = tag;
    }
}
