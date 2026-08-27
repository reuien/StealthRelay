package usrs;

import blk.StoreInterface;
import controllerInterface.ControllerInterface;
import crypto.MACCheckFailed;
import exceptions.CouldNotReceiveException;
import exceptions.InvalidQueryException;
import exceptions.QueryFailedException;
import keyManagement.StreamKeyManager;
import dataServerInterface.DataServerInterface;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statistics.StaticticForFederation;
import statistics.StaticticForFederationNew;
import streamHandling.*;

import java.io.IOException;
import java.util.*;

public class AcManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcManager.class);
    private final String SERVER_ADDRESS = "127.0.0.1";
    private final int SERVER_PORT = 1101;
    private final String CONTROLLER_ADDRESS = "127.0.0.1";
    private final int CONTROLLER_PORT = 1102;

    public final DataServerInterface dataServerInterface;
    public final ControllerInterface ControllerInterface;
    public final StoreInterface storeInterface = getStoreInterface();
    public AcManager() {
        try {
            this.dataServerInterface = new DataServerInterface(SERVER_ADDRESS, SERVER_PORT);
            this.ControllerInterface = new ControllerInterface(CONTROLLER_ADDRESS, CONTROLLER_PORT);
        } catch (IOException e) {
            // TODO: Better error
            LOGGER.error("无法连接服务器", e);
            throw new RuntimeException("无法连接服务器" + e.getMessage());
        }
    }

    public StoreInterface getStoreInterface(){
        boolean useLocalNetwork = false; // true表示使用本地网络，false表示使用Sepolia测试网络
        return new StoreInterface(useLocalNetwork);
    }
    public Map<Long, PrivacyPolicy> getPrivacyPolicies(String ownerName) {
        /*if (ownerName.equals(pcProfile.getUserName())){
            //storeInterface.queryData(ownerName, -1L);
            return pcProfile.getPrivacyPolicies();
        }*/
        return null;
    }

    public Map<Long, FederationPolicy> getFederationPolicies(String ownerName) {
        /*if (ownerName.equals(pcProfile.getUserName())){
            //storeInterface.queryData(ownerName, -1L);
            return pcProfile.getFederationPolicies();
        }*/
        return null;
    }


    public Request createRequest(String consumerName, String ownerUsrName, long streamID, Date startTime, Date endTime, long granularity){
        return new Request(consumerName, ownerUsrName, streamID, startTime, endTime, granularity);
    }

    public Token sendRequest(String consumerName, String ownerName, long policyId, long streamId, Date startTime, Date endTime, long granularity) throws IOException, ClassNotFoundException {
        return ControllerInterface.sendRequest(consumerName, ownerName, policyId, streamId, startTime, endTime, granularity);
    }

    public List<ChunkForDC> getChunksDC(Token tk, Date startTime, Date endTime) throws CouldNotReceiveException,
            InvalidQueryException, QueryFailedException {
        String stream = tk.getUsrName() + tk.getStreamID();
        Date startDate = tk.getStreamStartDate();
        long chunkSize = tk.getChunkSize();
        long chunkIdFrom = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, startTime.getTime());
        long chunkIdTo = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, endTime.getTime());

        StreamKeyManager skm = new StreamKeyManager(tk.getNodes(), tk.getMacKey(), tk.getTreeDepth());
        List<ChunkForDC> chunks = new ArrayList<>();
        for (EncryptedChunk encryptedChunk : dataServerInterface.getChunks(tk.getUsrName(), tk.getStreamID(), chunkIdFrom, chunkIdTo))
            chunks.add(new ChunkForDC(stream, startDate, chunkSize, encryptedChunk.getChunkId(), encryptedChunk.getPayload(), skm));
        return chunks;
    }

