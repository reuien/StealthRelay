package index;

import java.math.BigInteger;

public class DigestShow implements Comparable<DigestShow> {
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


    public DigestShow(long chunkIdFrom, long chunkIdTo, long sum, long count, long square, boolean isEncrypted, boolean hasMac) {
        this.chunkIdFrom = chunkIdFrom;
        this.chunkIdTo = chunkIdTo;
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.isEncrypted = isEncrypted;
        this.hasMac = hasMac;
    }
    public DigestShow(long chunkIdFrom, long chunkIdTo, long sum, long count, long square, boolean isEncrypted,
                      boolean hasMac, BigInteger sumMac, BigInteger countMac, BigInteger squareMac) {
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
    public DigestShow mergeDigest(DigestShow digestShow1, DigestShow digestShow2){

        long newFrom = -1;
        long newTo = -1;
        if (digestShow2.getChunkIdFrom() == digestShow1.getChunkIdTo()+1){
            newFrom = digestShow1.getChunkIdFrom();
            newTo = digestShow2.getChunkIdTo();
        } else if (digestShow1.getChunkIdFrom() == digestShow2.getChunkIdTo()+1) {
            newFrom = digestShow2.getChunkIdFrom();
            newTo = digestShow1.getChunkIdTo();
        }else {
            throw new RuntimeException("非连续的摘要不允许聚合! ");
        }
        if (digestShow1.isHasMac() && digestShow2.isHasMac()){
            return new DigestShow(newFrom, newTo,
                    digestShow1.getSum()+ digestShow2.getSum(),
                    digestShow1.getCount()+ digestShow2.getCount(),
                    digestShow1.getSquare()+ digestShow2.getSquare(),
                    digestShow1.isEncrypted(),
                    digestShow1.isHasMac(),
                    digestShow1.getSumMac().add(digestShow2.getSumMac()),
                    digestShow1.getCountMac().add(digestShow2.getCountMac()),
                    digestShow1.getSquareMac().add(digestShow2.getSquareMac()));
        }
        return new DigestShow(newFrom, newTo,
                digestShow1.getSum()+ digestShow2.getSum(),
                digestShow1.getCount()+ digestShow2.getCount(),
                digestShow1.getSquare()+ digestShow2.getSquare(),
                digestShow1.isEncrypted(), false);
        }

    @Override
    public int compareTo(DigestShow digestShow) {
        long idDelta = this.getChunkIdFrom() - digestShow.getChunkIdFrom();

        if (idDelta != 0) {
            return getComparableDiffFromLong(idDelta);
        }

        idDelta = this.getChunkIdTo() - digestShow.getChunkIdTo();

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
        return "DigestShow{" +
                "chunkIdFrom=" + chunkIdFrom +
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
