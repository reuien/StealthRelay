
package prg;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PRGAes implements IPRG {

    private Cipher cipher;

    public PRGAes(){
        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private byte[] AESBlockEncrypt(byte[] key, byte[] value) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return cipher.doFinal(value);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public byte[] apply(byte[] prgKey, byte[] input) {
        return AESBlockEncrypt(prgKey, input);
    }

    @Override
    public byte[] apply(byte[] prgKey, int input) {
        byte[] data = new byte[16];
        for (int shift = 0; shift < 4; shift++) {
            data[15 - shift] = (byte) ((input >> (shift * 8)) & 0xFF);
        }
        return apply(prgKey, data);
    }

    @Override
    public byte[] multiApply(byte[] prgKey, int[] inputs) {
        byte[] cur = prgKey;
        for (int k_iter : inputs) {
            cur = apply(cur, k_iter);
        }
        return cur;
    }
}
