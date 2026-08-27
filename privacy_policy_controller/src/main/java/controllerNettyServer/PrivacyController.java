package controllerNettyServer;

import blk.StoreInterface;
import exceptions.CouldNotReceiveException;
import exceptions.CouldNotStoreException;
import exceptions.InvalidQueryException;
import exceptions.QueryFailedException;
import keyDerivation.KeyDerivationTree;
import keyDerivation.SeedNode;
import keyManagement.KeyUtil;
import keyManagement.StreamKeyManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import prg.IPRG;
import sqlConnect.FrontEndSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.ControllerKeyStore;
import streamHandling.*;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class PrivacyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyController.class);
    private static final String OWNER_KEYSTORE_PASSWORD = "usrTestCryptPassword";
    public ControllerKeyStore keyStore;
    private String usrName;
    private byte[] sharedSecretKey;
    private static final int NONCE_SIZE = 12;
    private StoreInterface storeInterface = getStoreInterface();

    public PrivacyController(String usrName) throws IOException, CertificateException, KeyStoreException,
            NoSuchAlgorithmException, CouldNotStoreException, QueryFailedException {
        this.usrName = usrName;
        String keyPath = projectPath("privacy_policy_controller/src/main/java/key/" + usrName + "_KeyStore.jks").toString();
        String keyPassword = usrName + "_KeyPassword";
        if (!(new File(keyPath)).exists()) {
            this.keyStore = ControllerKeyStore.createLocalKeystore(keyPath, keyPassword.toCharArray());
            this.keyStore.syncKeystore(true);
        }else {
            this.keyStore = ControllerKeyStore.localKeystoreFromFile(keyPath, keyPassword.toCharArray());
        }
    }
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public StoreInterface getStoreInterface(){
        boolean useLocalNetwork = false; // true表示使用本地网络，false表示使用Sepolia测试网络
        return new StoreInterface(useLocalNetwork);
    }

    public void refreshProfile() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        String keyPath = projectPath("privacy_policy_controller/src/main/java/key/" + usrName + "_KeyStore.jks").toString();
        String keyPassword = usrName + "_KeyPassword";
        this.keyStore = ControllerKeyStore.localKeystoreFromFile(keyPath, keyPassword.toCharArray());
    }

    private static Path projectPath(String relativePath) {
        Path currentDir = Paths.get("").toAbsolutePath();
        while (currentDir != null) {
            if (Files.isDirectory(currentDir.resolve("privacy_policy_controller"))
                    && Files.isDirectory(currentDir.resolve("web_gateway"))) {
                return currentDir.resolve(relativePath);
            }
            if (Files.isDirectory(currentDir.resolve("pcsig-alfred"))) {
                return currentDir.resolve("pcsig-alfred").resolve(relativePath);
            }
            currentDir = currentDir.getParent();
        }
        return Paths.get(relativePath);
    }

    public static byte[] minKey(byte[] key) {
        byte[] out = new byte[32];
        System.arraycopy(key, 0, out, 0, out.length);
        for (int i = out.length; i < key.length; i++) {
            out[i - out.length] ^= key[i];
        }
        return out;
    }


    public byte[] keyAgreement(String usrName, byte[] receivedPubKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519");

        assert this.usrName.equals(usrName);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] sendPubKeyBytes = publicKey.getEncoded();

        KeyAgreement keyAgreement = KeyAgreement.getInstance("X25519");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(KeyFactory.getInstance("X25519")
                .generatePublic(new X509EncodedKeySpec(receivedPubKeyBytes)), true);

        this.sharedSecretKey = keyAgreement.generateSecret();

        return sendPubKeyBytes;
    }


    public static byte[] decryptAESGcm(byte[] key, byte[] encData) throws InvalidKeyException, BadPaddingException,
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] ivByte = new byte[NONCE_SIZE];
        System.arraycopy(encData, 0, ivByte, 0, ivByte.length);

        GCMParameterSpec gcmParams = new GCMParameterSpec(cipher.getBlockSize() * 8, ivByte);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, gcmParams);
        return cipher.doFinal(encData, ivByte.length, encData.length - ivByte.length);
    }

    public boolean storeStreamKey(String usrName, long streamId, byte[] encStreamKey) throws CouldNotStoreException,
            QueryFailedException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        byte[] streamKey = decryptAESGcm(sharedSecretKey, encStreamKey);
        SecretKey sKey = new SecretKeySpec(streamKey, "AES");

        this.keyStore.storeStreamKey(usrName + streamId, sKey);
        this.keyStore.syncKeystore(true);
        return true;
    }

    public boolean deleteStreamKey(String usrName, long streamId) throws CouldNotStoreException, QueryFailedException {
        this.keyStore.syncKeystore(true);
        return true;
    }

    public static Stream getStream(long streamId) {
        FrontEndSQL sql = new FrontEndSQL();
        return sql.getStream(streamId);
    }

    public boolean addPrivacyPolicy(PrivacyPolicy pp) throws Exception {
        return true;
    }
    public boolean deletePrivacyPolicy(String ownerUsrName, long privacyPolicyId) throws Exception {
        return true;
    }
    public boolean addFederationPolicy(FederationPolicy fp) throws Exception {
        return true;
    }
    public boolean deleteFederationPolicy(String ownerUsrName, long fPolicyId) throws Exception {
        return true;
    }

    public PrivacyPolicy getPrivacyPolicy(long policyId) throws Exception {
        FrontEndSQL sql = new FrontEndSQL();
        return sql.getPrivacyPolicy(policyId);
    }
    public FederationPolicy getFederationPolicy(long policyId) throws Exception {
        FrontEndSQL sql = new FrontEndSQL();
        return sql.getFederationPolicy(policyId);
    }

    public StreamKeyManager getStreamKeyManager(long streamId) throws CouldNotReceiveException, InvalidQueryException {
        int keyTreeDepth = 31;
        String keyId = usrName + streamId;
        SecretKey streamKey = keyStore.receiveStreamKey(keyId);
        if (streamKey == null) {
            streamKey = recoverStreamKeyFromOwnerKeystore(keyId, streamId);
        }
        if (streamKey == null) {
            throw new InvalidQueryException("控制器缺少流主密钥：" + keyId + "，请用 owner 重新上传/重建该数据流");
        }
        StreamKeyManager skm = new StreamKeyManager(
                streamKey.getEncoded(), keyTreeDepth);
        return skm;
    }

    private SecretKey recoverStreamKeyFromOwnerKeystore(String keyId, long streamId)
            throws InvalidQueryException, CouldNotReceiveException {
        Path ownerKeyPath = projectPath("traffic_access_core/src/main/java/key/" + usrName + "KeyStore.jks");
        try {
            if (!Files.isRegularFile(ownerKeyPath)) {
                return null;
            }
            KeyStore ownerKeyStore = KeyStore.getInstance("pkcs12");
            try (java.io.FileInputStream input = new java.io.FileInputStream(ownerKeyPath.toFile())) {
                ownerKeyStore.load(input, OWNER_KEYSTORE_PASSWORD.toCharArray());
            }
            SecretKey ownerKey = (SecretKey) ownerKeyStore.getKey(keyId, "".toCharArray());
            if (ownerKey == null) {
                return null;
            }
            keyStore.storeStreamKey(keyId, ownerKey);
            keyStore.syncKeystore(true);
            LOGGER.warn("Recovered missing controller stream key {} for owner {} stream {}", keyId, usrName, streamId);
            return ownerKey;
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException |
                 UnrecoverableKeyException | CouldNotStoreException | QueryFailedException e) {
            throw new InvalidQueryException("无法从 owner keystore 恢复控制器流主密钥 " + keyId + ": " + e.getMessage());
        }
    }

    public boolean checkRequest(PrivacyPolicy privacyPolicy, Request request){
        if (privacyPolicy == null || request == null) {
            return false;
        }
        return (privacyPolicy.getConsumerUsrName().equals(request.getConsumerUsrName())
                && privacyPolicy.getPrivacyPolicyId() == request.getPrivacyPolicyID()
                && privacyPolicy.getStartTime().getTime() <= request.getStartTime().getTime()
                && privacyPolicy.getEndTime().getTime() >= request.getEndTime().getTime()
                && privacyPolicy.getMinGranularity() <= request.getGranularity());
    }

    public String explainRequestCheckFailure(PrivacyPolicy privacyPolicy, Request request) {
        if (privacyPolicy == null) {
            return "未找到对应策略";
        }
        if (request == null) {
            return "查询请求为空";
        }
        if (!privacyPolicy.getConsumerUsrName().equals(request.getConsumerUsrName())) {
            return "消费者不匹配：策略=" + privacyPolicy.getConsumerUsrName() + "，请求=" + request.getConsumerUsrName();
        }
        if (privacyPolicy.getPrivacyPolicyId() != request.getPrivacyPolicyID()) {
            return "策略ID不匹配：策略=" + privacyPolicy.getPrivacyPolicyId() + "，请求=" + request.getPrivacyPolicyID();
        }
        if (privacyPolicy.getStartTime().getTime() > request.getStartTime().getTime()) {
            return "查询起始时间早于授权起始时间：策略=" + privacyPolicy.getStartTime().getTime() + "，请求=" + request.getStartTime().getTime();
        }
        if (privacyPolicy.getEndTime().getTime() < request.getEndTime().getTime()) {
            return "查询结束时间晚于授权结束时间：策略=" + privacyPolicy.getEndTime().getTime() + "，请求=" + request.getEndTime().getTime();
        }
        if (privacyPolicy.getMinGranularity() > request.getGranularity()) {
            return "查询粒度小于授权下限：策略=" + privacyPolicy.getMinGranularity() + "，请求=" + request.getGranularity();
        }
        return "未知策略校验失败";
    }

    public Token createTokenForRequest(PrivacyPolicy privacyPolicy, Request request) throws CouldNotReceiveException, InvalidQueryException {
        if (checkRequest(privacyPolicy, request)){
            Stream stream = getStream(privacyPolicy.getStreamId());
            StreamKeyManager skm = getStreamKeyManager(stream.getId());
            KeyDerivationTree tree = skm.getKeyDerivationTree();
            long startKeyId = TimeUtil.getChunkIdAtTime(stream, request.getStartTime().getTime());
            long endKeyId = (TimeUtil.getChunkIdAtTime(stream, request.getEndTime().getTime()) + 1);
            ArrayList<SeedNode> seedNodes = tree.revealSeeds(startKeyId,endKeyId);
            int treeDepth = tree.getDepth();
            long tokenID = privacyPolicy.getPrivacyPolicyId();
            Token token = new Token(tokenID, usrName, stream.getId(), stream.getStartDate(),
                    stream.getChunkSize(), request.getGranularity(), seedNodes, treeDepth, skm.getMacKey());
            return token;
        }
        return null;
    }

    public long getEdgeKey(IPRG prg, byte[] seedFrom, byte[] seedTo, long metadataID){
        long keyFrom = KeyUtil.deriveKey(prg, seedFrom, true, metadataID);
        long keyTo = KeyUtil.deriveKey(prg, seedTo, true, metadataID);
        return keyFrom-keyTo;
    }
    public ArrayList<Long> getEdgeKeys(long streamId, long fromTime, long toTime) throws CouldNotReceiveException, InvalidQueryException {
        Stream stream = getStream(streamId);
        StreamKeyManager skm = getStreamKeyManager(streamId);
        KeyDerivationTree tree = skm.getKeyDerivationTree();
        long startId = TimeUtil.getChunkIdAtTime(stream, fromTime);
        long endId = (TimeUtil.getChunkIdAtTime(stream, toTime) + 1);
        byte[] seedFrom = tree.getSeed(startId);
        byte[] seedTo = tree.getSeed(endId);
        IPRG prg = tree.getPRG();
        ArrayList<Long> keys = new ArrayList<>();
        for (int metaId = 0; metaId < 9; metaId++) {
            keys.add(getEdgeKey(prg, seedFrom, seedTo, metaId));
        }
        return keys;
    }

    public Pair<Long, Long> getSteamInfo(long streamId) throws CouldNotReceiveException {
        Stream stream = getStream(streamId);
        return Pair.of(stream.getStartDate().getTime(), stream.getChunkSize());
    }

    public static class KeyAndMac{
        public long key;
        public BigInteger mac;
    }


}
