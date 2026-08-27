package index.blockindex.node;

public interface NodeContent {

    NodeContent copy();

    void mergeOther(NodeContent otherContent);

    NodeContent mergeOtherCopy(NodeContent otherContent);

    byte[] encode();

    String getStringRepresentation();

}
