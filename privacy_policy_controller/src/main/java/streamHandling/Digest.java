package streamHandling;

import crypto.MACCheckFailed;
import crypto.StreamCrypto;
import keyManagement.CachedKeys;
import keyManagement.StreamKeyManager;

import java.math.BigInteger;


public class Digest implements Comparable<Digest> {
    private final long streamId;
    private final long chunkIdFrom;
    private final long chunkIdTo;
    private long sum;
    private long count;
    private long square;
    private boolean isEncrypted;
    private boolean hasMac;
    private BigInteger sumMac;
    private BigInteger countMac;
    private BigInteger squareMac;


    public Digest(long streamId, long chunkIdFrom, long chunkIdTo, long sum, long count, long square, boolean isEncrypted, boolean hasMac) {
        this.streamId = streamId;
        this.chunkIdFrom = chunkIdFrom;
        this.chunkIdTo = chunkIdTo;
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.isEncrypted = isEncrypted;
        this.hasMac = hasMac;
    }
    public Digest(long streamId, long chunkIdFrom, long chunkIdTo, long sum, long count, long square, boolean isEncrypted,
                  boolean hasMac, BigInteger sumMac, BigInteger countMac, BigInteger squareMac) {
        this.streamId = streamId;
        this.chunkIdFrom = chunkIdFrom;
        this.chunkIdTo = chunkIdTo;
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.isEncrypted = isEncrypted;
        this.hasMac = hasMac;
        this.sumMac = sumMac;
        this.countMac = countMac;
        this.squareMac = squareMac;
    }


    public long getStreamId() {
        return streamId;
    }

    public long getChunkIdFrom() {
        return chunkIdFrom;
    }

    public long getChunkIdTo() {
        return chunkIdTo;
    }

    public long getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }
    public long getSquare() {
        return square;
    }
    public boolean isEncrypted(){
        return isEncrypted;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public void setCount(long count) {
        this.count = count;
    }
    public void setSquare(long square) {
        this.square = square;
    }
    public void setEncrypted(boolean encrypted) {
        this.isEncrypted = encrypted;
    }
    public int getSumId() {
        return 0;
    }
    public int getCountId() {
        return 1;
    }
    public int getSquareId() {
        return 2;
    }
    public boolean isHasMac() {
        return hasMac;
    }
    public BigInteger getSumMac() {
        return sumMac;
    }
    public BigInteger getCountMac() {
        return countMac;
    }
    public BigInteger getSquareMac() {
        return squareMac;
    }
    public void setHasMac(boolean hasMac) {
        this.hasMac = hasMac;
    }
    public void setSumMac(BigInteger sumMac) {
        this.sumMac = sumMac;
    }
    public void setCountMac(BigInteger countMac) {
        this.countMac = countMac;
    }
    public void setSquareMac(BigInteger squareMac) {
        this.squareMac = squareMac;
    }
    public Digest mergeDigest(Digest digest1, Digest digest2){
        if (digest1.getStreamId() != digest2.getStreamId()){
            throw new RuntimeException("不同流的摘要不允许聚合! ");
        }
        if (digest1.isEncrypted() != digest2.isEncrypted()){
            throw new RuntimeException("未加密的摘要和加密后的摘要不允许聚合! ");
        }
        long newFrom = -1;
        long newTo = -1;
        if (digest2.getChunkIdFrom() == digest1.getChunkIdTo()+1){
            newFrom = digest1.getChunkIdFrom();
            newTo = digest2.getChunkIdTo();
        } else if (digest1.getChunkIdFrom() == digest2.getChunkIdTo()+1) {
            newFrom = digest2.getChunkIdFrom();
            newTo = digest1.getChunkIdTo();
        }else {
            throw new RuntimeException("非连续的摘要不允许聚合! ");
        }
        if (digest1.isHasMac() && digest2.isHasMac()){
            return new Digest(digest1.getStreamId(), newFrom, newTo,
                    digest1.getSum()+digest2.getSum(),
                    digest1.getCount()+digest2.getCount(),
                    digest1.getSquare()+digest2.getSquare(),
                    digest1.isEncrypted(),
                    digest1.isHasMac(),
                    digest1.getSumMac().add(digest2.getSumMac()),
                    digest1.getCountMac().add(digest2.getCountMac()),
                    digest1.getSquareMac().add(digest2.getSquareMac()));
        }
        return new Digest(digest1.getStreamId(), newFrom, newTo,
                digest1.getSum()+digest2.getSum(),
                digest1.getCount()+digest2.getCount(),
                digest1.getSquare()+digest2.getSquare(),
                digest1.isEncrypted(), false);
    }
    public Digest decrypt(StreamKeyManager skm) throws MACCheckFailed {

        StreamCrypto sc =  new StreamCrypto(skm.getKeyDerivationTree(), skm.getMacKeyAsBigInteger());
        System.out.println("-----------------------");
        System.out.println("digest: "+this.chunkIdFrom+" to "+this.chunkIdTo);
        System.out.println(this.toString());
        CachedKeys cachedKeys = new CachedKeys();
        if (this.hasMac){
            this.setSum(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.sum, this.sumMac),
                    this.chunkIdFrom, this.chunkIdTo, this.getSumId(), cachedKeys));
            this.setCount(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count, this.countMac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCountId(), cachedKeys));
            this.setSquare(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.square, this.squareMac),
                    this.chunkIdFrom, this.chunkIdTo, this.getSquareId(), cachedKeys));
        }else {
            this.setSum(sc.decryptDigestData(this.sum, this.chunkIdFrom, this.chunkIdTo, this.getSumId(), cachedKeys));
            this.setCount(sc.decryptDigestData(this.count, this.chunkIdFrom, this.chunkIdTo, this.getCountId(), cachedKeys));
            this.setSquare(sc.decryptDigestData(this.square, this.chunkIdFrom, this.chunkIdTo, this.getSquareId(), cachedKeys));
        }
        this.setEncrypted(false);
        System.out.println(this.toString());
        System.out.println("-----------------------");
        return this;
    }

    @Override
    public int compareTo(Digest digest) {
        if (this.streamId != digest.streamId) {
            return getComparableDiffFromLong(this.streamId -
                    digest.streamId);
        }
        long idDelta = this.getChunkIdFrom() - digest.getChunkIdFrom();

        if (idDelta != 0) {
            return getComparableDiffFromLong(idDelta);
        }

        idDelta = this.getChunkIdTo() - digest.getChunkIdTo();

        if (idDelta != 0) {
            return getComparableDiffFromLong(idDelta);
        }

        return 0;
    }

    private int getComparableDiffFromLong(long delta) {
        if (delta > Integer.MAX_VALUE) {
            return +1;
        } else if (delta < Integer.MIN_VALUE) {
            return -1;
        } else {
            return (int) delta;
        }
    }

    @Override
    public String toString() {
        return "Digest{" +
                "streamId=" + streamId +
                ", chunkIdFrom=" + chunkIdFrom +
                ", chunkIdTo=" + chunkIdTo +
                ", sum=" + sum +
                ", count=" + count +
                ", square=" + square +
                ", isEncrypted=" + isEncrypted +
                ", hasMac=" + hasMac +
                ", sumMac=" + sumMac +
                ", countMac=" + countMac +
                ", squareMac=" + squareMac +
                '}';
    }
}
