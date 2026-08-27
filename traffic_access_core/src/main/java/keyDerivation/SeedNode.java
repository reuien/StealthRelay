
package keyDerivation;

import java.io.Serializable;

import static java.lang.StrictMath.max;

/**
 * Each Node is a Triple (seed, depth, nodeNr), where seed is the seed value at
 * a specific depth and nodeNr in a TreeKeyRegression. NodeNr starts from 0 at each
 * depth and increases from left to right on the same layer of the Tree.
 */

public class SeedNode implements Serializable {

    private byte[] seed;  //当前节点的特定种子
    private int depth;  //深度（根为0）
    private long nodeNr;  //节点在所在层的编号，从0开始，每层独立

    /**
     * Creates a TreeKeyRegressionNode object
     *
     * @param seed   the seed value at this node
     * @param depth  the depth of the node in the tree
     * @param nodeNr nr of this node from left to right at same depth
     */
    public SeedNode(byte[] seed, int depth, long nodeNr) {
        this.seed = seed;
        this.depth = depth;
        this.nodeNr = nodeNr;
    }

    public byte[] getSeed() {
        return seed;
    }

    public int getDepth() {
        return depth;
    }

    public long getNodeNr() {
        return nodeNr;
    }

    public void printNode() {
        System.out.println("[Seed: " + seed.toString() + ", Depth: " + depth + ", NodeNr: " + nodeNr + "]");
    }

    public int compareTo(SeedNode node) {
        if (this.depth == node.getDepth() && this.nodeNr == node.getNodeNr())
            return 0;
        long factor1 = this.getNodeNr() / max(1, this.getDepth());
        long factor2 = node.getNodeNr() / max(1, node.getDepth());
        if (factor1 < factor2)
            return -1;
        else
            return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SeedNode))
            return false;
        SeedNode node = (SeedNode) obj;
        return this.depth == node.getDepth() && this.nodeNr == node.getNodeNr();
    }
}
