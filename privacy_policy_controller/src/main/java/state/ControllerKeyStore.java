
package state;

import exceptions.CouldNotReceiveException;
import exceptions.CouldNotStoreException;
import exceptions.InvalidQueryException;
import exceptions.QueryFailedException;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a TimeCryptKeystore that uses a local file to store data.
 */
public class ControllerKeyStore {

    private static final String KEYSTORE_TYPE = "pkcs12";
    private final java.security.KeyStore keyStore;
    private final String path;
    private final char[] pwdArray;
    private final boolean dirty;
    private final Map<String, SecretKey> keyCache = Collections.synchronizedMap(new HashMap<>());

    private ControllerKeyStore(java.security.KeyStore ks, String path, char[] pwdArray) {
        this.keyStore = ks;
        this.pwdArray = pwdArray;
        this.path = path;
        dirty = false;
    }

    public static ControllerKeyStore localKeystoreFromFile(String path, char[] pwdArray) throws IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException {
        if (!new File(path).exists()) {
            throw new FileNotFoundException();
        }

        java.security.KeyStore ks = java.security.KeyStore.getInstance(KEYSTORE_TYPE);
        ks.load(new FileInputStream(path), pwdArray);

        return new ControllerKeyStore(ks, path, pwdArray);
    }

    public static ControllerKeyStore createLocalKeystore(String path, char[] pwdArray) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        java.security.KeyStore ks = java.security.KeyStore.getInstance(KEYSTORE_TYPE);
        ks.load(null, pwdArray);
        return new ControllerKeyStore(ks, path, pwdArray);
    }

    public void syncKeystore(boolean force) throws QueryFailedException, CouldNotStoreException {
        if (path != null) {
            if (new File(path).exists() && !force) {
                throw new QueryFailedException(QueryFailedException.FailReason.FILE_ALREADY_EXISTING, "File: " + path
                        + " Please force writing if you are sure to overwrite it.");
            }

            try {
                File parent = new File(path).getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                keyStore.store(new FileOutputStream(path), pwdArray);
            } catch (KeyStoreException | NoSuchAlgorithmException | IOException | CertificateException e) {
                throw new CouldNotStoreException("Error occurred during storing of the keystore: " + e.getMessage());
            }
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void storeStreamKey(String keyID, SecretKey streamMasterKey) throws CouldNotStoreException {
        keyCache.remove(keyID);
        // Don't put a password to the key because the keystore is already encrypted.
        try {
            keyStore.setEntry(keyID, new java.security.KeyStore.SecretKeyEntry(streamMasterKey),
                    new java.security.KeyStore.PasswordProtection("".toCharArray()));
        } catch (KeyStoreException e) {
            throw new CouldNotStoreException("Error occurred during storing of the keystore: " + e.getMessage());
        }

    }

    public SecretKey receiveStreamKey(String keyId) throws CouldNotReceiveException, InvalidQueryException {
        // Don't put a password to the key because the keystore is already encrypted.
        if (keyCache.containsKey(keyId))
            return keyCache.get(keyId);
        try {
            SecretKey out = (SecretKey) keyStore.getKey(keyId, "".toCharArray());
            if (out != null) {
                keyCache.put(keyId, out);
            }
            return out;
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new CouldNotReceiveException("Error occurred during reciving the key with id " + keyId
                    + " Error is: " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw new InvalidQueryException("Error occurred during storing of the keystore: " + e.getMessage());
        }
    }
}
