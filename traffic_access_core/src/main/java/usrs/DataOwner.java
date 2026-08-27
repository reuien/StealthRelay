package usrs;

import controllerInterface.ControllerInterface;
import crypto.MACCheckFailed;
import exceptions.CouldNotReceiveException;
import exceptions.CouldNotStoreException;
import exceptions.InvalidQueryException;
import exceptions.QueryFailedException;
import keyManagement.StreamKeyManager;
import dataServerInterface.DataServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import producerInterface.ProducerInterface;
import sqlConnect.FrontEndSQL;
import state.DOKeyStore;
import streamHandling.*;

import javax.crypto.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class DataOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataOwner.class);
    private final String SERVER_ADDRESS = "127.0.0.1";
    private final int SERVER_PORT = 1101;
    private final String CONTROLLER_ADDRESS = "127.0.0.1";
    private final int CONTROLLER_PORT = 1102;
    private final String PRODUCER_ADDRESS = "127.0.0.1";
    private final int PRODUCER_PORT = 1234;
    private final Random rand = new Random();
    private final String usrName;
    public final DOKeyStore keyStore;
    public final DataServerInterface serverInterface;
    public final ControllerInterface controllerInterface;
    public final ProducerInterface producerInterface;
    public DataOwner(String usrName, DOKeyStore keyStore) {
        this.usrName = usrName;
        this.keyStore = keyStore;
        try {
            this.serverInterface = new DataServerInterface(SERVER_ADDRESS, SERVER_PORT);
            this.controllerInterface = new ControllerInterface(CONTROLLER_ADDRESS, CONTROLLER_PORT);
            this.producerInterface = new ProducerInterface(PRODUCER_ADDRESS, PRODUCER_PORT);
        } catch (IOException e) {
            // TODO: Better error
            LOGGER.error("无法连接服务器", e);
            throw new RuntimeException("无法连接服务器" + e.getMessage());
        }
    }

    public boolean registerProducer(long producerId, String producerName, String producerAddress, int producerPort) throws IOException {
        return producerInterface.registerProducer(usrName, producerId, producerName, producerAddress, producerPort);
    }

    public boolean linkProducer(long producerId, String producerName) throws IOException {
        return producerInterface.linkProducer(usrName, producerId, producerName);
    }

    public Stream createStreamDO(long producerId, String streamName, String description, Date startDate, Date endDate, TimeUtil.Precision precision,
                                 List<TimeUtil.Precision> resolutionLevels) throws CouldNotStoreException, IOException {
        // 1、创建kafka topic  2、本地保存密钥  3、控制器保存密钥  4、生产者保存密钥
        SecretKey streamMasterKey;
        try {
            streamMasterKey = KeyGenerator.getInstance("AES").generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new CouldNotStoreException("无法为流创建主密钥");
        }
        long streamId = serverInterface.createStream(usrName);
        try {
            keyStore.storeStreamKey(usrName + streamId, streamMasterKey);
            keyStore.syncKeystore(true);
            //控制器
            controllerInterface.CreateStream(usrName, streamId, streamMasterKey);
            //生产者
            producerInterface.CreateStream(usrName, producerId, streamId, streamMasterKey);
        } catch (Exception e) {
            serverInterface.deleteStream(usrName, streamId);
            throw new CouldNotStoreException("无法保存主密钥");
        }
        //eg: chunkSize = TimeUtil.Precision.ONE_SECOND;
        //    resolutionLevels = Collections.singletonList(TimeUtil.Precision.TEN_SECONDS);
        Stream stream = new Stream(streamId, streamName, description, startDate, endDate, precision, resolutionLevels);
        return stream;
    }

    public boolean deleteStreamDO(long streamId) throws IOException, CouldNotStoreException {
        if(serverInterface.deleteStream(usrName, streamId)){
            return true;
        }
        return false;
    }

    public Stream getStream(long streamId) throws CouldNotReceiveException, CouldNotStoreException {
        //profile.syncProfile(true);
        //  sql
        FrontEndSQL sql = new FrontEndSQL();
        return sql.getStream(streamId);
    }

    public void producerUploadData(long producerId, long streamId) throws CouldNotReceiveException,
            CouldNotStoreException, IOException {
        Stream st = getStream(streamId);
        producerInterface.uploadData(usrName, producerId, streamId, st.getDescription(), st.getStartDate().getTime(),
                st.getEndDate().getTime(), st.getChunkSize(), st.getResolutionLevels().get(0).getMillis());
    }

    public void producerUploadDataLive(long producerId, long streamId) throws CouldNotReceiveException,
            CouldNotStoreException, IOException {
        Stream st = getStream(streamId);
        producerInterface.uploadDataLive(usrName, producerId, streamId, st.getDescription(), st.getStartDate().getTime(),
                st.getEndDate().getTime(), st.getChunkSize(), st.getResolutionLevels().get(0).getMillis());
    }

    public StreamKeyManager getStreamKeyManager(long streamId) throws CouldNotReceiveException, InvalidQueryException {
        int keyTreeDepth = 31;
        StreamKeyManager skm = new StreamKeyManager(
                keyStore.receiveStreamKey(usrName + streamId).getEncoded(), keyTreeDepth);
        return skm;
    }

    public UploadManager getUploadManager(long streamId) throws CouldNotReceiveException, InvalidQueryException, CouldNotStoreException {
        UploadManager um = new UploadManager(usrName, getStream(streamId), getStreamKeyManager(streamId), serverInterface);
        return um;
    }

    public List<Chunk> getChunksOwner(long streamId, long chunkIdFrom, long chunkIdTo) throws CouldNotReceiveException,
            InvalidQueryException, QueryFailedException, CouldNotStoreException {
        /*System.out.println(chunkIdFrom);
        System.out.println(chunkIdTo);*/
        Stream stream = getStream(streamId);
        StreamKeyManager skm = getStreamKeyManager(streamId);
        List<Chunk> chunks = new ArrayList<>();
        for (EncryptedChunk encryptedChunk : serverInterface.getChunks(usrName, streamId, chunkIdFrom, chunkIdTo)) {
            chunks.add(new Chunk(streamId, stream.getStartDate().getTime(), stream.getChunkSize(), encryptedChunk.getChunkId(), encryptedChunk.getPayload(), skm));
        }
        return chunks;
    }

    public List<Digest> getDigestsOwner(long streamId, long chunkIdFrom, long chunkIdTo, int granularity) throws
            InvalidQueryException, MACCheckFailed, CouldNotReceiveException, CouldNotStoreException {
        StreamKeyManager skm = getStreamKeyManager(streamId);
        List<Digest> digests = new ArrayList<>();
        for (Digest digest : serverInterface.getStatisticalData(usrName, streamId, chunkIdFrom, chunkIdTo, granularity)){
            digests.add(digest.decrypt(skm));
        }
        return digests;
    }

    public List<Digest> getNewDigestsOwner(long streamId, long chunkIdFrom, long chunkIdTo, int granularity) throws
            InvalidQueryException, MACCheckFailed, CouldNotReceiveException, CouldNotStoreException {
        StreamKeyManager skm = getStreamKeyManager(streamId);
        List<Digest> digests = new ArrayList<>();
        for (Digest digest : serverInterface.getStatisticalNewDigest(usrName, streamId, chunkIdFrom, chunkIdTo, granularity)){
            digests.add(digest.decryptNew(skm));
        }
        return digests;
    }

    public Digest getAllNewDigestsOwner(long streamId, long chunkIdFrom, long chunkIdTo) throws
            InvalidQueryException, CouldNotReceiveException, IOException, MACCheckFailed {
        StreamKeyManager skm = getStreamKeyManager(streamId);
        Digest digest = serverInterface.getStatisticAllNew(usrName, streamId, chunkIdFrom, chunkIdTo);
        digest.decryptNew(skm);
        return digest;
    }

    public PrivacyPolicy createPrivacyPolicy(String consumerUsrName, long streamId, Date startTime, Date endTime, long minGranularity) throws Exception {
        Stream correspondingStream = getStream(streamId);
        long privacyPolicyID = rand.nextLong();
        if(controllerInterface.createPolicy(consumerUsrName, usrName, privacyPolicyID, correspondingStream.getId(), startTime, endTime, minGranularity)){
            return new PrivacyPolicy(consumerUsrName, usrName, privacyPolicyID, correspondingStream, startTime, endTime, minGranularity);
        }
        LOGGER.error("创建隐私策略失败");
        return null;
    }

    public FederationPolicy createFederationPolicy(String consumerUsrName, long streamId, Date startTime, Date endTime) throws IOException {
        long federationPolicyID = rand.nextLong();
        if(controllerInterface.createFederationPolicy(consumerUsrName, usrName, federationPolicyID, streamId, startTime.getTime(), endTime.getTime())){
            return new FederationPolicy(consumerUsrName, usrName, federationPolicyID, streamId, startTime.getTime(), endTime.getTime());
        }
        LOGGER.error("创建联邦策略失败");
        return null;
    }

}
