package dataServerInterface;

import com.google.protobuf.ByteString;
import dataServerInterface.dataServerProtocol.DataServerProtocol.*;
import exceptions.CouldNotReceiveException;
import exceptions.InvalidQueryException;
import streamHandling.Digest;
import streamHandling.EncryptedChunk;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class DataServerConnect implements Closeable, AutoCloseable {

    private Socket net;
    private OutputStream outStream;
    private InputStream inStream;

    private byte[] buffer = new byte[1024 * 8];

    public DataServerConnect(String ip, int port) throws IOException {
        this.net = new Socket(ip, port);
        this.net.setTcpNoDelay(true);
        outStream = this.net.getOutputStream();
        inStream = this.net.getInputStream();
    }

    static void writeRawVarint32(OutputStream out, int value) throws IOException {
        while ((value & -128) != 0) {
            out.write(value & 127 | 128);
            value >>>= 7;
        }

        out.write(value);
    }
    private static int readRawVarint32(InputStream buffer) throws IOException {
        if (false) {
            return 0;
        } else {
            byte tmp = (byte) buffer.read();
            if (tmp >= 0) {
                return tmp;
            } else {
                int result = tmp & 127;
                if (false) {
                    return 0;
                } else {
                    if ((tmp = (byte) buffer.read()) >= 0) {
                        result |= tmp << 7;
                    } else {
                        result |= (tmp & 127) << 7;

                        if ((tmp = (byte) buffer.read()) >= 0) {
                            result |= tmp << 14;
                        } else {
                            result |= (tmp & 127) << 14;

                            if ((tmp = (byte) buffer.read()) >= 0) {
                                result |= tmp << 21;
                            } else {
                                result |= (tmp & 127) << 21;

                                result |= (tmp = (byte) buffer.read()) << 28;
                                if (tmp < 0) {
                                    throw new RuntimeException("ERROR on Decode");
                                }
                            }
                        }
                    }
                    return result;
                }
            }
        }
    }
    private void writeRequest(RequestMessage requestMessage) throws IOException {
        writeRawVarint32(outStream, requestMessage.getSerializedSize());
        requestMessage.writeTo(outStream);
        outStream.flush();
    }
    private ResponseMessage loadResponse() throws IOException {
        int len = readRawVarint32(inStream);
        int curOffset = 0;
        if (buffer.length < len)
            buffer = new byte[len];
        do {
            curOffset = inStream.read(buffer, curOffset, len);
        } while (curOffset < len);

        return ResponseMessage.parser().parseFrom(buffer, 0, len);
    }

    public boolean createStream(String owner, long streamId) throws IOException {
        CreateStream cs = CreateStream.newBuilder().setUserName(owner).setStreamId(streamId).build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Create_Stream).setCreateStream(cs)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public boolean deleteStream(String owner, long streamId) throws IOException {
        DeleteStream ds = DeleteStream.newBuilder().setUserName(owner).setStreamId(streamId).build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Delete_Stream)
                .setDeleteStream(ds)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }



    public boolean addChunkNew(String usrName,long correspondingStreamId, long chunkId, EncryptedChunk encryptedChunk, Digest encryptedDigest) throws IOException {
        System.out.println("_____");
        System.out.println("Chunk:"+encryptedChunk.getChunkId());

        AddChunkNew.Builder acBuilder = AddChunkNew.newBuilder()
                .setUserName(usrName)
                .setStreamID(correspondingStreamId)
                .setChunkID(chunkId)
                .setEncryptedChunkBytes(ByteString.copyFrom(encryptedChunk.getPayload()))
                .setSum(encryptedDigest.getSum())
                .setCount(encryptedDigest.getCount())
                .setSquare(encryptedDigest.getSquare());
        System.out.println("chunk len: "+encryptedChunk.getPayload().length);
        if (encryptedDigest.isHasMac()){
            acBuilder.setHasMac(true)
                    .setSumMacBytes(ByteString.copyFrom(encryptedDigest.getSumMac().toByteArray()))
                    .setCountMacBytes(ByteString.copyFrom(encryptedDigest.getCountMac().toByteArray()))
                    .setSquareMacBytes(ByteString.copyFrom(encryptedDigest.getSquareMac().toByteArray()));
            System.out.println("mac len: "+encryptedDigest.getSumMac().toByteArray().length);
        }else {
            acBuilder.setHasMac(false);
        }

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Add_Chunk_New)
                .setAddChunkNew(acBuilder.build())
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public boolean addChunkNewDigest(String usrName,long correspondingStreamId, long chunkId, EncryptedChunk encryptedChunk, Digest encryptedDigest) throws IOException {
        System.out.println("_____");
        System.out.println("Chunk:"+encryptedChunk.getChunkId());

        AddChunkNewDigest.Builder acBuilder = AddChunkNewDigest.newBuilder()
                .setUserName(usrName)
                .setStreamID(correspondingStreamId)
                .setChunkID(chunkId)
                .setEncryptedChunkBytes(ByteString.copyFrom(encryptedChunk.getPayload()))
                .setSum(encryptedDigest.getSum())
                .setCount(encryptedDigest.getCount())
                .setSquare(encryptedDigest.getSquare())
                .setCount1(encryptedDigest.getCount1())
                .setCount2(encryptedDigest.getCount2())
                .setCount3(encryptedDigest.getCount3())
                .setCount4(encryptedDigest.getCount4())
                .setCount5(encryptedDigest.getCount5())
                .setCount6(encryptedDigest.getCount6());
        System.out.println("chunk len: "+encryptedChunk.getPayload().length);
        if (encryptedDigest.isHasMac()){
            acBuilder.setHasMac(true)
                    .setSumMacBytes(ByteString.copyFrom(encryptedDigest.getSumMac().toByteArray()))
                    .setCountMacBytes(ByteString.copyFrom(encryptedDigest.getCountMac().toByteArray()))
                    .setSquareMacBytes(ByteString.copyFrom(encryptedDigest.getSquareMac().toByteArray()))
                    .setCount1MacBytes(ByteString.copyFrom(encryptedDigest.getCount1Mac().toByteArray()))
                    .setCount2MacBytes(ByteString.copyFrom(encryptedDigest.getCount2Mac().toByteArray()))
                    .setCount3MacBytes(ByteString.copyFrom(encryptedDigest.getCount3Mac().toByteArray()))
                    .setCount4MacBytes(ByteString.copyFrom(encryptedDigest.getCount4Mac().toByteArray()))
                    .setCount5MacBytes(ByteString.copyFrom(encryptedDigest.getCount5Mac().toByteArray()))
                    .setCount6MacBytes(ByteString.copyFrom(encryptedDigest.getCount6Mac().toByteArray()));
            System.out.println("mac len: "+encryptedDigest.getSumMac().toByteArray().length);
        }else {
            acBuilder.setHasMac(false);
        }

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Add_Chunk_NewDigest)
                .setAddChunkNewDigest(acBuilder.build())
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public List<EncryptedChunk> getChunks(String usrName, long streamId, long chunkIdFrom, long chunkIdTo) throws IOException, CouldNotReceiveException {
        GetChunks gc = GetChunks.newBuilder()
                .setUserName(usrName)
                .setStreamID(streamId)
                .setChunkIdFrom(chunkIdFrom)
                .setChunkIdTo(chunkIdTo)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Get_Chunks)
                .setGetChunks(gc)
                .build();
        writeRequest(req);

        outStream.flush();
        int numMsgs = (int) ((chunkIdTo - chunkIdFrom + 1));
        //System.out.println(numMsgs);
        int cur = 0;
        boolean hasError = false;
        String errMsg = "";
        List<EncryptedChunk> result = new ArrayList<>();
        do {
            ResponseMessage msg = loadResponse();
            switch (msg.getType()) {
                case Data_Response:
                    DataResponse resp = msg.getDataResponse();
                    result.add(new EncryptedChunk(streamId, resp.getKey(), resp.getData().toByteArray()));
                    cur++;
                    //System.out.println(cur);
                    break;
                case Error_Response:
                    result.add(new EncryptedChunk(streamId, 0, null));
                    hasError = true;
                    errMsg = msg.getErrorResponse().getMessage();
                    cur++;
                    break;
                case MultiData_Response:
                    assert (numMsgs == msg.getMultiDataTransfer().getNumTransfers());
                    numMsgs = msg.getMultiDataTransfer().getNumTransfers();
                    break;

                default:
                    hasError = true;
            }
        } while (cur < numMsgs);
        if (hasError) {
            throw new CouldNotReceiveException("GetChunks Query Failed " + errMsg);
        }
        return result;

    }

    public List<Digest> getStatistics(String usrName, long streamId, long chunkIdFrom, long chunkIdTo,
                                      long granularity) throws IOException, InvalidQueryException {

        GetStatisticsNew gsn = GetStatisticsNew.newBuilder()
                .setUserName(usrName)
                .setStreamID(streamId)
                .setChunkIdFrom(chunkIdFrom)
                .setChunkIdTo(chunkIdTo)
                .setGranularity(granularity)
                .build();

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Get_Statistics_New)
                .setGetStatisticsNew(gsn)
                .build();
        writeRequest(req);

        int numMsgs = (int) ((chunkIdTo - chunkIdFrom + 1) / granularity);
        int cur = 0;
        boolean hasError = false;
        List<Digest> result = new ArrayList<>();
        String errMsg = "";
        long fromCur = chunkIdFrom;
        long toCur = fromCur + granularity - 1;
        //System.out.println("num: "+numMsgs);
        do {
            ResponseMessage msg = loadResponse();
            switch (msg.getType()) {
                case Digest_Response:
                    result.add(parseDigest(streamId, fromCur, toCur, msg.getDigestResponse()));
                    //System.out.println("cur: "+cur);
                    cur++;
                    fromCur += granularity;
                    toCur += granularity;
                    break;
                case Error_Response:
                    errMsg = msg.getErrorResponse().getMessage();
                    //System.out.println("cur: "+cur);
                    hasError = true;
                    cur++;
                    break;
                case MultiData_Response:
                    assert (numMsgs == msg.getMultiDataTransfer().getNumTransfers());
                    numMsgs = msg.getMultiDataTransfer().getNumTransfers();
                    break;
                default:
                    hasError = true;
            }
        } while (cur < numMsgs);
        if (hasError) {
            System.out.println("hasError");
            throw new InvalidQueryException("Error message from server: '" + errMsg + "'");
        }
        return result;
    }
    private static Digest parseDigest(long streamId, long chunkIdFrom, long chunkIdTo, DigestResponse response) {
        Digest dn = new Digest(streamId, chunkIdFrom, chunkIdTo, response.getSum(), response.getCount(), response.getSquare(), true, false);
        if(response.getHasMac()){
            dn.setHasMac(true);
            dn.setSumMac(new BigInteger(response.getSumMacBytes().toByteArray()));
            dn.setCountMac(new BigInteger(response.getCountMacBytes().toByteArray()));
            dn.setSquareMac(new BigInteger(response.getSquareMacBytes().toByteArray()));
        }
        return dn;
    }

    public List<Digest> getStatisticsNewDigest(String usrName, long streamId, long chunkIdFrom, long chunkIdTo,
                                      long granularity) throws IOException, InvalidQueryException {

        GetStatisticsNew gsn = GetStatisticsNew.newBuilder()
                .setUserName(usrName)
                .setStreamID(streamId)
                .setChunkIdFrom(chunkIdFrom)
                .setChunkIdTo(chunkIdTo)
                .setGranularity(granularity)
                .build();

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Get_Statistics_New)
                .setGetStatisticsNew(gsn)
                .build();
        writeRequest(req);

        int numMsgs = (int) ((chunkIdTo - chunkIdFrom + 1) / granularity);
        int cur = 0;
        boolean hasError = false;
        List<Digest> result = new ArrayList<>();
        String errMsg = "";
        long fromCur = chunkIdFrom;
        long toCur = fromCur + granularity - 1;
        //System.out.println("num: "+numMsgs);
        do {
            ResponseMessage msg = loadResponse();
            switch (msg.getType()) {
                case Digest_Response_New:
                    result.add(parseNewDigest(streamId, fromCur, toCur, msg.getDigestResponseNew()));
                    //System.out.println("cur: "+cur);
                    cur++;
                    fromCur += granularity;
                    toCur += granularity;
                    break;
                case Error_Response:
                    errMsg = msg.getErrorResponse().getMessage();
                    //System.out.println("cur: "+cur);
                    hasError = true;
                    cur++;
                    break;
                case MultiData_Response:
                    assert (numMsgs == msg.getMultiDataTransfer().getNumTransfers());
                    numMsgs = msg.getMultiDataTransfer().getNumTransfers();
                    break;
                default:
                    hasError = true;
            }
        } while (cur < numMsgs);
        if (hasError) {
            System.out.println("hasError");
            throw new InvalidQueryException("Error message from server: '" + errMsg + "'");
        }
        return result;
    }

    private static Digest parseNewDigest(long streamId, long chunkIdFrom, long chunkIdTo, DigestResponseNew response) {
        Digest dn = new Digest(streamId, chunkIdFrom, chunkIdTo, response.getSum(), response.getCount(),
                response.getSquare(), response.getCount1(), response.getCount2(), response.getCount3(),
                response.getCount4(), response.getCount5(), response.getCount6(), true, false);
        if(response.getHasMac()){
            dn.setHasMac(true);
            dn.setSumMac(new BigInteger(response.getSumMacBytes().toByteArray()));
            dn.setCountMac(new BigInteger(response.getCountMacBytes().toByteArray()));
            dn.setSquareMac(new BigInteger(response.getSquareMacBytes().toByteArray()));
            dn.setCount1Mac(new BigInteger(response.getCount1MacBytes().toByteArray()));
            dn.setCount2Mac(new BigInteger(response.getCount2MacBytes().toByteArray()));
            dn.setCount3Mac(new BigInteger(response.getCount3MacBytes().toByteArray()));
            dn.setCount4Mac(new BigInteger(response.getCount4MacBytes().toByteArray()));
            dn.setCount5Mac(new BigInteger(response.getCount5MacBytes().toByteArray()));
            dn.setCount6Mac(new BigInteger(response.getCount6MacBytes().toByteArray()));
        }
        return dn;
    }

    public Digest getStatisticAll(String usrName, long streamId, long chunkIdFrom, long chunkIdTo) throws IOException {
        GetStatisticAllNew gsan = GetStatisticAllNew.newBuilder()
                .setUserName(usrName)
                .setStreamID(streamId)
                .setChunkIdFrom(chunkIdFrom)
                .setChunkIdTo(chunkIdTo)
                .build();

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Get_Statistics_All_New)
                .setGetStatisticAllNew(gsan)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        if (msg.getType() == MessageResponseType.Digest_Response){
            Digest res = parseDigest(streamId, chunkIdFrom, chunkIdTo, msg.getDigestResponse());
            return res;
        }else {
            throw new RuntimeException("Error Response");
        }
    }
    public Digest getStatisticAllNew(String usrName, long streamId, long chunkIdFrom, long chunkIdTo) throws IOException {
        GetStatisticAllNew gsan = GetStatisticAllNew.newBuilder()
                .setUserName(usrName)
                .setStreamID(streamId)
                .setChunkIdFrom(chunkIdFrom)
                .setChunkIdTo(chunkIdTo)
                .build();

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Get_Statistics_All_New)
                .setGetStatisticAllNew(gsan)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        if (msg.getType() == MessageResponseType.Digest_Response_New){
            Digest res = parseNewDigest(streamId, chunkIdFrom, chunkIdTo, msg.getDigestResponseNew());
            return res;
        }else {
            throw new RuntimeException("Error Response");
        }
    }

    /*public boolean createToken(long tokenId, byte[] nodes_content, int depth) throws IOException {
        CreateToken ct = CreateToken.newBuilder()
                .setTokenId(tokenId)
                .setNodesContent(ByteString.copyFrom(nodes_content))
                .setDepth(depth)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Create_Token)
                .setCreateToken(ct)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }
    public boolean deleteToken(long tokenId) throws IOException {
        DeleteToken dt = DeleteToken.newBuilder()
                .setTokenId(tokenId)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Delete_Token)
                .setDeleteToken(dt)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }*/


    public void close() throws IOException {
        net.close();
    }

}