//    public List<Digest> getDigestsDC(Token tk, Date startTime, Date endTime, int granularity) throws
//            InvalidQueryException, MACCheckFailed {
//        String stream = tk.getUsrName() + tk.getStreamID();
//        Date startDate = tk.getStreamStartDate();
//        long chunkSize = tk.getChunkSize();
//        long chunkIdFrom = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, startTime.getTime());
//        long chunkIdTo = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, endTime.getTime());
//        StreamKeyManager skm = new StreamKeyManager(tk.getNodes(), tk.getMacKey(), tk.getTreeDepth());
//        List<Digest> digests = new ArrayList<>();
//        for (Digest digest : dataServerInterface.getStatisticalData(tk.getUsrName(), tk.getStreamID(),
//                chunkIdFrom, chunkIdTo, granularity)){
//            digests.add(digest.decrypt(skm));
//        }
//        return digests;
//    }

    public List<Digest> getNewDigestsDC(Token tk, Date startTime, Date endTime, int granularity) throws
            InvalidQueryException, MACCheckFailed {
        String stream = tk.getUsrName() + tk.getStreamID();
        Date startDate = tk.getStreamStartDate();
        long chunkSize = tk.getChunkSize();
        long chunkIdFrom = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, startTime.getTime());
        long chunkIdTo = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, endTime.getTime());
        StreamKeyManager skm = new StreamKeyManager(tk.getNodes(), tk.getMacKey(), tk.getTreeDepth());
        List<Digest> digests = new ArrayList<>();
        for (Digest digest : dataServerInterface.getStatisticalNewDigest(tk.getUsrName(), tk.getStreamID(),
                chunkIdFrom, chunkIdTo, granularity)){
            digests.add(digest.decryptNew(skm));
        }
        return digests;
    }
    public Digest getAllNewDigestsDC(Token tk, Date startTime, Date endTime) throws IOException, MACCheckFailed {
        Date startDate = tk.getStreamStartDate();
        long chunkSize = tk.getChunkSize();
        long chunkIdFrom = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, startTime.getTime());
        long chunkIdTo = TimeUtilForDC.getChunkIdAtTime(startDate, chunkSize, endTime.getTime());
        StreamKeyManager skm = new StreamKeyManager(tk.getNodes(), tk.getMacKey(), tk.getTreeDepth());
        Digest digest = dataServerInterface.getStatisticAllNew(tk.getUsrName(), tk.getStreamID(), chunkIdFrom, chunkIdTo);
        digest.decryptNew(skm);
        System.out.println(digest.toStringNew());
        return digest;
    }

    public FederationToken getFederationToken(String consumerName, ArrayList<Pair<String, Long>> nameAndStreamList, long fromTime, long toTime) throws IOException, ClassNotFoundException {
        FederationRequest fq = new FederationRequest(consumerName, nameAndStreamList, fromTime, toTime);
        FederationToken ft = ControllerInterface.getFederationToken(fq);
        if (ft == null){
            throw new RuntimeException("请求不符合策略！");
        }
        return ft;
    }

    public StaticticForFederation getFederationInfo(FederationToken fToken, ArrayList<Pair<String, Long>> nameAndStreamList, long fromTime, long toTime) throws IOException {

        List<Long> encRes = new ArrayList<>(Collections.nCopies(3, 0L));
        for (int i = 0; i < nameAndStreamList.size(); i++) {
            long chunkIdFrom = TimeUtilForDC.getChunkIdAtTime(new Date(fToken.getStreamStartTimeList().get(i)),
                    fToken.getChunkSizeList().get(i), fromTime);
            long chunkIdTo = TimeUtilForDC.getChunkIdAtTime(new Date(fToken.getStreamStartTimeList().get(i)),
                    fToken.getChunkSizeList().get(i), toTime);
            Digest encryptedDigest = dataServerInterface.getStatisticAll(nameAndStreamList.get(i).getLeft(),
                    nameAndStreamList.get(i).getRight(), chunkIdFrom, chunkIdTo);
            encRes.set(encryptedDigest.getSumId(), encRes.get(encryptedDigest.getSumId()) + encryptedDigest.getSum());
            encRes.set(encryptedDigest.getCountId(), encRes.get(encryptedDigest.getCountId()) + encryptedDigest.getCount());
            encRes.set(encryptedDigest.getSquareId(), encRes.get(encryptedDigest.getSquareId()) + encryptedDigest.getSquare());
        }
        List<Long> decRes = new ArrayList<>(Collections.nCopies(3, 0L));
        for (int i = 0; i < fToken.getFederationKeys().size(); i++) {
            long k = fToken.getFederationKeys().get(i);
            decRes.set(i, (encRes.get(i)-k));
            System.out.println("res"+i+": "+ decRes.get(i));
        }
        StaticticForFederation sff = new StaticticForFederation(decRes);
        return sff;
    }

    public StaticticForFederationNew getFederationInfoNew(FederationToken fToken, ArrayList<Pair<String, Long>> nameAndStreamList, long fromTime, long toTime) throws IOException {

        List<Long> encRes = new ArrayList<>(Collections.nCopies(9, 0L));
        for (int i = 0; i < nameAndStreamList.size(); i++) {
            long chunkIdFrom = TimeUtilForDC.getChunkIdAtTime(new Date(fToken.getStreamStartTimeList().get(i)),
                    fToken.getChunkSizeList().get(i), fromTime);
            long chunkIdTo = TimeUtilForDC.getChunkIdAtTime(new Date(fToken.getStreamStartTimeList().get(i)),
                    fToken.getChunkSizeList().get(i), toTime);
            Digest encryptedDigest = dataServerInterface.getStatisticAllNew(nameAndStreamList.get(i).getLeft(),
                    nameAndStreamList.get(i).getRight(), chunkIdFrom, chunkIdTo);
            encRes.set(encryptedDigest.getSumId(), encRes.get(encryptedDigest.getSumId()) + encryptedDigest.getSum());
            encRes.set(encryptedDigest.getCountId(), encRes.get(encryptedDigest.getCountId()) + encryptedDigest.getCount());
            encRes.set(encryptedDigest.getSquareId(), encRes.get(encryptedDigest.getSquareId()) + encryptedDigest.getSquare());
            encRes.set(encryptedDigest.getCount1Id(), encRes.get(encryptedDigest.getCount1Id()) + encryptedDigest.getCount1());
            encRes.set(encryptedDigest.getCount2Id(), encRes.get(encryptedDigest.getCount2Id()) + encryptedDigest.getCount2());
            encRes.set(encryptedDigest.getCount3Id(), encRes.get(encryptedDigest.getCount3Id()) + encryptedDigest.getCount3());
            encRes.set(encryptedDigest.getCount4Id(), encRes.get(encryptedDigest.getCount4Id()) + encryptedDigest.getCount4());
            encRes.set(encryptedDigest.getCount5Id(), encRes.get(encryptedDigest.getCount5Id()) + encryptedDigest.getCount5());
            encRes.set(encryptedDigest.getCount6Id(), encRes.get(encryptedDigest.getCount6Id()) + encryptedDigest.getCount6());
        }
        List<Long> decRes = new ArrayList<>(Collections.nCopies(9, 0L));
        for (int i = 0; i < fToken.getFederationKeys().size(); i++) {
            long k = fToken.getFederationKeys().get(i);
            decRes.set(i, (encRes.get(i)-k));
            System.out.println("res"+i+": "+ decRes.get(i));
        }
        StaticticForFederationNew sff = new StaticticForFederationNew(decRes);
        return sff;
    }
}
