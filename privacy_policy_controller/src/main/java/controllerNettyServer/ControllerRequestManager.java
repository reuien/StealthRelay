package controllerNettyServer;

import com.google.protobuf.ByteString;
import controllerProtocol.ControllerProtocol.*;
import exceptions.CouldNotReceiveException;
import exceptions.CouldNotStoreException;
import exceptions.InvalidQueryException;
import exceptions.QueryFailedException;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import streamHandling.*;

import javax.crypto.*;
import java.io.*;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;


public class ControllerRequestManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerRequestManager.class);
    private PrivacyControllerManager pcManager;
    private PrivacyController curController;

    public ControllerRequestManager() {
        this.pcManager = new PrivacyControllerManager();
    }

    public void keyAgreement(ChannelHandlerContext ctx, String usrName, byte[] receivedPubKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, CouldNotStoreException,
            QueryFailedException, CertificateException, IOException, KeyStoreException, NoSuchProviderException {

        curController = pcManager.getPrivacyControllerForUsr(usrName);
        byte[] sendPubKeyBytes = curController.keyAgreement(usrName, receivedPubKeyBytes);

        ctx.writeAndFlush(createPubKeyResponse(usrName, sendPubKeyBytes));
    }

    public void createStream(ChannelHandlerContext ctx, String usrName, long streamId, byte[] encStreamKey)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, CertificateException, IOException,
            KeyStoreException, CouldNotStoreException, QueryFailedException {

        curController = pcManager.getPrivacyControllerForUsr(usrName);
        if (curController.storeStreamKey(usrName, streamId, encStreamKey)){
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
        }else {
            ctx.writeAndFlush(createErrorResponse("ERROR", 1000));
        }
    }

    public void deleteStream(ChannelHandlerContext ctx, String usrName, long streamId) throws CouldNotStoreException, QueryFailedException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        curController = pcManager.getPrivacyControllerForUsr(usrName);
        curController.refreshProfile();
        //删除密钥；删除所有关联的隐私策略、联邦策略
        if (curController.deleteStreamKey(usrName, streamId)){
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
        }else {
            ctx.writeAndFlush(createErrorResponse("ERROR", 1000));
        }
        ctx.writeAndFlush(createSuccessResponse("Success", 1101));
    }

    public void createPrivacyPolicy(ChannelHandlerContext ctx, String consumerUsrName, String ownerUsrName, long privacyPolicyId,
                                    long streamID, long startTime, long endTime, long minGranularity) {
        try {
            curController = pcManager.getPrivacyControllerForUsr(ownerUsrName);
            curController.refreshProfile();
            PrivacyPolicy pp = new PrivacyPolicy(consumerUsrName, ownerUsrName, privacyPolicyId,
                    streamID, new Date(startTime), new Date(endTime), minGranularity);
            if (curController.addPrivacyPolicy(pp)){
                ctx.writeAndFlush(createSuccessResponse("Success", 1101));
                System.out.println("__Response send over__");
            }else {
                ctx.write(createErrorResponse("ERROR", 1000));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }

    public void deletePrivacyPolicy(ChannelHandlerContext ctx, String ownerUsrName, long privacyPolicyId) throws Exception {
        curController = pcManager.getPrivacyControllerForUsr(ownerUsrName);
        curController.refreshProfile();
        if (curController.deletePrivacyPolicy(ownerUsrName, privacyPolicyId)){
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("__Response send over__");
        }else {
            ctx.write(createErrorResponse("ERROR", 1000));
        }
    }

    public void requestToken(ChannelHandlerContext ctx, String consumerUsrName, String ownerUsrName, long privacyPolicyID,
                             long streamID, long startTime, long endTime, long granularity) {
        try {
            /*System.out.println(consumerUsrName);
            System.out.println(ownerUsrName);
            System.out.println(privacyPolicyID);
            System.out.println(streamID);
            System.out.println(new Date(startTime));
            System.out.println(new Date(endTime));
            System.out.println(granularity);
            System.out.println();*/

            curController = pcManager.getPrivacyControllerForUsr(ownerUsrName);
            curController.refreshProfile();
            Request request = new Request(consumerUsrName, ownerUsrName, privacyPolicyID, streamID, new Date(startTime), new Date(endTime), granularity);
            PrivacyPolicy pp = curController.getPrivacyPolicy(privacyPolicyID);
            //System.out.println(pp.toString());
            Token tk = curController.createTokenForRequest(pp, request);
            if (tk == null) {
                String reason = curController.explainRequestCheckFailure(pp, request);
                ctx.writeAndFlush(createErrorResponse("控制器未颁发查询令牌：" + reason, 1000));
                return;
            }
            //System.out.println(tk == null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(tk.getNodes());
            byte[] nodes_content = bos.toByteArray();

            TokenResponse tr = TokenResponse.newBuilder()
                    .setTokenId(tk.getTokenID())
                    .setUsrName(tk.getUsrName())
                    .setStreamId(tk.getStreamID())
                    .setStreamStartTime(tk.getStreamStartDate().getTime())
                    .setChunkSize(tk.getChunkSize()).setGranularity(tk.getGranularity())
                    .setNodesContent(ByteString.copyFrom(nodes_content))
                    .setDepth(tk.getTreeDepth())
                    .setMacKey(ByteString.copyFrom(tk.getMacKey()))
                    .build();
            ctx.writeAndFlush(
                    ResponseMessage.newBuilder()
                    .setType(MessageResponseType.Token_Response)
                    .setTokenResponse(tr)
                    .build()
            );
            System.out.println("__Response send over__");
        } catch (Exception e) {
            System.out.println("!!! Request_Token 处理异常: " + e);
            e.printStackTrace();
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }

    public void createFederationPolicy(ChannelHandlerContext ctx, String consumerName, String ownerName,
                                       long fpId, long streamID, long startTime, long endTime) {
        try {
            curController = pcManager.getPrivacyControllerForUsr(ownerName);
            curController.refreshProfile();
            FederationPolicy fp = new FederationPolicy(consumerName, ownerName, fpId, streamID, startTime, endTime);

            if (curController.addFederationPolicy(fp)){
                ctx.writeAndFlush(createSuccessResponse("Success", 1101));
                System.out.println("__Response send over__");
            }else {
                ctx.write(createErrorResponse("ERROR", 1000));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }

    public void deleteFederationPolicy(ChannelHandlerContext ctx, String ownerName, long fpId) throws Exception {
        curController = pcManager.getPrivacyControllerForUsr(ownerName);
        curController.refreshProfile();
        if (curController.deleteFederationPolicy(ownerName, fpId)){
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("__Response send over__");
        }else {
            ctx.write(createErrorResponse("ERROR", 1000));
        }
    }

    public void requestFederationToken(ChannelHandlerContext ctx, byte[] content) throws IOException,
            ClassNotFoundException, CouldNotReceiveException, CouldNotStoreException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException, InvalidQueryException, QueryFailedException {
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        ObjectInputStream ois = new ObjectInputStream(bis);
        FederationRequest fq = (FederationRequest) ois.readObject();

        FederationToken ft = pcManager.getFederationToken(fq);

        System.out.println(ft.toString());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        byte[] ft_content = bos.toByteArray();

        FederationTokenResponse ftr = FederationTokenResponse.newBuilder()
                .setFederationTokenContent(ByteString.copyFrom(ft_content))
                .build();
        ctx.writeAndFlush(
                ResponseMessage.newBuilder()
                        .setType(MessageResponseType.FederationToken_Response)
                        .setFederationTokenResponse(ftr)
                        .build()
        );
        System.out.println("__Response send over__");

    }



/*    public void createToken(ChannelHandlerContext ctx, long tokenId, byte[] nodes_content, int depth) {
        try {
            System.out.println("Receive: ");
            System.out.println("tokenId: "+tokenId);
            System.out.println("nodes_content: "+Arrays.toString(nodes_content));
            System.out.println("depth: "+depth);

            ByteArrayInputStream bis = new ByteArrayInputStream(nodes_content);
            ObjectInputStream ois = new ObjectInputStream(bis);
            ArrayList<SeedNode> readNodes = (ArrayList<SeedNode>) ois.readObject();
            System.out.println(readNodes);
            for (int i = 0; i < readNodes.size(); i++) {
                SeedNode temp = readNodes.get(i);
                System.out.println(i+": ");
                System.out.println("depth: "+temp.getDepth()+"  num: "+temp.getNodeNr()+"  seed: "+ Arrays.toString(temp.getSeed()));
            }
            ctx.writeAndFlush(createSuccessResponse("Success", 1101));
            System.out.println("Response send over ");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            ctx.write(createErrorResponse("Message: " + e.getMessage(), 1000));
        }
    }*/

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
