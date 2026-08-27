package controllerInterface;

import com.google.protobuf.ByteString;
import controllerInterface.controllerProtocol.ControllerProtocol;
import controllerInterface.controllerProtocol.ControllerProtocol.*;
import keyDerivation.SeedNode;
import prg.IPRG;
import streamHandling.FederationRequest;
import streamHandling.FederationToken;
import streamHandling.Token;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;

public class ControllerConnect implements Closeable, AutoCloseable {

    private static final int NONCE_SIZE = 12;
    private static SecureRandom randomSecureRandom = new SecureRandom();
    private Socket net;
    private OutputStream outStream;
    private InputStream inStream;

    private byte[] buffer = new byte[1024 * 8];

    public ControllerConnect(String ip, int port) throws IOException {
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

    private void writeRequest(ControllerProtocol.RequestMessage requestMessage) throws IOException {
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

    public static byte[] encryptAESGcm(byte[] key, byte[] data, int lenData) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            ShortBufferException, IllegalBlockSizeException {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] ivBytes = new byte[NONCE_SIZE];
        randomSecureRandom.nextBytes(ivBytes);

        GCMParameterSpec gcm = new GCMParameterSpec(cipher.getBlockSize() * 8, ivBytes);
        byte[] finalResult = new byte[ivBytes.length + gcm.getTLen() / 8 + lenData];

        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, gcm);
        System.arraycopy(ivBytes, 0, finalResult, 0, ivBytes.length);
        cipher.doFinal(data, 0, lenData, finalResult, ivBytes.length);
        return finalResult;
    }
    public static byte[] encryptAESGcm(byte[] key, byte[] data) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            ShortBufferException, IllegalBlockSizeException {
        return encryptAESGcm(key,data, data.length);
    }

    public static byte[] minKey(byte[] key) {
        byte[] out = new byte[32];
        System.arraycopy(key, 0, out, 0, out.length);
        for (int i = out.length; i < key.length; i++) {
            out[i - out.length] ^= key[i];
        }
        return out;
    }


    public boolean CreateStream(String ownerName, long streamId, SecretKey streamMasterKey) throws
            NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519");

        // 生成DH密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] sendPubKeyBytes = publicKey.getEncoded();

        KeyAgreementRequest ka = KeyAgreementRequest.newBuilder()
                .setUserName(ownerName)
                .setPubKey(ByteString.copyFrom(sendPubKeyBytes))
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Key_Agreement)
                .setKeyAgreement(ka)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        if (msg.hasPubKeyResponse()){
            PubKeyResponse pr = msg.getPubKeyResponse();
            byte[] receivedPubKeyBytes = pr.getPubKey().toByteArray();

            KeyAgreement keyAgreement = KeyAgreement.getInstance("X25519");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(KeyFactory.getInstance("X25519")
                    .generatePublic(new X509EncodedKeySpec(receivedPubKeyBytes)), true);

            // 生成SecretKey本地密钥(会话密钥)
            byte[] sharedSecretKey = keyAgreement.generateSecret();



            byte[] encStreamKey = encryptAESGcm(sharedSecretKey, streamMasterKey.getEncoded());
//            byte[] encStreamKey = encryptAESGcm(minKey(sharedSecretKey), streamMasterKey.getEncoded());

            CreateStream cs = CreateStream.newBuilder()
                    .setUserName(ownerName)
                    .setStreamId(streamId)
                    .setPubKey(ByteString.copyFrom(encStreamKey))
                    .build();

            RequestMessage req1 = RequestMessage.newBuilder()
                    .setType(MessageRequestType.Create_Stream)
                    .setCreateStream(cs)
                    .build();
            writeRequest(req1);
            ResponseMessage msg1 = loadResponse();
            System.out.println("Con KA OK");
            return msg1.hasSuccessResponse();
        }else {
            System.out.println("KA ERROR");
            return false;
        }
    }

    public boolean createPolicy(String consumerName, String ownerName, long policyId, long streamId, Date startTime, Date endTime, long minGranularity) throws IOException {
        CreatePrivacyPolicy cpp = CreatePrivacyPolicy.newBuilder()
                .setConsumerName(consumerName)
                .setOwnerName(ownerName)
                .setPolicyId(policyId)
                .setStreamId(streamId)
                .setStartTime(startTime.getTime())
                .setEndTime(endTime.getTime())
                .setMinGranularity(minGranularity)
                .build();

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Create_PrivacyPolicy)
                .setCreatePrivacyPolicy(cpp)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public boolean createFederationPolicy(String consumerName, String ownerName, long policyId, long streamId, long startTime, long endTime) throws IOException {
        CreateFederationPolicy cfp = CreateFederationPolicy.newBuilder()
                .setConsumerName(consumerName)
                .setOwnerName(ownerName)
                .setPolicyId(policyId)
                .setStreamId(streamId)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .build();

        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Create_FederationPolicy)
                .setCreateFederationPolicy(cfp)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public Token sendRequest(String consumerName, String ownerName, long policyId, long streamId, Date startTime, Date endTime, long granularity) throws IOException, ClassNotFoundException {
        RequestToken rt = RequestToken.newBuilder()
                .setConsumerName(consumerName)
                .setOwnerName(ownerName)
                .setPolicyId(policyId)
                .setStreamId(streamId)
                .setStartTime(startTime.getTime())
                .setEndTime(endTime.getTime())
                .setGranularity(granularity)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Request_Token).setRequestToken(rt)
                .build();
        writeRequest(req);
        ResponseMessage msg = loadResponse();
        if (msg.hasErrorResponse()) {
            throw new IOException(msg.getErrorResponse().getMessage());
        }
        if (msg.hasTokenResponse()){
            TokenResponse tr = msg.getTokenResponse();
            ByteArrayInputStream bis = new ByteArrayInputStream(tr.getNodesContent().toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            ArrayList<SeedNode> nodes = (ArrayList<SeedNode>) ois.readObject();
            return new Token(tr.getTokenId(), tr.getUsrName(), tr.getStreamId(), new Date(tr.getStreamStartTime()),
                    tr.getChunkSize(), tr.getGranularity(), nodes, tr.getDepth(), tr.getMacKey().toByteArray());
        }
        return null;
    }

    public FederationToken sendFederationRequest(FederationRequest fq) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(fq);
        byte[] fq_content = bos.toByteArray();

        RequestFederationToken rft = RequestFederationToken.newBuilder()
                .setRequestContent(ByteString.copyFrom(fq_content))
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Request_FederationToken).setRequestFederationToken(rft)
                .build();
        writeRequest(req);
        ResponseMessage msg = loadResponse();
        if (msg.hasFederationTokenResponse()){
            FederationTokenResponse ftr = msg.getFederationTokenResponse();
            ByteArrayInputStream bis = new ByteArrayInputStream(ftr.getFederationTokenContent().toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            FederationToken ft = (FederationToken) ois.readObject();
            return ft;
        }
        return null;
    }

    public void close() throws IOException {
        net.close();
    }
}
