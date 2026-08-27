package controllerInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import streamHandling.FederationRequest;
import streamHandling.FederationToken;
import streamHandling.Token;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

public class ControllerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerInterface.class);
    private final String ip;
    private final int port;
    private ControllerConnect connect;
    private Random rand = new Random();

    public ControllerInterface(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        createNewConnection();
    }

    private void createNewConnection() throws IOException {
        connect = new ControllerConnect(ip, port);
    }

    public boolean CreateStream(String ownerName, long streamId, SecretKey streamMasterKey) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return connect.CreateStream(ownerName, streamId, streamMasterKey);

    }

    public boolean createPolicy(String consumerName, String ownerName, long policyId, long streamId, Date startTime, Date endTime, long minGranularity) throws IOException {
        return connect.createPolicy(consumerName, ownerName, policyId, streamId, startTime, endTime, minGranularity);
    }

    public boolean createFederationPolicy(String consumerName, String ownerName, long policyId, long streamId, long startTime, long endTime) throws IOException {
        return connect.createFederationPolicy(consumerName, ownerName, policyId, streamId, startTime, endTime);
    }

    public Token sendRequest(String consumerName, String ownerName, long policyId, long streamId, Date startTime, Date endTime, long granularity) throws IOException, ClassNotFoundException {
        return connect.sendRequest(consumerName, ownerName, policyId, streamId, startTime, endTime, granularity);
    }

    public FederationToken getFederationToken(FederationRequest fq) throws IOException, ClassNotFoundException {
        return connect.sendFederationRequest(fq);
    }



}
