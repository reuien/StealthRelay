package dataServerNettyServer;

import com.google.protobuf.ByteString;
import index.Chunk;
import index.Digest;
import index.blockindex.node.NodeContent;
import io.netty.channel.ChannelHandlerContext;
import kafka.KafkaReader;
import kafka.KafkaTopicManager;
import kafka.KafkaUploader;
import dataServerProtocol.DataServerProtocol.*;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DataServerRequestManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataServerRequestManager.class);
    private Producer<Long, byte[]> kafkaProducerChunk = KafkaUploader.getProducerChunk();
    private Producer<Long, String> kafkaProducerDigest = KafkaUploader.getProducerDigest();
    private KafkaReader kafkaReader = new KafkaReader();

    public void createStream(ChannelHandlerContext ctx, String userName, long streamId) {
        try {
            System.out.println("Receive: "+"  usrName:  "+userName+"  streamId:  "+streamId);
            KafkaTopicManager.createTopic(userName, streamId);
            ctx.writeAndFlush(createSuccessResponse("Success Create", 1101));
            System.out.println("Response send over ");
        } catch (Exception e) {
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }
    public void deleteStream(ChannelHandlerContext ctx, String owner, long sid) {
        try {
            System.out.println("Receive: "+"  usrName:  "+owner+"  streamId:  "+sid);
            KafkaTopicManager.deleteTopic(owner, sid);
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("Response send over ");
        } catch (Exception e) {
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }

    public void addChunk(ChannelHandlerContext ctx, String usrName, long streamID, Chunk chunk, NodeContent[] contentData) {
        try {
//            System.out.println("Receive: ");
//            System.out.println("StreamID: "+streamID);
//            System.out.println("ChunkID: "+chunk.getId());
//            System.out.println("ChunkBytes: "+Arrays.toString(chunk.getData()));
//            System.out.println("DigestBytes: "+Arrays.toString(contentData));
//
//            for (int i = 0; i < Arrays.stream(contentData).count(); i++) {
//                LongMacNodeNodeContent nodei = (LongMacNodeNodeContent) contentData[i];
//                System.out.println("Node_"+i);
//                System.out.println("long: "+nodei.getLong());
//                System.out.println("mac:  "+nodei.getMac());
//                System.out.println("String:"+nodei.getStringRepresentation());
//                System.out.println("toString:"+nodei);
//            }
            String TOPIC_NAME = usrName + streamID;
            KafkaUploader.uploadChunk(kafkaProducerChunk, TOPIC_NAME, 0, chunk.getId(), chunk.getData());
            for (int i = 0; i < 3; i++) {
                KafkaUploader.uploadChunk(kafkaProducerChunk, TOPIC_NAME, i+1, chunk.getId(), contentData[i].encode());
            }

            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("Response send over ");
        } catch (Exception e) {
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }

    public void addChunkNew(ChannelHandlerContext ctx, String usrName, long streamID, Chunk chunk, Digest digest) {
        try {

            String TOPIC_NAME = usrName + streamID;
            KafkaUploader.uploadChunk(kafkaProducerChunk, TOPIC_NAME, 0, chunk.getId(), chunk.getData());
            KafkaUploader.uploadDigest(kafkaProducerDigest, TOPIC_NAME, 1, chunk.getId(), digest);

            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("Response send over ");
        } catch (Exception e) {
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }

    public void getChunks(ChannelHandlerContext ctx, String usrName, long steamId, long from, long to) {
        int size = (int) (to - from + 1);
        if (size > 1) {
            ResponseMessage multiTransfer = ResponseMessage.newBuilder()
                    .setType(MessageResponseType.MultiData_Response)
                    .setMultiDataTransfer(
                            MultiDataTransfer.newBuilder()
                            .setMessageType(MessageResponseType.Data_Response)
                            .setNumTransfers(size)
                            .build())
                    .build();
            ctx.write(multiTransfer);
            System.out.println("multiTransfer send");
        }
        List<Chunk> chunks = kafkaReader.getChunks(usrName, steamId, from, to);
        //System.out.println(chunks.size());
        for (Chunk curChunk : chunks){
            try {
                if (curChunk == null) {
                    LOGGER.warn("Could not find any chunks data for the given request {uid " + steamId +
                            " owner " + usrName + " from " + from + " to " + to + "}");
                    ctx.write(createErrorResponse("Could not find any chunks data for the given request "
                            , 1));
                }

                ResponseMessage chunkResponse = ResponseMessage.newBuilder()
                        .setType(MessageResponseType.Data_Response)
                        .setDataResponse(DataResponse.newBuilder()
                                .setKey(curChunk.getId())
                                .setData(ByteString.copyFrom(curChunk.getData())))
                        .build();
                ctx.write(chunkResponse);
                //System.out.println(curChunk.getId());
            } catch (Exception e) {
                LOGGER.error("Exception caught - while processing chunks of get chunk request {steamId "
                        + steamId + " usrName " + usrName + " from " + from + " to " + to + "}", e);
                ctx.write(createErrorResponse("Message: " + e.getMessage(), 1));
            }
        }
        ctx.flush();
    }

    /*public void getChunkSingle(ChannelHandlerContext ctx, String usrName, long steamId, long from, long to) {

        List<Integer> getKeys = new ArrayList<>();
        for (int i = (int) from; i < to; i++) {
            getKeys.add(i);
        }
        if (getKeys.size() > 1) {
            ResponseMessage multiTransfer = ResponseMessage.newBuilder()
                    .setType(MessageResponseType.MultiData_Response)
                    .setMultiDataTransfer(MultiDataTransfer.newBuilder()
                            .setMessageType(MessageResponseType.Data_Response)
                            .setNumTransfers(getKeys.size())
                            .build())
                    .build();
            ctx.write(multiTransfer);
        }

        for (int key : getKeys) {
            try {
                Chunk curChunk = kafkaReader.getChunk(usrName, steamId, key);
                if (curChunk == null) {
                    LOGGER.warn("Could not find any chunks data for the given request {uid " + steamId +
                            " owner " + usrName + " from " + from + " to " + to + "}");
                    ctx.write(createErrorResponse("Could not find any chunks data for the given request "
                            , 1));
                }
                ResponseMessage chunkResponse = ResponseMessage.newBuilder()
                        .setType(MessageResponseType.Data_Response)
                        .setDataResponse(DataResponse.newBuilder()
                                .setKey(curChunk.getId())
                                .setData(ByteString.copyFrom(curChunk.getData())))
                        .build();
                ctx.write(chunkResponse);
            } catch (Exception e) {
                LOGGER.error("Exception caught - while processing chunks of get chunk request {steamId "
                        + steamId + " usrName " + usrName + " from " + from + " to " + to + "}", e);
                ctx.write(createErrorResponse("Message: " + e.getMessage(), 1));
            }
        }
        ctx.flush();
    }*/
    public void getStatistics(ChannelHandlerContext ctx, String usrName, long steamId,  long from, long to,
                              long granularity, int[] ids) {
        long fromIter = from;
        long toIter = from + granularity - 1;
        int numIter = (int) ((to - from + 1) / granularity);
/*
        System.out.println(from);
        System.out.println(to);
        System.out.println(granularity);
        System.out.println(numIter);
*/

        if (numIter > 1) {
            ResponseMessage multiTransfer = ResponseMessage.newBuilder()
                    .setType(MessageResponseType.MultiData_Response)
                    .setMultiDataTransfer(MultiDataTransfer.newBuilder()
                            .setMessageType(MessageResponseType.Statistics_Response)
                            .setNumTransfers(numIter)
                            .build())
                    .build();
            ctx.write(multiTransfer);
        }

        NodeContent[] content;
        while (toIter <= to) {
            try {
                System.out.println("form: "+ fromIter+"   to: "+ toIter);
                content = kafkaReader.getAggregation(usrName, steamId, fromIter, toIter, ids);

                if (content == null) {
                    LOGGER.warn("Could not find any statistics data for the given request {steamId " + steamId +
                            " usrName " + usrName + " from " + from + " to " + to + " granularity " + granularity +
                            " ids " + Arrays.toString(ids) + " }");
                    ctx.write(createErrorResponse("Could not find any statistics data for the given request "
                            , 1));
                }

                List<MetaData> metadata = new ArrayList<>(content.length);
                for (int iter : ids) {
                    metadata.add(MetaData.newBuilder()
                            .setEncryptedMetaDataId(iter)
                            .setEncryptedMetaDataBytes(ByteString.copyFrom(content[iter].encode()))
                            .build());
                }

                ResponseMessage response = ResponseMessage.newBuilder()
                        .setType(MessageResponseType.Statistics_Response)
                        .setStatisticsResponse(StatisticsResponse.newBuilder().addAllData(metadata))
                        .build();
                ctx.write(response);
            } catch (Exception e) {
                LOGGER.error("Exception caught - while processing node content of statistic request {steamId " + steamId +
                        " usrName " + usrName + " from " + from + " to " + to + " granularity " + granularity +
                        " ids " + Arrays.toString(ids) + " }", e);
                ctx.write(createErrorResponse("Message: " + e.getMessage(), 1));
            }

            toIter += granularity;
            fromIter += granularity;

        }
        ctx.flush();
    }

    public void getStatisticsNew(ChannelHandlerContext ctx, String usrName, long steamId,  long from, long to, long granularity) {
        long fromIter = from;
        long toIter = from + granularity - 1;
        int numIter = (int) ((to - from + 1) / granularity);

        if (numIter > 1) {
            ResponseMessage multiTransfer = ResponseMessage.newBuilder()
                    .setType(MessageResponseType.MultiData_Response)
                    .setMultiDataTransfer(MultiDataTransfer.newBuilder()
                            .setMessageType(MessageResponseType.Digest_Response)
                            .setNumTransfers(numIter)
                            .build())
                    .build();
            ctx.write(multiTransfer);
        }
        List<Digest> digestList = kafkaReader.getDigests(usrName, steamId, from, to);
        if (digestList == null) {
            LOGGER.warn("Could not find any statistics data for the given request {steamId " + steamId +
                    " usrName " + usrName + " from " + from + " to " + to + " granularity " + granularity + " }");
            ctx.write(createErrorResponse("Could not find any statistics data for the given request "
                    , 1));
        }
        Digest initialDigest;
        boolean hasMAC = false;
        if (digestList != null) {
            hasMAC = digestList.get(0).isThisHasMAC();
            System.out.println(digestList.get(0));
            System.out.println("HAS MAC: "+ hasMAC);
        }
        if (hasMAC){
            initialDigest = new Digest(0, 0, 0, true, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        }else{
            initialDigest = new Digest(0, 0, 0, false);
        }
        while (toIter <= to) {
            try {
                System.out.println("form: "+ fromIter+"   to: "+ toIter);
                Digest curAggeratedDigest = (Digest) initialDigest.clone();
                System.out.println("initial:  "+initialDigest);
                System.out.println("before   curAggeratedDigest: "+curAggeratedDigest);
                for (long cur = fromIter; cur <= toIter ; cur++) {
                    System.out.println(cur-from);
                    curAggeratedDigest.mergeOther(digestList.get((int) (cur-from)));
                }
                System.out.println("merged   curAggeratedDigest: "+curAggeratedDigest);
                DigestResponse.Builder drBuilder = DigestResponse.newBuilder()
                        .setChunkIdFrom(fromIter)
                        .setChunkIdTo(toIter)
                        .setSum(curAggeratedDigest.getSum())
                        .setCount(curAggeratedDigest.getCount())
                        .setSquare(curAggeratedDigest.getSquare());
                if (hasMAC){
                    drBuilder.setSumMacBytes(ByteString.copyFrom(curAggeratedDigest.getSumMac().toByteArray()))
                            .setCountMacBytes(ByteString.copyFrom(curAggeratedDigest.getCountMac().toByteArray()))
                            .setSquareMacBytes(ByteString.copyFrom(curAggeratedDigest.getSquareMac().toByteArray()))
                            .setHasMac(true);
                }
                ResponseMessage digestResponse = ResponseMessage.newBuilder()
                        .setType(MessageResponseType.Digest_Response)
                        .setDigestResponse(drBuilder.build())
                        .build();
                ctx.write(digestResponse);
            } catch (Exception e) {
                LOGGER.error("Exception caught - while processing node content of statistic request {steamId " + steamId +
                        " usrName " + usrName + " from " + from + " to " + to + " granularity " + granularity + " }", e);
                ctx.write(createErrorResponse("Message: " + e.getMessage(), 1));
            }
            toIter += granularity;
            fromIter += granularity;
        }
        ctx.flush();
    }

    public void getStatisticsNewDigest(ChannelHandlerContext ctx, String usrName, long steamId,  long from, long to, long granularity) {
        long fromIter = from;
        long toIter = from + granularity - 1;
        int numIter = (int) ((to - from + 1) / granularity);

        if (numIter > 1) {
            ResponseMessage multiTransfer = ResponseMessage.newBuilder()
                    .setType(MessageResponseType.MultiData_Response)
                    .setMultiDataTransfer(MultiDataTransfer.newBuilder()
                            .setMessageType(MessageResponseType.Digest_Response)
                            .setNumTransfers(numIter)
                            .build())
                    .build();
            ctx.write(multiTransfer);
        }
        List<Digest> digestList = kafkaReader.getDigests(usrName, steamId, from, to);
        if (digestList == null) {
            LOGGER.warn("Could not find any statistics data for the given request {steamId " + steamId +
                    " usrName " + usrName + " from " + from + " to " + to + " granularity " + granularity + " }");
            ctx.write(createErrorResponse("Could not find any statistics data for the given request "
                    , 1));
        }
        Digest initialDigest;
        boolean hasMAC = false;
        if (digestList != null) {
            hasMAC = digestList.get(0).isThisHasMAC();
            System.out.println(digestList.get(0));
            System.out.println("HAS MAC: "+ hasMAC);
        }
        if (hasMAC){
            initialDigest = new Digest(0, 0, 0, 0, 0, 0, 0, 0,
                    0, true, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        }else{
            initialDigest = new Digest(0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        }
        while (toIter <= to) {
            try {
                System.out.println("form: "+ fromIter+"   to: "+ toIter);
                Digest curAggeratedDigest = (Digest) initialDigest.clone();
                System.out.println("initial:  "+initialDigest);
                System.out.println("before   curAggeratedDigest: "+curAggeratedDigest);
                for (long cur = fromIter; cur <= toIter ; cur++) {
                    System.out.println(cur-from);
                    curAggeratedDigest.mergeOtherNewDigest(digestList.get((int) (cur-from)));
                }
                System.out.println("merged   curAggeratedDigest: "+curAggeratedDigest);
                DigestResponseNew.Builder drnBuilder = DigestResponseNew.newBuilder()
                        .setChunkIdFrom(fromIter)
                        .setChunkIdTo(toIter)
                        .setSum(curAggeratedDigest.getSum())
                        .setCount(curAggeratedDigest.getCount())
                        .setSquare(curAggeratedDigest.getSquare())
                        .setCount1(curAggeratedDigest.getCount1())
                        .setCount2(curAggeratedDigest.getCount2())
                        .setCount3(curAggeratedDigest.getCount3())
                        .setCount4(curAggeratedDigest.getCount4())
                        .setCount5(curAggeratedDigest.getCount5())
                        .setCount6(curAggeratedDigest.getCount6());
                if (hasMAC){
                    drnBuilder.setSumMacBytes(ByteString.copyFrom(curAggeratedDigest.getSumMac().toByteArray()))
                            .setCountMacBytes(ByteString.copyFrom(curAggeratedDigest.getCountMac().toByteArray()))
                            .setSquareMacBytes(ByteString.copyFrom(curAggeratedDigest.getSquareMac().toByteArray()))
                            .setCount1MacBytes(ByteString.copyFrom(curAggeratedDigest.getCount1Mac().toByteArray()))
                            .setCount2MacBytes(ByteString.copyFrom(curAggeratedDigest.getCount2Mac().toByteArray()))
                            .setCount3MacBytes(ByteString.copyFrom(curAggeratedDigest.getCount3Mac().toByteArray()))
                            .setCount4MacBytes(ByteString.copyFrom(curAggeratedDigest.getCount4Mac().toByteArray()))
                            .setCount5MacBytes(ByteString.copyFrom(curAggeratedDigest.getCount5Mac().toByteArray()))
                            .setCount6MacBytes(ByteString.copyFrom(curAggeratedDigest.getCount6Mac().toByteArray()))
                            .setHasMac(true);
                }
                ResponseMessage digestResponseNew = ResponseMessage.newBuilder()
                        .setType(MessageResponseType.Digest_Response_New)
                        .setDigestResponseNew(drnBuilder.build())
                        .build();
                ctx.write(digestResponseNew);
            } catch (Exception e) {
                LOGGER.error("Exception caught - while processing node content of statistic request {steamId " + steamId +
                        " usrName " + usrName + " from " + from + " to " + to + " granularity " + granularity + " }", e);
                ctx.write(createErrorResponse("Message: " + e.getMessage(), 1));
            }
            toIter += granularity;
            fromIter += granularity;
        }
        ctx.flush();
    }

    public void getStatisticAll(ChannelHandlerContext ctx, String usrName, long steamId,  long from, long to, int[] ids) throws Exception {

        NodeContent[] content;
        content = kafkaReader.getAggregation(usrName, steamId, from, to, ids);
        if (content == null) {
            LOGGER.warn("Could not find any statistics data for the given request {steamId " + steamId +
                    " usrName " + usrName + " from " + from + " to " + to + " ids " + Arrays.toString(ids) + " }");
            ctx.write(createErrorResponse("Could not find any statistics data for the given request ", 1));
        }
        List<MetaData> metadata = new ArrayList<>(content.length);
        for (int iter : ids) {
            metadata.add(MetaData.newBuilder()
                    .setEncryptedMetaDataId(iter)
                    .setEncryptedMetaDataBytes(ByteString.copyFrom(content[iter].encode()))
                    .build());
        }

        ResponseMessage response = ResponseMessage.newBuilder()
                .setType(MessageResponseType.Statistics_Response)
                .setStatisticsResponse(StatisticsResponse.newBuilder().addAllData(metadata))
                .build();
        ctx.write(response);

        ctx.flush();
    }

    public void getStatisticAllNew(ChannelHandlerContext ctx, String usrName, long steamId,  long from, long to) throws Exception {

        Digest aggeratedDigest = kafkaReader.getAggregatedDigest(usrName, steamId, from, to);

        DigestResponse.Builder drBuilder = DigestResponse.newBuilder()
                .setChunkIdFrom(from)
                .setChunkIdTo(to)
                .setSum(aggeratedDigest.getSum())
                .setCount(aggeratedDigest.getCount())
                .setSquare(aggeratedDigest.getSquare());
        if (aggeratedDigest.isThisHasMAC()){
            drBuilder.setSumMacBytes(ByteString.copyFrom(aggeratedDigest.getSumMac().toByteArray()))
                    .setCountMacBytes(ByteString.copyFrom(aggeratedDigest.getCountMac().toByteArray()))
                    .setSquareMacBytes(ByteString.copyFrom(aggeratedDigest.getSquareMac().toByteArray()));
        }
        ResponseMessage digestResponse = ResponseMessage.newBuilder()
                .setType(MessageResponseType.Digest_Response)
                .setDigestResponse(drBuilder.build())
                .build();
        ctx.write(digestResponse);

        ctx.flush();
    }

    public void getStatisticAllNewDigest(ChannelHandlerContext ctx, String usrName, long steamId,  long from, long to) throws Exception {

        Digest aggeratedDigest = kafkaReader.getAggregatedDigestNew(usrName, steamId, from, to);

        DigestResponseNew.Builder drnBuilder = DigestResponseNew.newBuilder()
                .setChunkIdFrom(from)
                .setChunkIdTo(to)
                .setSum(aggeratedDigest.getSum())
                .setCount(aggeratedDigest.getCount())
                .setSquare(aggeratedDigest.getSquare())
                .setCount1(aggeratedDigest.getCount1())
                .setCount2(aggeratedDigest.getCount2())
                .setCount3(aggeratedDigest.getCount3())
                .setCount4(aggeratedDigest.getCount4())
                .setCount5(aggeratedDigest.getCount5())
                .setCount6(aggeratedDigest.getCount6());
        if (aggeratedDigest.isThisHasMAC()){
            drnBuilder.setSumMacBytes(ByteString.copyFrom(aggeratedDigest.getSumMac().toByteArray()))
                    .setCountMacBytes(ByteString.copyFrom(aggeratedDigest.getCountMac().toByteArray()))
                    .setSquareMacBytes(ByteString.copyFrom(aggeratedDigest.getSquareMac().toByteArray()))
                    .setCount1MacBytes(ByteString.copyFrom(aggeratedDigest.getCount1Mac().toByteArray()))
                    .setCount2MacBytes(ByteString.copyFrom(aggeratedDigest.getCount2Mac().toByteArray()))
                    .setCount3MacBytes(ByteString.copyFrom(aggeratedDigest.getCount3Mac().toByteArray()))
                    .setCount4MacBytes(ByteString.copyFrom(aggeratedDigest.getCount4Mac().toByteArray()))
                    .setCount5MacBytes(ByteString.copyFrom(aggeratedDigest.getCount5Mac().toByteArray()))
                    .setCount6MacBytes(ByteString.copyFrom(aggeratedDigest.getCount6Mac().toByteArray()));
        }
        ResponseMessage digestResponseNew = ResponseMessage.newBuilder()
                .setType(MessageResponseType.Digest_Response_New)
                .setDigestResponseNew(drnBuilder.build())
                .build();
        ctx.write(digestResponseNew);

        ctx.flush();
    }

    private ResponseMessage createSuccessResponse(String response, int id) {
        return ResponseMessage.newBuilder()
                .setType(MessageResponseType.Success_Response)
                .setSuccessResponse(SuccessResponse.newBuilder()
                        .setId(id)
                        .setMessage(response))
                .build();
    }
    private ResponseMessage createErrorResponse(String response, int id) {
        return ResponseMessage.newBuilder()
                .setType(MessageResponseType.Error_Response)
                .setErrorResponse(ErrorResponse.newBuilder()
                        .setId(id)
                        .setMessage(response))
                .build();
    }



}
