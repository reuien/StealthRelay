package streamHandling;

import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;
import java.util.Objects;


public class EncryptedChunk {
    private final long streamId;
    private final long chunkId;
    private final byte[] payload;

    public EncryptedChunk(long streamId, long chunkId, byte[] payload) {
        this.streamId = streamId;
        this.chunkId = chunkId;
        this.payload = payload;
    }

    public long getStreamId() {
        return streamId;
    }
    public long getChunkId() {
        return chunkId;
    }
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptedChunk that = (EncryptedChunk) o;
        return streamId == that.streamId &&
                chunkId == that.chunkId &&
                Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(streamId, chunkId);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    @Override
    public String toString() {
        return "EncryptedChunk: {" +
                "streamId=" + streamId +
                ", chunkId=" + chunkId +
                ", payload=" + Hex.encodeHexString(payload) +
                '}';
    }

}
