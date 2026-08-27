package streamHandling;

import crypto.ChunkCrypto.ChunkEncryption;
import exceptions.QueryFailedException;
import keyManagement.StreamKeyManager;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class ChunkForDC {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkForDC.class);
    private final String stream;
    private final long chunkID;
    private final long startTime;
    private final long endTime;
    private final HashMap<Long, DataPoint> values;
    private boolean finalized = false;
    public ChunkForDC(String stream, Date startDate, long chunkSize, long chunkID, byte[] encryptedData, StreamKeyManager streamKeyManager)
            throws QueryFailedException {
        this.stream = stream;
        if (chunkID < 0) {
            throw new RuntimeException("Chunk ID 不能小于0! ");
        }
        this.chunkID = chunkID;
        this.startTime = TimeUtilForDC.getChunkStartTime(startDate, chunkSize, this.chunkID);
        this.endTime = TimeUtilForDC.getChunkEndTime(startDate, chunkSize, this.chunkID);
        this.finalized = true;

        byte[] valueBytes;
        try {
            valueBytes = ChunkEncryption.decryptAESGcm(streamKeyManager.getChunkEncryptionKey(chunkID),
                    encryptedData);
        } catch (InvalidKeyException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
            LOGGER.error("Could not decrypt chunk.", e);
            throw new QueryFailedException(QueryFailedException.FailReason.COULD_NOT_DECRYPT_CHUNK, e.getMessage());
        }
        this.values = SerializationUtils.deserialize(valueBytes);
    }

    public String getStream() {
        return stream;
    }

    public long getChunkID() {
        return chunkID;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public Collection<DataPoint> getValues() {
        return values.values();
    }


    @Override
    public String toString() {
        return "数据块 " + chunkID +
                ", 时间：" + new Date(startTime) +
                "—" + new Date(endTime) +
                ", 数据：" + values.values() +
                " ";
    }
}
