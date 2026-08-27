package streamHandling;

import crypto.ChunkCrypto.ChunkEncryption;
import exceptions.ChunkAlreadyWrittenException;
import exceptions.DuplicateDataPointException;
import exceptions.QueryFailedException;
import exceptions.WrongChunkException;
import keyManagement.CachedKeys;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class Chunk {
    private static final Logger LOGGER = LoggerFactory.getLogger(Chunk.class);
    private final long streamId;
    private final long chunkID;
    private final long startTime;
    private final long endTime;
    private final HashMap<Long, DataPoint> values;
    private boolean finalized = false;

//    public Chunk(long streamId, long streamStartTime, long chunkSize, long chunkID, byte[] encryptedData, StreamKeyManager streamKeyManager)
//            throws QueryFailedException {
//        this.streamId = streamId;
//        if (chunkID < 0) {
//            throw new RuntimeException("数据块ID不能小于0! ");
//        }
//        this.chunkID = chunkID;
//        this.startTime = TimeUtil.getChunkStartTime(streamStartTime, chunkSize, this.chunkID);
//        this.endTime = TimeUtil.getChunkEndTime(streamStartTime, chunkSize, this.chunkID);
//        this.finalized = true;
//
//        byte[] valueBytes;
//        try {
//            valueBytes = ChunkEncryption.decryptAESGcm(streamKeyManager.getChunkEncryptionKey(chunkID),
//                    encryptedData);
//        } catch (InvalidKeyException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException |
//                 InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
//            LOGGER.error("无法解密数据块", e);
//            throw new QueryFailedException(QueryFailedException.FailReason.COULD_NOT_DECRYPT_CHUNK, e.getMessage());
//        }
//        this.values = SerializationUtils.deserialize(valueBytes);
//    }

    public Chunk(long streamId, long streamStartTime, long chunkSize, long chunkID) {
        this.streamId = streamId;
        if (chunkID < 0) {
            throw new RuntimeException("数据块ID不能小于0! ");
        }
        this.chunkID = chunkID;
        this.startTime = TimeUtil.getChunkStartTime(streamStartTime, chunkSize, this.chunkID);
        this.endTime = TimeUtil.getChunkEndTime(streamStartTime, chunkSize, this.chunkID);
        this.values = new HashMap<>();
    }

    public long getStreamId() {
        return streamId;
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

    public void addDataPoint(Date timestamp, long value) throws WrongChunkException, ChunkAlreadyWrittenException,
            DuplicateDataPointException {

        if (timestamp.getTime() < startTime || timestamp.getTime() > endTime) {
            throw new WrongChunkException(timestamp, new Date(this.startTime), new Date(this.endTime), value);
        }

        synchronized (this) {
            if (finalized) {
                throw new ChunkAlreadyWrittenException(timestamp, value);
            } else if (values.containsKey(timestamp.getTime())) {
                throw new DuplicateDataPointException(timestamp, values.get(timestamp.getTime()).getValue(), value);
            } else {
                DataPoint dataPoint = new DataPoint(timestamp, value);
                values.put(timestamp.getTime(), dataPoint);
                LOGGER.debug("数据块" + this.chunkID + "添加数据" + dataPoint.toString());
            }
        }
    }

    public void finalizeChunk() {
        synchronized (this) {
            finalized = true;
        }
        LOGGER.debug("数据块" + this.chunkID + " 已完成");
    }
    public long calculateSum(Collection<DataPoint> dataPoints) {
        long val = 0;
        for (DataPoint dataPoint : dataPoints) {
            val += dataPoint.getValue();
        }
        return val;
    }
    public long calculateCount(Collection<DataPoint> dataPoints) {
        return dataPoints.size();
    }
    public long calculateSquare(Collection<DataPoint> dataPoints) {
        long val = 0;
        for (DataPoint dataPoint : dataPoints) {
            val += dataPoint.getValue() * dataPoint.getValue();
        }
        return val;
    }

    public long[] calculateCountTypes(Collection<DataPoint> dataPoints) {
        long[] val = new long[6];
        Arrays.fill(val, 0);
        for (DataPoint dataPoint : dataPoints) {
            if (dataPoint.getValue() < 60){
                val[0] += 1;
            } else if (dataPoint.getValue() >= 60 && dataPoint.getValue() < 70) {
                val[1] += 1;
            } else if (dataPoint.getValue() >= 70 && dataPoint.getValue() < 80) {
                val[2] += 1;
            } else if (dataPoint.getValue() >= 80 && dataPoint.getValue() < 90) {
                val[3] += 1;
            } else if (dataPoint.getValue() >= 90 && dataPoint.getValue() < 100) {
                val[4] += 1;
            } else if (dataPoint.getValue() >= 100) {
                val[5] += 1;
            }
        }
        //System.out.println("count[]: "+Arrays.toString(val));
        return val;
    }


    public Digest calculateDigest(){
        Collection<DataPoint> dps = getValues();
        long sum = calculateSum(dps);
        long count = calculateCount(dps);
        long square = calculateSquare(dps);
        return new Digest(streamId, chunkID, chunkID, sum, count, square, false, false);
    }

    public Digest calculateDigestNew(){
        Collection<DataPoint> dps = getValues();
        long sum = calculateSum(dps);
        long count = calculateCount(dps);
        long square = calculateSquare(dps);
        long[] counts = calculateCountTypes(dps);
        //System.out.println("long[] counts: "+Arrays.toString(counts));
        return new Digest(streamId, chunkID, chunkID, sum, count, square, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], false, false);
    }

    public byte[] encrypt(StreamKeyManager streamKeyManager) throws Exception {
        byte[] valueBytes = SerializationUtils.serialize(values);
        byte[] bytes = ChunkEncryption.encryptAESGcm(streamKeyManager.getChunkEncryptionKey(chunkID), valueBytes);
        return bytes;
    }

    public byte[] encrypt(StreamKeyManager streamKeyManager, CachedKeys cachedKeys) throws Exception {
        byte[] valueBytes = SerializationUtils.serialize(values);
        byte[] bytes = ChunkEncryption.encryptAESGcm(streamKeyManager.getChunkEncryptionKey(chunkID, cachedKeys), valueBytes);
        return bytes;
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
