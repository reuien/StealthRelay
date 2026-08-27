package index;

import java.math.BigInteger;

public class Digest implements Cloneable{
    public static final BigInteger PRIME = new BigInteger("340282366920938463463374607431768211297");
    //private long chunkIdFrom;
    //private long chunkIdTo;
    private long sum;
    private long count;
    private long square;
    private long count1;
    private long count2;
    private long count3;
    private long count4;
    private long count5;
    private long count6;
    private boolean thisHasMAC;
    private BigInteger sumMac;
    private BigInteger countMac;
    private BigInteger squareMac;
    private BigInteger count1Mac;
    private BigInteger count2Mac;
    private BigInteger count3Mac;
    private BigInteger count4Mac;
    private BigInteger count5Mac;
    private BigInteger count6Mac;

    public Digest(long sum, long count, long square, boolean thisHasMAC) {
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.thisHasMAC = thisHasMAC;
    }
    public Digest(long sum, long count, long square, boolean thisHasMAC, BigInteger sumMac, BigInteger countMac, BigInteger squareMac) {
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.thisHasMAC = thisHasMAC;
        this.sumMac = sumMac;
        this.countMac = countMac;
        this.squareMac = squareMac;
    }

    public Digest(long sum, long count, long square, long count1, long count2, long count3, long count4, long count5,
                  long count6, boolean hasMac) {
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.count1 = count1;
        this.count2 = count2;
        this.count3 = count3;
        this.count4 = count4;
        this.count5 = count5;
        this.count6 = count6;
        this.thisHasMAC = hasMac;
    }

    public Digest(long sum, long count, long square, long count1, long count2, long count3, long count4, long count5,
                  long count6, boolean hasMac, BigInteger sumMac, BigInteger countMac, BigInteger squareMac,
                  BigInteger count1Mac, BigInteger count2Mac, BigInteger count3Mac, BigInteger count4Mac,
                  BigInteger count5Mac, BigInteger count6Mac) {
        this.sum = sum;
        this.count = count;
        this.square = square;
        this.count1 = count1;
        this.count2 = count2;
        this.count3 = count3;
        this.count4 = count4;
        this.count5 = count5;
        this.count6 = count6;
        this.thisHasMAC = hasMac;
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


    /*public void setChunkIdFrom(long chunkIdFrom) {
        this.chunkIdFrom = chunkIdFrom;
    }

    public void setChunkIdTo(long chunkIdTo) {
        this.chunkIdTo = chunkIdTo;
    }

    public long getChunkIdFrom() {
        return chunkIdFrom;
    }

    public long getChunkIdTo() {
        return chunkIdTo;
    }*/

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
    public boolean isThisHasMAC() {
        return thisHasMAC;
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
    public void setHasMAC(boolean thisHasMAC) {
        this.thisHasMAC = thisHasMAC;
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

    public Digest mergeOther(Digest cdo){
        this.sum += cdo.getSum();
        this.count += cdo.getCount();
        this.square += cdo.getSquare();
        if (this.isThisHasMAC() && cdo.isThisHasMAC()){
            this.sumMac = this.sumMac.add(cdo.getSumMac()).mod(PRIME);
            this.countMac = this.countMac.add(cdo.getCountMac()).mod(PRIME);
            this.squareMac = this.squareMac.add(cdo.getSquareMac()).mod(PRIME);
        }
        return this;
    }

    public Digest mergeOtherNewDigest(Digest cdo){
        this.sum += cdo.getSum();
        this.count += cdo.getCount();
        this.square += cdo.getSquare();
        this.count1 += cdo.getCount1();
        this.count2 += cdo.getCount2();
        this.count3 += cdo.getCount3();
        this.count4 += cdo.getCount4();
        this.count5 += cdo.getCount5();
        this.count6 += cdo.getCount6();
        if (this.isThisHasMAC() && cdo.isThisHasMAC()){
            this.sumMac = this.sumMac.add(cdo.getSumMac()).mod(PRIME);
            this.countMac = this.countMac.add(cdo.getCountMac()).mod(PRIME);
            this.squareMac = this.squareMac.add(cdo.getSquareMac()).mod(PRIME);
            this.count1Mac = this.count1Mac.add(cdo.getCount1Mac()).mod(PRIME);
            this.count2Mac = this.count2Mac.add(cdo.getCount2Mac()).mod(PRIME);
            this.count3Mac = this.count3Mac.add(cdo.getCount3Mac()).mod(PRIME);
            this.count4Mac = this.count4Mac.add(cdo.getCount4Mac()).mod(PRIME);
            this.count5Mac = this.count5Mac.add(cdo.getCount5Mac()).mod(PRIME);
            this.count6Mac = this.count6Mac.add(cdo.getCount6Mac()).mod(PRIME);
        }
        return this;
    }

    @Override
    public String toString() {
        if (thisHasMAC){
            return "Digest{" +
                    /*"chunkIdFrom=" + chunkIdFrom +
                    ", chunkIdTo=" + chunkIdTo +*/
                    ", sum=" + sum +
                    ", count=" + count +
                    ", square=" + square +
                    ", thisHasMAC=" + thisHasMAC +
                    ", sumMac=" + sumMac +
                    ", countMac=" + countMac +
                    ", squareMac=" + squareMac +
                    '}';
        }
        return "Digest{" +
                /*"chunkIdFrom=" + chunkIdFrom +
                ", chunkIdTo=" + chunkIdTo +*/
                ", sum=" + sum +
                ", count=" + count +
                ", square=" + square +
                ", thisHasMAC=" + thisHasMAC +
                '}';

    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
