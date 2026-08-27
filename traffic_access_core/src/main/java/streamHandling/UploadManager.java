package streamHandling;

import dataServerInterface.DataServerInterface;
import exceptions.ChunkAlreadyWrittenException;
import exceptions.CouldNotStoreException;
import exceptions.WriteException;
import keyManagement.StreamKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UploadManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadManager.class);
    private final String usrName;
    private final Stream stream;
    private final StreamKeyManager streamKeyManager;
    private final DataServerInterface dataServerInterface;
    private Chunk curChunk;

    public UploadManager(String usrName, Stream stream, StreamKeyManager streamKeyManager, DataServerInterface dataServerInterface) {
        this.usrName = usrName;
        this.stream = stream;
        this.streamKeyManager = streamKeyManager;
        this.dataServerInterface = dataServerInterface;
        this.curChunk = new Chunk(stream.getId(), stream.getStartDate().getTime(), stream.getChunkSize(), TimeUtil.getChunkIdAtTime(stream, stream.getStartDate()));
    }

    public void writeDataPointToStream(DataPoint dataPoint) throws WriteException {
        long chunkId = TimeUtil.getChunkIdAtTime(stream, dataPoint.getTimestamp().getTime());
        if (chunkId < curChunk.getChunkID()) {
            throw new ChunkAlreadyWrittenException(dataPoint.getTimestamp(), dataPoint.getValue(), "Chunk already written");
        } else if (chunkId == curChunk.getChunkID()) {
            curChunk.addDataPoint(dataPoint.getTimestamp(), dataPoint.getValue());
        } else {
            sendChunk(curChunk);

            for (long id = curChunk.getChunkID() + 1; id < chunkId; id++) {
                sendChunk(new Chunk(stream.getId(), stream.getStartDate().getTime(), stream.getChunkSize(), id));
            }
            curChunk = new Chunk(stream.getId(), stream.getStartDate().getTime(), stream.getChunkSize(), chunkId);
            curChunk.addDataPoint(dataPoint.getTimestamp(), dataPoint.getValue());
        }
    }

    public void flush() throws CouldNotStoreException {
        sendChunk(curChunk);
        curChunk = new Chunk(stream.getId(), stream.getStartDate().getTime(), stream.getChunkSize(), curChunk.getChunkID() + 1);
    }

    public void terminate() throws CouldNotStoreException {
        this.flush();
    }

    public long getStreamID() {
        return this.stream.getId();
    }

    private void sendChunk(Chunk curChunk) {
        long chunkId = curChunk.getChunkID();
        LOGGER.debug("Finalizing chunk " + chunkId);
        curChunk.finalizeChunk();
        LOGGER.debug("Encrypting metadata for chunk " + chunkId);
        Digest encryptedDigest = null;
        EncryptedChunk encryptedChunk = null;
        try {
            LOGGER.debug("Encrypting chunk " + chunkId);
            //Encryption.CiphertextPairNew pair = Encryption.encryptChunkAndDigest(stream.getId(), chunkId, curChunk, streamKeyManager);
            Encryption.CiphertextPairNew pair = Encryption.encryptChunkAndDigestNew(stream.getId(), chunkId, curChunk, streamKeyManager);
            encryptedDigest = pair.encryptedDigest;
            encryptedChunk = pair.encryptedChunk;
        } catch (Exception e) {
            LOGGER.error("Could not encrypt chunk.", e);
            // TODO: raise a useful exception.
            throw new RuntimeException("Could encrypt chunk " + chunkId + " for stream " + stream.getId() +
                    ". Message:" + e.getMessage());
        }

        long serverChunkId = -1;
        try {
            LOGGER.debug("Sending chunk " + chunkId + " to server.");
            //serverChunkId = this.dataServerInterface.addChunkNew(usrName, stream.getId(), chunkId, encryptedChunk, encryptedDigest);
            serverChunkId = this.dataServerInterface.addChunkNewDigest(usrName, stream.getId(), chunkId, encryptedChunk, encryptedDigest);
            if (serverChunkId != chunkId) {
                LOGGER.error("Server reported a different chunkId than we expected for stream " + stream.getId() +
                        "Expected " + curChunk + " got " + serverChunkId + ". This means the understanding of the " +
                        "stream got inconsistent between server and client - can't handle that");
                // TODO: raise a useful exception.
                throw new RuntimeException("Server reported a different chunkId than we expected. For stream " +
                        stream.getId());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

/*        // 更新结束时间
        associatedStream.setEndDate(new Date(curChunk.getEndTime()));
        profile.addStream(associatedStream);
        try {
            if (!profile.syncProfile(false)) {
                throw new CouldNotStoreException("无法将流存入配置文件");
            }
        } catch (Exception e) {
            throw new CouldNotStoreException("错误");
        }*/

    }

}
