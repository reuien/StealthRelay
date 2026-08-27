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
    private long count1;
    private long count2;
    private long count3;
    private long count4;
    private long count5;
    private long count6;
    private boolean isEncrypted;
    private boolean hasMac;
    private BigInteger sumMac;
    private BigInteger countMac;
    private BigInteger squareMac;
    private BigInteger count1Mac;
    private BigInteger count2Mac;
    private BigInteger count3Mac;
    private BigInteger count4Mac;
    private BigInteger count5Mac;
    private BigInteger count6Mac;


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

    public Digest(long streamId, long chunkIdFrom, long chunkIdTo, long sum, long count, long square, long count1,
                  long count2, long count3, long count4, long count5, long count6, boolean isEncrypted, boolean hasMac) {
        this.streamId = streamId;
        this.chunkIdFrom = chunkIdFrom;
        this.chunkIdTo = chunkIdTo;
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.count1 = count1;
        this.count2 = count2;
        this.count3 = count3;
        this.count4 = count4;
        this.count5 = count5;
        this.count6 = count6;
        this.isEncrypted = isEncrypted;
        this.hasMac = hasMac;
    }

    public Digest(long streamId, long chunkIdFrom, long chunkIdTo, long sum, long count, long square, long count1,
                  long count2, long count3, long count4, long count5, long count6, boolean isEncrypted, boolean hasMac,
                  BigInteger sumMac, BigInteger countMac, BigInteger squareMac, BigInteger count1Mac, BigInteger count2Mac,
                  BigInteger count3Mac, BigInteger count4Mac, BigInteger count5Mac, BigInteger count6Mac) {
        this.streamId = streamId;
        this.chunkIdFrom = chunkIdFrom;
        this.chunkIdTo = chunkIdTo;
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.count1 = count1;
        this.count2 = count2;
        this.count3 = count3;
        this.count4 = count4;
        this.count5 = count5;
        this.count6 = count6;
        this.isEncrypted = isEncrypted;
        this.hasMac = hasMac;
        this.sumMac = sumMac;
        this.countMac = countMac;
        this.squareMac = squareMac;
        this.count1Mac = count1Mac;
        this.count2Mac = count2Mac;
        this.count3Mac = count3Mac;
        this.count4Mac = count4Mac;
        this.count5Mac = count5Mac;
        this.count6Mac = count6Mac;
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
    public long getCount1() {
        return count1;
    }
    public long getCount2() {
        return count2;
    }
    public long getCount3() {
        return count3;
    }
    public long getCount4() {
        return count4;
    }
    public long getCount5() {
        return count5;
    }
    public long getCount6() {
        return count6;
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
    public void setCount1(long count1) {
        this.count1 = count1;
    }
    public void setCount2(long count2) {
        this.count2 = count2;
    }
    public void setCount3(long count3) {
        this.count3 = count3;
    }
    public void setCount4(long count4) {
        this.count4 = count4;
    }
    public void setCount5(long count5) {
        this.count5 = count5;
    }
    public void setCount6(long count6) {
        this.count6 = count6;
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
    public int getCount1Id() {
        return 3;
    }
    public int getCount2Id() {
        return 4;
    }
    public int getCount3Id() {
        return 5;
    }
    public int getCount4Id() {
        return 6;
    }
    public int getCount5Id() {
        return 7;
    }
    public int getCount6Id() {
        return 8;
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
    public BigInteger getCount1Mac() {
        return count1Mac;
    }
    public BigInteger getCount2Mac() {
        return count2Mac;
    }
    public BigInteger getCount3Mac() {
        return count3Mac;
    }
    public BigInteger getCount4Mac() {
        return count4Mac;
    }
    public BigInteger getCount5Mac() {
        return count5Mac;
    }
    public BigInteger getCount6Mac() {
        return count6Mac;
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
    public void setCount1Mac(BigInteger count1Mac) {
        this.count1Mac = count1Mac;
    }
    public void setCount2Mac(BigInteger count2Mac) {
        this.count2Mac = count2Mac;
    }
    public void setCount3Mac(BigInteger count3Mac) {
        this.count3Mac = count3Mac;
    }
    public void setCount4Mac(BigInteger count4Mac) {
        this.count4Mac = count4Mac;
    }
    public void setCount5Mac(BigInteger count5Mac) {
        this.count5Mac = count5Mac;
    }
    public void setCount6Mac(BigInteger count6Mac) {
        this.count6Mac = count6Mac;
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
            this.setEncrypted(false);
        }else {
            this.setSum(sc.decryptDigestData(this.sum, this.chunkIdFrom, this.chunkIdTo, this.getSumId(), cachedKeys));
            this.setCount(sc.decryptDigestData(this.count, this.chunkIdFrom, this.chunkIdTo, this.getCountId(), cachedKeys));
            this.setSquare(sc.decryptDigestData(this.square, this.chunkIdFrom, this.chunkIdTo, this.getSquareId(), cachedKeys));
            this.setEncrypted(false);
        }
        this.setEncrypted(false);
        System.out.println(this.toString());
        System.out.println("-----------------------");
        return this;
    }

    public Digest decryptNew(StreamKeyManager skm) throws MACCheckFailed {

        StreamCrypto sc =  new StreamCrypto(skm.getKeyDerivationTree(), skm.getMacKeyAsBigInteger());
        System.out.println("-----------------------");
        System.out.println("digest: "+this.chunkIdFrom+" to "+this.chunkIdTo);
        System.out.println(this.toStringNew());
        CachedKeys cachedKeys = new CachedKeys();
        if (this.hasMac){
            this.setSum(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.sum, this.sumMac),
                    this.chunkIdFrom, this.chunkIdTo, this.getSumId(), cachedKeys));
            this.setCount(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count, this.countMac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCountId(), cachedKeys));
            this.setSquare(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.square, this.squareMac),
                    this.chunkIdFrom, this.chunkIdTo, this.getSquareId(), cachedKeys));
            this.setCount1(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count1, this.count1Mac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCount1Id(), cachedKeys));
            this.setCount2(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count2, this.count2Mac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCount2Id(), cachedKeys));
            this.setCount3(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count3, this.count3Mac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCount3Id(), cachedKeys));
            this.setCount4(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count4, this.count4Mac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCount4Id(), cachedKeys));
            this.setCount5(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count5, this.count5Mac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCount5Id(), cachedKeys));
            this.setCount6(sc.decryptDigestDataWithMAC(new StreamCrypto.Ciphertext(this.count6, this.count6Mac),
                    this.chunkIdFrom, this.chunkIdTo, this.getCount6Id(), cachedKeys));
            this.setEncrypted(false);
        }else {
            this.setSum(sc.decryptDigestData(this.sum, this.chunkIdFrom, this.chunkIdTo, this.getSumId(), cachedKeys));
            this.setCount(sc.decryptDigestData(this.count, this.chunkIdFrom, this.chunkIdTo, this.getCountId(), cachedKeys));
            this.setSquare(sc.decryptDigestData(this.square, this.chunkIdFrom, this.chunkIdTo, this.getSquareId(), cachedKeys));
            this.setCount1(sc.decryptDigestData(this.count1, this.chunkIdFrom, this.chunkIdTo, this.getCount1Id(), cachedKeys));
            this.setCount2(sc.decryptDigestData(this.count2, this.chunkIdFrom, this.chunkIdTo, this.getCount2Id(), cachedKeys));
            this.setCount3(sc.decryptDigestData(this.count3, this.chunkIdFrom, this.chunkIdTo, this.getCount3Id(), cachedKeys));
            this.setCount4(sc.decryptDigestData(this.count4, this.chunkIdFrom, this.chunkIdTo, this.getCount4Id(), cachedKeys));
            this.setCount5(sc.decryptDigestData(this.count5, this.chunkIdFrom, this.chunkIdTo, this.getCount5Id(), cachedKeys));
            this.setCount6(sc.decryptDigestData(this.count6, this.chunkIdFrom, this.chunkIdTo, this.getCount6Id(), cachedKeys));
            this.setEncrypted(false);
        }
        this.setEncrypted(false);
        System.out.println(this.toStringNew());
        System.out.println("-----------------------");
        return this;
    }

    public long getStartTime(Stream corStream){
        return TimeUtil.getChunkStartTime(corStream, chunkIdFrom);
    }
    public long getEndTime(Stream corStream){
        return TimeUtil.getChunkEndTime(corStream, chunkIdTo);
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
        if (this.isEncrypted){
            if (this.hasMac){
                return "Digest{" +
                        "streamId=" + streamId +
                        ", chunkIdFrom=" + chunkIdFrom +
                        ", chunkIdTo=" + chunkIdTo +
                        ", sum=" + sum +
                        ", count=" + count +
                        ", square=" + square +
                        ", isEncrypted=" + true +
                        ", hasMac=" + true +
                        ", sumMac=" + sumMac +
                        ", countMac=" + countMac +
                        ", squareMac=" + squareMac +
                        '}';
            }else {
                return "Digest{" +
                        "streamId=" + streamId +
                        ", chunkIdFrom=" + chunkIdFrom +
                        ", chunkIdTo=" + chunkIdTo +
                        ", sum=" + sum +
                        ", count=" + count +
                        ", square=" + square +
                        ", isEncrypted=" + true +
                        ", hasMac=" + false +
                        '}';
            }
        }else {
            return "Digest{" +
                    "streamId=" + streamId +
                    ", chunkIdFrom=" + chunkIdFrom +
                    ", chunkIdTo=" + chunkIdTo +
                    ", sum=" + sum +
                    ", count=" + count +
                    ", square=" + square +
                    ", isEncrypted=" + false +
                    '}';
        }
    }

    public String toStringNew() {
        if (this.isEncrypted){
            if (this.hasMac){
                return "Digest{" +
                        "streamId=" + streamId +
                        ", chunkIdFrom=" + chunkIdFrom +
                        ", chunkIdTo=" + chunkIdTo +
                        ", sum=" + sum +
                        ", count=" + count +
                        ", square=" + square +
                        ", count1=" + count1 +
                        ", count2=" + count2 +
                        ", count3=" + count3 +
                        ", count4=" + count4 +
                        ", count5=" + count5 +
                        ", count6=" + count6 +
                        ", isEncrypted=" + true +
                        ", hasMac=" + true +
                        ", sumMac=" + sumMac +
                        ", countMac=" + countMac +
                        ", squareMac=" + squareMac +
                        ", count1Mac=" + count1Mac +
                        ", count2Mac=" + count2Mac +
                        ", count3Mac=" + count3Mac +
                        ", count4Mac=" + count4Mac +
                        ", count5Mac=" + count5Mac +
                        ", count6Mac=" + count6Mac +
                        '}';
            }else {
                return "Digest{" +
                        "streamId=" + streamId +
                        ", chunkIdFrom=" + chunkIdFrom +
                        ", chunkIdTo=" + chunkIdTo +
                        ", sum=" + sum +
                        ", count=" + count +
                        ", square=" + square +
                        ", count1=" + count1 +
                        ", count2=" + count2 +
                        ", count3=" + count3 +
                        ", count4=" + count4 +
                        ", count5=" + count5 +
                        ", count6=" + count6 +
                        ", isEncrypted=" + true +
                        ", hasMac=" + false +
                        '}';
            }
        }else {
            return "Digest{" +
                    "streamId=" + streamId +
                    ", chunkIdFrom=" + chunkIdFrom +
                    ", chunkIdTo=" + chunkIdTo +
                    ", sum=" + sum +
                    ", count=" + count +
                    ", square=" + square +
                    ", count1=" + count1 +
                    ", count2=" + count2 +
                    ", count3=" + count3 +
                    ", count4=" + count4 +
                    ", count5=" + count5 +
                    ", count6=" + count6 +
                    ", isEncrypted=" + false +
                    '}';
        }
    }

}
