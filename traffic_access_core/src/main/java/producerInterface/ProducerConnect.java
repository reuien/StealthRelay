package producerInterface;

import com.google.protobuf.ByteString;
import producerInterface.producerProtocol.ProducerProtocol.*;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


public class ProducerConnect implements Closeable, AutoCloseable {

    private static final int NONCE_SIZE = 12;
    private static SecureRandom randomSecureRandom = new SecureRandom();
    private Socket net;
    private OutputStream outStream;
    private InputStream inStream;

    private byte[] buffer = new byte[1024 * 8];

    public ProducerConnect(String ip, int port) throws IOException {
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

    public boolean registerProducer(String owner, long producerId, String producerName, String producerAddress, int producerPort) throws IOException {
        RegisterProducer rp = RegisterProducer.newBuilder()
                .setUserName(owner)
                .setProducerId(producerId)
                .setProducerName(producerName)
                .setProducerAddress(producerAddress)
                .setProducerPort(producerPort)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Register_Producer)
                .setRegisterProducer(rp)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public boolean linkProducer(String owner, long producerId, String producerName) throws IOException {
        LinkProducer lp = LinkProducer.newBuilder()
                .setUserName(owner)
                .setProducerId(producerId)
                .setProducerName(producerName)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Link_Producer)
                .setLinkProducer(lp)
                .build();
        writeRequest(req);

        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
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

    public boolean CreateStream(String ownerName, long producerId, long streamId, SecretKey streamMasterKey) throws
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
                .setProducerId(producerId)
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

            // 用户A生成共享密钥
            KeyAgreement keyAgreement = KeyAgreement.getInstance("X25519");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(KeyFactory.getInstance("X25519")
                    .generatePublic(new X509EncodedKeySpec(receivedPubKeyBytes)), true);

            // 生成SecretKey本地密钥(会话密钥)
            byte[] sharedSecretKey = keyAgreement.generateSecret();


//            byte[] encStreamKey = encryptAESGcm(minKey(sharedSecretKey), streamMasterKey.getEncoded());
            byte[] encStreamKey = encryptAESGcm(sharedSecretKey, streamMasterKey.getEncoded());



            // 通过KeyFactory创建公钥



            // 生成SecretKey本地密钥(会话密钥)

            CreateStream cs = CreateStream.newBuilder()
                    .setUserName(ownerName)
                    .setProducerId(producerId)
                    .setStreamId(streamId)
                    .setPubKey(ByteString.copyFrom(encStreamKey))
                    .build();

            RequestMessage req1 = RequestMessage.newBuilder()
                    .setType(MessageRequestType.Create_Stream)
                    .setCreateStream(cs)
                    .build();
            writeRequest(req1);
            ResponseMessage msg1 = loadResponse();
            return msg1.hasSuccessResponse();
        }else {
            System.out.println("KA ERROR");
            return false;
        }
    }

    public boolean uploadData(String usrName, long producerId, long streamId, String description, long startTime,
                           long endTime, long chunkSize, long resolutionLevels) throws IOException {
        UploadData ud = UploadData.newBuilder()
                .setUserName(usrName)
                .setProducerId(producerId)
                .setStreamId(streamId)
                .setDescription(description)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setChunkSize(chunkSize)
                .setResolutionLevels(resolutionLevels)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Upload_Data)
                .setUploadData(ud)
                .build();
        writeRequest(req);
        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public boolean uploadDataLive(String usrName, long producerId, long streamId, String description, long startTime,
                              long endTime, long chunkSize, long resolutionLevels) throws IOException {
        UploadDataLive udl = UploadDataLive.newBuilder()
                .setUserName(usrName)
                .setProducerId(producerId)
                .setStreamId(streamId)
                .setDescription(description)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setChunkSize(chunkSize)
                .setResolutionLevels(resolutionLevels)
                .build();
        RequestMessage req = RequestMessage.newBuilder()
                .setType(MessageRequestType.Upload_Data_Live)
                .setUploadDataLive(udl)
                .build();
        writeRequest(req);
        ResponseMessage msg = loadResponse();
        return msg.hasSuccessResponse();
    }

    public void close() throws IOException {
        net.close();
    }
}