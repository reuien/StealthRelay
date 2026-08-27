package producerNettyServer;

import exceptions.*;
import keyManagement.StreamKeyManager;
import dataServerInterface.DataServerInterface;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlConnect.FrontEndSQL;
import state.ProducerKeyStore;
import state.ProducerProfile;
import streamHandling.*;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class DataProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProducer.class);
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 1101;
    private static final Path CSV_UPLOAD_ROOT = projectPath("data/uploads");
    private final Random rand = new Random();
    private final String owner;
    private final long producerId;
    private final String producerName;
    private final String PRODUCER_ADDRESS;
    private final int PRODUCER_PORT;
    private String keyStorePassword;
    private String keyStorePath;
    private byte[] sharedSecretKey;
    private static final int NONCE_SIZE = 12;
    public ProducerKeyStore keyStore;
    public ProducerProfile profile;
    public DataServerInterface serverInterface;
    public DataProducer(String owner, long producerId, String producerName, String producerAddress, int producerPort,
                        ProducerKeyStore keyStore, ProducerProfile profile) {
        this.owner = owner;
        this.producerId = producerId;
        this.producerName = producerName;
        this.PRODUCER_ADDRESS = producerAddress;
        this.PRODUCER_PORT = producerPort;
        this.keyStore = keyStore;
        this.profile = profile;
        try {
            this.serverInterface = new DataServerInterface(SERVER_ADDRESS, SERVER_PORT);
        } catch (IOException e) {
            LOGGER.error("无法连接服务器", e);
            throw new RuntimeException("无法连接服务器" + e.getMessage());
        }
    }

    public String getOwner() {
        return owner;
    }

    public long getProducerId() {
        return producerId;
    }

    public String getProducerName() {
        return producerName;
    }

    public static boolean registerProducer(String owner, long producerId, String producerName, String producerAddress,
                                           int producerPort) throws CertificateException, KeyStoreException,
            NoSuchAlgorithmException, IOException, CouldNotStoreException, QueryFailedException {
        System.out.println("start");
        String profilePath = projectPath("traffic_stream_producer/src/main/java/key/" + owner + "_Producer_" + producerId + "_Profile.jks").toString();
        String keyPath = projectPath("traffic_stream_producer/src/main/java/key/" + owner + "_Producer_" + producerId + "_KeyStore.jks").toString();
        String keyPassword = owner + "_Producer_" + producerId;
        ProducerProfile profile = new ProducerProfile(owner, producerId, producerName, producerAddress, producerPort,
                profilePath, keyPath, SERVER_ADDRESS, SERVER_PORT);
        ProducerKeyStore pks = ProducerKeyStore.createLocalKeystore(keyPath, keyPassword.toCharArray());
        return profile.syncProfile() && pks.syncKeystore(true);
    }

    public static DataProducer getDataProducer(String owner, long producerId) throws IOException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException {
        String profilePath = projectPath("traffic_stream_producer/src/main/java/key/" + owner + "_Producer_" + producerId + "_Profile.jks").toString();
        String keyPath = projectPath("traffic_stream_producer/src/main/java/key/" + owner + "_Producer_" + producerId + "_KeyStore.jks").toString();
        String keyPassword = owner + "_Producer_" + producerId;
        ProducerProfile profile = ProducerProfile.localProfileFromFile(profilePath);
        ProducerKeyStore pks = ProducerKeyStore.localKeystoreFromFile(keyPath, keyPassword.toCharArray());
        return new DataProducer(owner, producerId, profile.getProducerName(), profile.getProducerAddress(), profile.getProducerPort(), pks, profile);
    }

    public void refreshProfile() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        String profilePath = projectPath("traffic_stream_producer/src/main/java/key/" + owner + "_Producer_" + producerId + "_Profile.jks").toString();
        String keyPath = projectPath("traffic_stream_producer/src/main/java/key/" + owner + "_Producer_" + producerId + "_KeyStore.jks").toString();
        String keyPassword = owner + "_Producer_" + producerId;
        this.keyStore = ProducerKeyStore.localKeystoreFromFile(keyPath, keyPassword.toCharArray());
        this.profile = ProducerProfile.localProfileFromFile(profilePath);
    }

    public static byte[] minKey(byte[] key) {
        byte[] out = new byte[32];
        System.arraycopy(key, 0, out, 0, out.length);
        for (int i = out.length; i < key.length; i++) {
            out[i - out.length] ^= key[i];
        }
        return out;
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public byte[] keyAgreement(String usrName, byte[] receivedPubKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519");

        assert this.owner.equals(usrName);
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
        Cipher cipher;
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

        assert this.owner.equals(usrName);
        byte[] streamKey = decryptAESGcm(sharedSecretKey, encStreamKey);
        SecretKey sKey = new SecretKeySpec(streamKey, "AES");

        this.keyStore.storeStreamKey(usrName + streamId, sKey);
        this.keyStore.syncKeystore(true);
        return true;
    }

    public boolean createStreamProducer() throws CouldNotStoreException, IOException {

        SecretKey streamMasterKey;
        try {
            streamMasterKey = KeyGenerator.getInstance("AES").generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new CouldNotStoreException("无法为流创建主密钥");
        }
        long streamId = serverInterface.createStream(owner);
        try {
            keyStore.storeStreamKey(owner + streamId, streamMasterKey);
            keyStore.syncKeystore(true);
        } catch (Exception e) {
            serverInterface.deleteStream(owner, streamId);
            throw new CouldNotStoreException("无法保存主密钥");
        }

        return true;
    }

    public boolean deleteStreamProducer(long streamId) throws IOException, CouldNotStoreException {
        if(serverInterface.deleteStream(owner, streamId)){
            return true;
        }
        return false;
    }

    public Stream getStream(long streamId) {
        FrontEndSQL sql = new FrontEndSQL();
        return sql.getStream(streamId);
    }

    public StreamKeyManager getStreamKeyManager(long streamId) throws CouldNotReceiveException, InvalidQueryException {
        int keyTreeDepth = 31;
        StreamKeyManager skm = new StreamKeyManager(
                keyStore.receiveStreamKey(owner + streamId).getEncoded(), keyTreeDepth);
        return skm;
    }

    public UploadManager getUploadManager(long streamId) throws CouldNotReceiveException, InvalidQueryException, CouldNotStoreException {
        UploadManager um = new UploadManager(owner, getStream(streamId), getStreamKeyManager(streamId), serverInterface);
        return um;
    }

    public void uploadData(long streamId) throws CouldNotReceiveException, InvalidQueryException, WriteException {
        UploadManager um = getUploadManager(streamId);
        Stream st = getStream(streamId);
        if (uploadCsvDataIfPresent(um, st)) {
            return;
        }
        long start = st.getStartDate().getTime();
        long end = st.getEndDate().getTime();
        long size = st.getChunkSize();

        double mean = 75.0;
        double stdDev = 15.0;

        for (long stl = start; stl <= end; stl += size) {
            for (int i = 0; i < 2; i++) {
                double normalValue = mean + stdDev * rand.nextGaussian();
                int simulatedHeartRate = (int) Math.max(0, Math.min(110, normalValue));
                DataPoint dp = new DataPoint(new Date(stl + i), simulatedHeartRate);
                um.writeDataPointToStream(dp);
            }
        }
        um.flush();
    }

    private boolean uploadCsvDataIfPresent(UploadManager um, Stream stream) throws WriteException {
        Path csvPath = getCsvPath(stream.getId());
        if (!Files.isRegularFile(csvPath)) {
            return false;
        }
        List<DataPoint> dataPoints = readCsvDataPoints(csvPath, stream);
        if (dataPoints.isEmpty()) {
            return false;
        }
        for (DataPoint dataPoint : dataPoints) {
            um.writeDataPointToStream(dataPoint);
        }
        try {
            um.flush();
        } catch (CouldNotStoreException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Uploaded {} CSV data points for stream {} from {}", dataPoints.size(), stream.getId(), csvPath);
        return true;
    }

    private List<DataPoint> readCsvDataPoints(Path csvPath, Stream stream) throws WriteException {
        List<DataPoint> dataPoints = new ArrayList<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new WriteException("读取 CSV 文件失败: " + csvPath + "，" + e.getMessage());
        }
        long streamStart = stream.getStartDate().getTime();
        long streamEnd = stream.getEndDate().getTime();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length < 2) {
                throw new WriteException("CSV 第 " + (i + 1) + " 行格式错误，应为 timestamp,value");
            }
            if (i == 0 && isHeader(parts[0], parts[1])) {
                continue;
            }
            long timestamp = parseTimestamp(parts[0].trim(), i + 1);
            if (timestamp < streamStart || timestamp > streamEnd) {
                continue;
            }
            long value = parseValue(parts[1].trim(), i + 1);
            dataPoints.add(new DataPoint(new Date(timestamp), value));
        }
        dataPoints.sort(Comparator.naturalOrder());
        return dataPoints;
    }

    private Path getCsvPath(long streamId) {
        return CSV_UPLOAD_ROOT.resolve(safePathPart(owner)).resolve(streamId + ".csv");
    }

    private static boolean isHeader(String timestamp, String value) {
        String left = timestamp.trim().toLowerCase();
        String right = value.trim().toLowerCase();
        return left.contains("time") || left.contains("date") || right.contains("value");
    }

    private static long parseTimestamp(String raw, int lineNumber) throws WriteException {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
        }
        String normalized = raw.replace('/', '-').replace('T', ' ');
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(normalized, formatter);
                return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return OffsetDateTime.parse(raw).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        throw new WriteException("CSV 第 " + lineNumber + " 行时间格式错误: " + raw);
    }

    private static long parseValue(String raw, int lineNumber) throws WriteException {
        try {
            return Math.round(Double.parseDouble(raw));
        } catch (NumberFormatException e) {
            throw new WriteException("CSV 第 " + lineNumber + " 行数值格式错误: " + raw);
        }
    }

    private static String safePathPart(String value) {
        return value == null ? "unknown" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static Path projectPath(String relativePath) {
        Path currentDir = Paths.get("").toAbsolutePath();
        while (currentDir != null) {
            if (Files.isDirectory(currentDir.resolve("traffic_stream_producer"))
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




}
