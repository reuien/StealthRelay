package index;

public class Chunk {

    private long id;

    private byte[] data;

    public Chunk(long id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
