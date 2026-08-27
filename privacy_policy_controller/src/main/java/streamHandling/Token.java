package streamHandling;

import keyDerivation.SeedNode;

import java.util.ArrayList;
import java.util.Date;

public class Token {
    private final long tokenID;
    private final String usrName;
    private final long streamID;
    private final Date streamStartDate;
    private final long chunkSize;
    private final long granularity;
    private final ArrayList<SeedNode> nodes;
    private final int treeDepth;
    private final byte[] macKey;

    public Token(long tokenID, String usrName, long streamID, Date streamStartDate, long chunkSize, long granularity, ArrayList<SeedNode> nodes, int treeDepth, byte[] macKey){
        this.tokenID = tokenID;
        this.usrName = usrName;
        this.streamID = streamID;
        this.streamStartDate = streamStartDate;
        this.chunkSize = chunkSize;
        this.granularity = granularity;
        this.nodes = nodes;
        this.treeDepth = treeDepth;
        this.macKey = macKey;
    }
    public long getTokenID(){ return tokenID; }
    public String getUsrName(){ return usrName; }
    public long getStreamID(){ return streamID; }

    public Date getStreamStartDate() {
        return streamStartDate;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public long getGranularity() {
        return granularity;
    }

    public ArrayList<SeedNode> getNodes(){ return nodes; }
    public int getTreeDepth(){ return treeDepth; }
    public byte[] getMacKey() { return macKey; }
}
