package producerNettyServer;

import com.google.protobuf.ByteString;
import exceptions.*;
import keyManagement.StreamKeyManager;
import producerProtocol.ProducerProtocol.*;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlConnect.FrontEndSQL;
import state.ProducerKeyStore;
import state.ProducerProfile;
import streamHandling.Stream;
import streamHandling.UploadManager;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;


public class producerRequestManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(producerRequestManager.class);
    private static final int NONCE_SIZE = 12;
    private DataProducer producer;

    public void registerProducer(ChannelHandlerContext ctx, String owner, long producerId, String producerName,
                                 String producerAddress, int producerPort) throws CouldNotStoreException,
            QueryFailedException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {

        if (DataProducer.registerProducer(owner, producerId, producerName, producerAddress, producerPort)){
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
        }else {
            ctx.writeAndFlush(createErrorResponse("ERROR", 1000));
        }
    }

    public void linkProducer(ChannelHandlerContext ctx, String owner, long producerId, String producerName)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        this.producer = DataProducer.getDataProducer(owner, producerId);
        System.out.println("get ok");
        System.out.println(this.producer.getProducerName());
        System.out.println(producerName);
        if (this.producer.getProducerName().equals(producerName)){
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("Link OK");
        }else {
            ctx.writeAndFlush(createErrorResponse("ERROR", 1000));
        }
    }

    public void keyAgreement(ChannelHandlerContext ctx, String usrName, long producerId, byte[] receivedPubKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, CertificateException,
            IOException, KeyStoreException, NoSuchProviderException {

//        System.out.println("001");
//        System.out.println(producer.getOwner().equals(usrName));
//        System.out.println(producer.getProducerId());
//        System.out.println(producerId);
//        System.out.println(producer.getProducerId() == producerId);
//        System.out.println((producer.getOwner().equals(usrName) && producer.getProducerId() == producerId));
        if (!(producer.getOwner().equals(usrName) && producer.getProducerId() == producerId)){
            producer = DataProducer.getDataProducer(usrName, producerId);
            producer.refreshProfile();
        }
        System.out.println("002");
        byte[] sendPubKeyBytes = producer.keyAgreement(usrName, receivedPubKeyBytes);

        System.out.println("003");
        ctx.writeAndFlush(createPubKeyResponse(usrName, sendPubKeyBytes));
        System.out.println("004");
    }

    public void createStream(ChannelHandlerContext ctx, String usrName, long producerId, long streamId, byte[] encStreamKey)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, CertificateException, IOException,
            KeyStoreException, CouldNotStoreException, QueryFailedException {

        if (!(producer.getOwner().equals(usrName) && producer.getProducerId() == producerId)){
            producer = DataProducer.getDataProducer(usrName, producerId);
            producer.refreshProfile();
        }
        if (producer.storeStreamKey(usrName, streamId, encStreamKey)){
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
        }else {
            ctx.writeAndFlush(createErrorResponse("ERROR", 1000));
        }
    }

    public void deleteStream(ChannelHandlerContext ctx, String owner, long sid) {
        try {
            System.out.println("Receive: "+"  usrName:  "+owner+"  streamId:  "+ sid);

            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("Response send over ");
            /*if(profile.deleteStream(owner, sid)){
                ctx.writeAndFlush(createSuccessResponse("Success", 1101));
                System.out.println("Response send over ");
            }*/
        } catch (Exception e) {
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }

    public void uploadData(ChannelHandlerContext ctx, long streamId) throws CouldNotReceiveException, WriteException, InvalidQueryException {
        producer.uploadData(streamId);
        ctx.writeAndFlush(createSuccessResponse("Success", 1101));
    }

    public void uploadDataLive(ChannelHandlerContext ctx, long streamId) throws CouldNotReceiveException, WriteException, InvalidQueryException {
        producer.uploadData(streamId);
        ctx.writeAndFlush(createSuccessResponse("Success", 1101));
    }

    /*public void UploadStreamData(){
        UploadManager um = getUploadManager(streamId);
        long start = st.getStartDate().getTime();
        long end = st.getEndDate().getTime();
        long size = st.getChunkSize();

        for (long stl = start; stl <= end; stl+=size){
            DataPoint dp = new DataPoint(new Date(stl), rand.nextInt(40)+60);
            //System.out.println((new DataPoint(new Date((start+size*i)), i)).toString());
            um.writeDataPointToStream(dp);
            //System.out.println(dp.getTimestamp());
        }
*//*        int dataN = (int) ((end - start) / size);
        for (int i = 0; i < dataN; i++) {
            DataPoint dp = new DataPoint(new Date((start+size*i)), rand.nextInt(40)+60);
            //System.out.println((new DataPoint(new Date((start+size*i)), i)).toString());
            System.out.println("add");
            um.writeDataPointToStream(dp);
            System.out.println(dp.getTimestamp());
            //um.writeDataPointToStream(new DataPoint(new Date((start+size*i+2)), rand.nextInt(40)+60));
        }*//*
        um.flush();
    }*/


    public void addChunk(ChannelHandlerContext ctx, String usrName, long correspondingStreamID) {
        try {
//            System.out.println("Receive: ");
//            System.out.println("StreamID: "+correspondingStreamID);
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
            String TOPIC_NAME = usrName + correspondingStreamID;

            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("Response send over ");
        } catch (Exception e) {
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
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

    private ResponseMessage createPubKeyResponse(String usrName, byte[] pubKey) {
        return ResponseMessage.newBuilder()
                .setType(MessageResponseType.PubKey_Response)
                .setPubKeyResponse(PubKeyResponse.newBuilder()
                        .setUsrName(usrName)
                        .setPubKey(ByteString.copyFrom(pubKey)))
                .build();
    }

}
