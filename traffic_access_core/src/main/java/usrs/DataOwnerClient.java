package usrs;

import crypto.MACCheckFailed;
import exceptions.*;
import keyManagement.StreamKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.DOKeyStore;
import streamHandling.*;
import streamHandling.Chunk;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class DataOwnerClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataOwnerClient.class);
    private final Random rand = new Random();
    private String usrId;
    private String usrName;
    private String keyStorePassword;
    private String pathKey;
    private DataOwner dataOwner;
    public long curProducerId;
    public String curProducerName;


    public DataOwnerClient(String usrId, String usrName, String keyStorePassword) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, CouldNotStoreException, QueryFailedException {
        this.usrId = usrId;
        this.usrName = usrName;
        this.keyStorePassword = keyStorePassword;
        this.pathKey = projectPath("traffic_access_core/src/main/java/key/" + usrName + "KeyStore.jks");
        this.dataOwner = createDataOwner();

    }

    private static String projectPath(String relativePath) {
        Path currentDir = Paths.get("").toAbsolutePath();
        while (currentDir != null) {
            if (Files.isDirectory(currentDir.resolve("traffic_access_core"))
                    && Files.isDirectory(currentDir.resolve("web_gateway"))) {
                return currentDir.resolve(relativePath).toString();
            }
            if (Files.isDirectory(currentDir.resolve("pcsig-alfred"))) {
                return currentDir.resolve("pcsig-alfred").resolve(relativePath).toString();
            }
            currentDir = currentDir.getParent();
        }
        return Paths.get(relativePath).toString();
    }

    private DataOwner createDataOwner() throws CertificateException, KeyStoreException, NoSuchAlgorithmException,
            IOException, CouldNotStoreException, QueryFailedException {
        DOKeyStore keyStore;
        if (!(new File(pathKey)).exists()) {
            keyStore = DOKeyStore.createLocalKeystore(pathKey, keyStorePassword.toCharArray());
            keyStore.syncKeystore(true);
        }else {
            keyStore = DOKeyStore.localKeystoreFromFile(pathKey, keyStorePassword.toCharArray());
        }
        DataOwner dataOwner = new DataOwner(usrName, keyStore);
        return dataOwner;
    }

    public boolean registerProducer(long producerId, String producerName, String producerAddress, int producerPort) throws IOException {
        return dataOwner.registerProducer(producerId, producerName, producerAddress, producerPort);
    }

    public boolean linkProducer(long producerId, String producerName) throws IOException {
        this.setCurProducerId(producerId);
        this.setCurProducerName(producerName);
        return dataOwner.linkProducer(producerId, producerName);
    }

    public long createStream(String streamName, String description, Date startDate, Date endDate, TimeUtil.Precision chunkSize,
                             List<TimeUtil.Precision> resolutionLevels) throws CouldNotStoreException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return dataOwner.createStreamDO(curProducerId, streamName, description, startDate, endDate, chunkSize, resolutionLevels).getId();
    }

    public boolean deleteStream(long streamId) throws IOException, CouldNotStoreException {
        return dataOwner.deleteStreamDO(streamId);
    }

    public Stream getStream(long streamId) throws CouldNotReceiveException, CouldNotStoreException {
        return dataOwner.getStream(streamId);
    }

    public void producerUploadData(long streamId) throws CouldNotReceiveException,
            CouldNotStoreException, IOException {
        dataOwner.producerUploadData(curProducerId, streamId);
    }

    public void producerUploadDataLive(long streamId) throws CouldNotReceiveException,
            CouldNotStoreException, IOException {
        dataOwner.producerUploadDataLive(curProducerId, streamId);
    }
    public void uploadData(long streamId) throws CouldNotReceiveException, InvalidQueryException, WriteException {
        UploadManager um = dataOwner.getUploadManager(streamId);
        Stream st = dataOwner.getStream(streamId);
        long start = st.getStartDate().getTime();
        long end = st.getEndDate().getTime();
        long size = st.getChunkSize();

        for (long stl = start; stl <= end; stl+=size){
            DataPoint dp = new DataPoint(new Date(stl), rand.nextInt(40)+60);
                    //System.out.println((new DataPoint(new Date((start+size*i)), i)).toString());
            um.writeDataPointToStream(dp);
            //System.out.println(dp.getTimestamp());
        }
/*        int dataN = (int) ((end - start) / size);
        for (int i = 0; i < dataN; i++) {
            DataPoint dp = new DataPoint(new Date((start+size*i)), rand.nextInt(40)+60);
            //System.out.println((new DataPoint(new Date((start+size*i)), i)).toString());
            System.out.println("add");
            um.writeDataPointToStream(dp);
            System.out.println(dp.getTimestamp());
            //um.writeDataPointToStream(new DataPoint(new Date((start+size*i+2)), rand.nextInt(40)+60));
        }*/
        um.flush();
    }

    public void uploadDataPoints(long streamId, List<DataPoint> dataPoints)
            throws CouldNotReceiveException, InvalidQueryException, WriteException {
        UploadManager uploadManager = dataOwner.getUploadManager(streamId);
        for (DataPoint dataPoint : dataPoints) {
            uploadManager.writeDataPointToStream(dataPoint);
        }
        uploadManager.flush();
    }

    public List<Chunk> getChunks(long streamId, Date startTime, Date endTime) throws CouldNotReceiveException, CouldNotStoreException {
        Stream stream = getStream(streamId);
        long chunkIdFrom = TimeUtil.getChunkIdAtTime(stream, startTime.getTime());
        long chunkIdTo = TimeUtil.getChunkIdAtTime(stream, endTime.getTime());
        List<Chunk> chunks;
        try {
            chunks = dataOwner.getChunksOwner(streamId, chunkIdFrom, chunkIdTo);
        } catch (CouldNotReceiveException e) {
            LOGGER.error("接收数据块时出错", e);
            return null;
        } catch (Exception e) {
            LOGGER.error("解密区块时出错", e);
            return null;
        }
        return chunks;
    }

    public List<Chunk> getChunksID(long streamId, long chunkIdFrom, long chunkIdTo) throws CouldNotReceiveException {
        List<Chunk> chunks;
        try {
            chunks = dataOwner.getChunksOwner(streamId, chunkIdFrom, chunkIdTo);
            return chunks;
        } catch (CouldNotReceiveException e) {
            LOGGER.error("接收数据块时出错", e);
            return null;
        } catch (Exception e) {
            LOGGER.error("解密区块时出错", e);
            return null;
        }
    }

    public List<Digest> getDigests(long streamId, Date startTime, Date endTime, int granularity) throws CouldNotReceiveException, CouldNotStoreException {
        Stream stream = getStream(streamId);
        long chunkIdFrom = TimeUtil.getChunkIdAtTime(stream, startTime.getTime());
        long chunkIdTo = TimeUtil.getChunkIdAtTime(stream, endTime.getTime());
        List<Digest> digests;
        try {
            digests = dataOwner.getDigestsOwner(streamId, chunkIdFrom, chunkIdTo, granularity);
        } catch (Exception e) {
            LOGGER.error("出错", e);
            return null;
        }
        return digests;
    }

    public List<Digest> getDigestsNew(long streamId, Date startTime, Date endTime, int granularity) throws CouldNotReceiveException, CouldNotStoreException {
        Stream stream = getStream(streamId);
        long chunkIdFrom = TimeUtil.getChunkIdAtTime(stream, startTime.getTime());
        long chunkIdTo = TimeUtil.getChunkIdAtTime(stream, endTime.getTime());
        List<Digest> digests;
        try {
            digests = dataOwner.getNewDigestsOwner(streamId, chunkIdFrom, chunkIdTo, granularity);
        } catch (Exception e) {
            LOGGER.error("出错", e);
            return null;
        }
        return digests;
    }

    public Digest getAllNewDigestsOwner(long streamId, Date startTime, Date endTime) throws
            InvalidQueryException, CouldNotReceiveException, IOException, MACCheckFailed, CouldNotStoreException {
        Stream stream = getStream(streamId);
        long chunkIdFrom = TimeUtil.getChunkIdAtTime(stream, startTime.getTime());
        long chunkIdTo = TimeUtil.getChunkIdAtTime(stream, endTime.getTime());
        Digest digest = dataOwner.getAllNewDigestsOwner(streamId, chunkIdFrom, chunkIdTo);
        return digest;
    }

    public PrivacyPolicy createPrivacyPolicy(String consumer, long streamId, Date startTime, Date endTime, int minGranularity) throws Exception {
        return dataOwner.createPrivacyPolicy(consumer, streamId, startTime, endTime, minGranularity);
    }

    public FederationPolicy createFederationPolicy(String consumer, long streamId, Date startTime, Date endTime) throws Exception {
        return dataOwner.createFederationPolicy(consumer, streamId, startTime, endTime);
    }

    public String getUsrId() {
        return usrId;
    }

    public String getUsrName() {
        return usrName;
    }

    public void setCurProducerId(long curProducerId) {
        this.curProducerId = curProducerId;
    }

    public void setCurProducerName(String curProducerName) {
        this.curProducerName = curProducerName;
    }

    public long getCurProducerId() {
        return curProducerId;
    }

    public String getCurProducerName() {
        return curProducerName;
    }
}
