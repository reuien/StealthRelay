package usrs;

import exceptions.CouldNotReceiveException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statistics.StaticticForFederation;
import statistics.StaticticForFederationNew;
import streamHandling.*;
import streamHandling.ChunkForDC;
import streamHandling.Request;
import streamHandling.Token;

import java.io.IOException;
import java.util.*;

public class DataConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataConsumer.class);
    private String usrName;
    private AcManager acManager;

    public DataConsumer(String usrName){
        this.usrName = usrName;
        this.acManager = new AcManager();
    }

    public String getUsrName() {
        return usrName;
    }
//
//    public Map<Long, PrivacyPolicy> getPrivacyPolicies(String ownerName) {
//        return acManager.getPrivacyPolicies(ownerName);
//    }
//    public Map<Long, FederationPolicy> getFederationPolicies(String ownerName) {
//        return acManager.getFederationPolicies(ownerName);
//    }
//
//    public Request getRequest(String consumerName, String ownerUsrName, long streamID, Date startTime, Date endTime, long granularity){
//        return acManager.createRequest(consumerName, ownerUsrName, streamID, startTime, endTime, granularity);
//    }

    public Token sendRequest(String consumerName, String ownerName, long policyId, long streamId, Date startTime, Date endTime, long granularity) throws IOException, ClassNotFoundException {
        return acManager.sendRequest(consumerName, ownerName, policyId, streamId, startTime, endTime, granularity);
    }

    public List<ChunkForDC> getChunksDC(Token tk, Date startTime, Date endTime) throws CouldNotReceiveException {
        List<ChunkForDC> chunks;
        try {
            chunks = acManager.getChunksDC(tk, startTime, endTime);
        } catch (CouldNotReceiveException e) {
            LOGGER.error("接收数据块时出错", e);
            return null;
        } catch (Exception e) {
            LOGGER.error("解密区块时出错", e);
            return null;
        }
/*        for (ChunkForDC chunk : chunks) {
            System.out.println(chunk.toString());
        }*/
        return chunks;
    }

//    public List<Digest> getDigestsDC(Token tk, Date startTime, Date endTime, int granularity) throws CouldNotReceiveException {
//        List<Digest> digests;
//        try {
//            digests = acManager.getDigestsDC(tk, startTime, endTime, granularity);
//        } catch (Exception e) {
//            LOGGER.error("出错", e);
//            return null;
//        }
//        return digests;
//
///*        for (DigestForDC digest : digests) {
//            System.out.println(StatisticForDC.getStatisticInfo(digest));
//        }*/
//
//    }

    public List<Digest> getNewDigestsDC(Token tk, Date startTime, Date endTime, int granularity) throws CouldNotReceiveException {
        List<Digest> digests;
        try {
            digests = acManager.getNewDigestsDC(tk, startTime, endTime, granularity);
        } catch (Exception e) {
            LOGGER.error("出错", e);
            return null;
        }
        return digests;
/*        for (DigestForDC digest : digests) {
            System.out.println(StatisticForDC.getStatisticInfo(digest));
        }*/
    }

    public Digest getAllNewDigestsDC(Token tk, Date startTime, Date endTime) throws CouldNotReceiveException {
        Digest digest;
        try {
            digest = acManager.getAllNewDigestsDC(tk, startTime, endTime);
        } catch (Exception e) {
            LOGGER.error("出错", e);
            return null;
        }
        return digest;

/*        for (DigestForDC digest : digests) {
            System.out.println(StatisticForDC.getStatisticInfo(digest));
        }*/
    }

    public FederationToken getFederationToken(String consumerName, ArrayList<Pair<String, Long>> nameAndStreamList, long fromTime, long toTime) throws IOException, ClassNotFoundException {
        return acManager.getFederationToken(consumerName, nameAndStreamList, fromTime, toTime);
    }

//    public StaticticForFederation getFederationInfo(FederationToken fToken, ArrayList<Pair<String, Long>> nameAndStreamList, long fromTime, long toTime) throws IOException {
//        return acManager.getFederationInfo(fToken, nameAndStreamList, fromTime, toTime);
//    }

    public StaticticForFederationNew getFederationInfoNew(FederationToken fToken, ArrayList<Pair<String, Long>> nameAndStreamList, long fromTime, long toTime) throws IOException {
        return acManager.getFederationInfoNew(fToken, nameAndStreamList, fromTime, toTime);
    }
}
