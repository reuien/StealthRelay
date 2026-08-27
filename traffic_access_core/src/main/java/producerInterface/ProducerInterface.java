package producerInterface;

import producerInterface.ProducerConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import producerInterface.producerProtocol.ProducerProtocol;

import javax.crypto.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

public class ProducerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerInterface.class);
    private final String ip;
    private final int port;
    private ProducerConnect connect;
    private Random rand = new Random();

    public ProducerInterface(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        createNewConnection();
    }

    private void createNewConnection() throws IOException {
        connect = new ProducerConnect(ip, port);
    }

    public boolean registerProducer(String owner, long producerId, String producerName, String producerAddress, int producerPort) throws IOException {
        return connect.registerProducer(owner, producerId, producerName, producerAddress, producerPort);
    }

    public boolean linkProducer(String owner, long producerId, String producerName) throws IOException {
        return connect.linkProducer(owner, producerId, producerName);
    }

    public boolean CreateStream(String ownerName, long producerId, long streamId, SecretKey streamMasterKey) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return connect.CreateStream(ownerName, producerId, streamId, streamMasterKey);
    }

    public boolean uploadData(String usrName, long producerId, long streamId, String description, long startTime,
                              long endTime, long chunkSize, long resolutionLevels) throws IOException {
       return connect.uploadData(usrName, producerId, streamId, description, startTime, endTime, chunkSize, resolutionLevels);
    }

    public boolean uploadDataLive(String usrName, long producerId, long streamId, String description, long startTime,
                                  long endTime, long chunkSize, long resolutionLevels) throws IOException {
        return connect.uploadDataLive(usrName, producerId, streamId, description, startTime, endTime, chunkSize, resolutionLevels);
    }


}
