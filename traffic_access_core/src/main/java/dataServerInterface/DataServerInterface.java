package dataServerInterface;

import exceptions.CouldNotStoreException;
import exceptions.CouldNotReceiveException;
import exceptions.InvalidQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import streamHandling.Digest;
import streamHandling.EncryptedChunk;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class DataServerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataServerInterface.class);
    private final String ip;
    private final int port;
    private DataServerConnect serverConnect;
    private Random rand = new Random();


    public DataServerInterface(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        createNewConnection();
    }

    private void createNewConnection() throws IOException {
        serverConnect = new DataServerConnect(ip, port);
    }

    public long createStream(String str) throws IOException, CouldNotStoreException {
        long streamId = rand.nextLong();
        if(!serverConnect.createStream(str, streamId)){
            System.out.println("Error");
            throw new CouldNotStoreException("Create Stream Failed");
        }else{
            System.out.println("Create stream success, ID: "+streamId);
            return streamId;
        }
    }

    public boolean deleteStream(String str, long streamId) throws IOException {
        if(!serverConnect.deleteStream(str, streamId)){
            System.out.println("Error");
            throw new RuntimeException("Delete Stream Failed");
        }else{
            System.out.println("Delete Stream Success, ID: "+streamId);
            return true;
        }
    }

    public long addChunkNew(String usrName, long correspondingStreamID, long chunkId, EncryptedChunk encryptedChunk, Digest encryptedDigest) throws IOException {
        /*
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(encryptedChunk);
        byte[] encryptedChunkBytes = bos.toByteArray();

        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
        oos1.writeObject(encryptedDigest);
        byte[] encryptedDigestBytes = bos1.toByteArray();

         */
        if(!serverConnect.addChunkNew(usrName, correspondingStreamID, chunkId, encryptedChunk, encryptedDigest)){
            System.out.println("Error");
            throw new RuntimeException("Save Chunk Failed");
        }else{
            System.out.println("Save Chunk Success");
            return encryptedChunk.getChunkId();
        }
    }

    public long addChunkNewDigest(String usrName, long correspondingStreamID, long chunkId, EncryptedChunk encryptedChunk, Digest encryptedDigest) throws IOException {
        /*
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(encryptedChunk);
        byte[] encryptedChunkBytes = bos.toByteArray();

        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
        oos1.writeObject(encryptedDigest);
        byte[] encryptedDigestBytes = bos1.toByteArray();

         */
        if(!serverConnect.addChunkNewDigest(usrName, correspondingStreamID, chunkId, encryptedChunk, encryptedDigest)){
            System.out.println("Error");
            throw new RuntimeException("Save Chunk Failed");
        }else{
            System.out.println("Save Chunk Success");
            return encryptedChunk.getChunkId();
        }
    }


    public List<EncryptedChunk> getChunks(String usrName, long streamId, long from, long to) throws CouldNotReceiveException {
        List<EncryptedChunk> result;
        try {
            result = serverConnect.getChunks(usrName, streamId, from, to);
        } catch (IOException e) {
            LOGGER.error("Tried to get chunks from stream id " + streamId + " got error.", e);
            try {
                createNewConnection();
            } catch (IOException ex) {
                LOGGER.error("Could not create a new connection to server", ex);
            }
            throw new CouldNotReceiveException(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Tried to get chunks from stream id " + streamId + " got error.", e);
            throw new CouldNotReceiveException(e.getMessage());
        }
        return result;
    }

    public List<Digest> getStatisticalData(String usrName, long streamId, long chunkIdFrom, long chunkIdTo,
                                           int granularity) throws InvalidQueryException {
        List<Digest> result;
        try {
            result = serverConnect.getStatistics(usrName, streamId, chunkIdFrom, chunkIdTo, granularity);
        } catch (IOException e) {
            LOGGER.error("Tried to get statistics for  stream id " + streamId + " got error.", e);
            try {
                createNewConnection();
            } catch (IOException ex) {
                LOGGER.error("Could not create a new connection to server", ex);
            }
            throw new InvalidQueryException(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Tried to get statistics for  stream id " + streamId + " got error.", e);
            throw new InvalidQueryException(e.getMessage());
        }
        return result;
    }

    public List<Digest> getStatisticalNewDigest(String usrName, long streamId, long chunkIdFrom, long chunkIdTo,
                                           int granularity) throws InvalidQueryException {
        List<Digest> result;
        try {
            result = serverConnect.getStatisticsNewDigest(usrName, streamId, chunkIdFrom, chunkIdTo, granularity);
        } catch (IOException e) {
            LOGGER.error("Tried to get statistics for  stream id " + streamId + " got error.", e);
            try {
                createNewConnection();
            } catch (IOException ex) {
                LOGGER.error("Could not create a new connection to server", ex);
            }
            throw new InvalidQueryException(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Tried to get statistics for  stream id " + streamId + " got error.", e);
            throw new InvalidQueryException(e.getMessage());
        }
        return result;
    }

    public Digest getStatisticAll(String usrName, long streamId, long chunkIdFrom, long chunkIdTo) throws IOException {
        Digest res = serverConnect.getStatisticAll(usrName, streamId, chunkIdFrom, chunkIdTo);
        return res;
    }

    public Digest getStatisticAllNew(String usrName, long streamId, long chunkIdFrom, long chunkIdTo) throws IOException {
        Digest res = serverConnect.getStatisticAllNew(usrName, streamId, chunkIdFrom, chunkIdTo);
        return res;
    }
    /*public long createToken(long tokenId, ArrayList<SeedNode> seedNodes, int depth) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(seedNodes);
        byte[] nodes_content = bos.toByteArray();

        if(!connect.createToken(tokenId, nodes_content, depth)){
            System.out.println("Error");
            throw new RuntimeException("Create Token Failed");
        }else{
            System.out.println("Create Token Success");
            return tokenId;
        }

    }
    public boolean deleteToken(long tokenId) throws IOException {
        if(!connect.deleteToken(tokenId)){
            System.out.println("Error");
            throw new RuntimeException("Delete Token Failed");
        }else{
            System.out.println("Delete Token Success");
            return true;
        }
    }*/

}
