package keyDerivation;

import prg.IPRG;
import keyManagement.KeyUtil;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

/**
 * Implements a TreeKeyRegression with Key Regression,
 * keys are the leaf nodes of the tree seed:
 * <p>
 * <pre>
 * RootSeed
 *
 *      - n1
 *          - k0
 *          - k1
 *      - n2
 *          - k2
 *          - k3
 * </pre>
 */
public class KeyDerivationTree {
    public long[] powers;  //数组存储每层节点含有的叶子结点数量，第0个为根节点，是所有叶子节点数量；最后一个为1，即最后一层就是叶子节点。
    public long[] keyInterval;  //密钥间隔，为最后一层（叶子结点）的Nr编号，也即密钥的编号
    Boolean isOwner = false;  //给出的节点列表含根节点（数据拥有者才能有根节点）
    private ArrayList<SeedNode> relevantSeeds;  //节点列表
    private byte[] rootSeed;  //根节点种子
    private int depth;  //树深度，根节点为0
    private int kFactor = 2;  //每个节点的子节点数
    private IPRG prg;  //伪随机函数

    /**
     * Creates a tree key-regression object for an owner.
     *
     * @param prg     the regression function used to derive new seeds
     * @param depth   the depth of the tree (i.e 0 for no tree, 1 for 2 keys, 2 for 4 keys etc.)
     * @param kFactor the nonzero(!) number of children in each node (Default: 2)
     */
    public KeyDerivationTree(Boolean isOwner, IPRG prg, ArrayList<SeedNode> relevantSeeds, int depth,
                             int kFactor) {
        this.isOwner = isOwner;
        this.prg = prg;
        this.relevantSeeds = relevantSeeds;
        this.depth = depth;
        this.kFactor = kFactor;

        if (isOwner) {
            this.rootSeed = relevantSeeds.get(0).getSeed();
        }
        computePowers();
        keyInterval = new long[2];
        keyInterval[0] = getKeyInterval(relevantSeeds.get(0))[0];
        keyInterval[1] = getKeyInterval(relevantSeeds.get(relevantSeeds.size() - 1))[1];
    }

//    /**
//     * Creates a tree key-regression object for a receiver.
//     *
//     * @param prg           the prg used to derive new seeds
//     * @param depth         the depth of the tree(i.e. 0 for no tree, 1 for 2 keys, 2 for 4 keys etc.)
//     * @param kFactor       the nonzero(!) number of childes in each node (Default: 2)
//     * @param relevantSeeds the seedNodes the owner revealed to us, in order to compute our keys.
//     */
//    public KeyDerivationTree(IPRG prg, int depth, int kFactor, int offset, int keyLength,
//                             ArrayList<SeedNode> relevantSeeds, long[] keyInterval) {
//        if (kFactor == 0)
//            throw new RuntimeException("kFactor is not allowed to be zero!");
//        this.prg = prg;
//        this.depth = depth;
//        this.kFactor = kFactor;
//        this.relevantSeeds = relevantSeeds;
//        this.keyInterval = keyInterval;
//        computePowers();
//    }

    private void checkValidAccess(long id) {  // 正常情况下，keyInterval[0] < id < keyInterval[1]
        if (keyInterval[0] > id || id > (keyInterval[1]+1))
            throw new InvalidKeyDerivation("Tree does not support this access");
    }

    private long getNumberOfKeys() {
        //如下面computePowers()所述，powers第0个即为所有叶子节点数，也就是最后生成密钥的总数。
        /* compute amount of keys in whole tree */
        if (depth < 1)  // depth==0，即只有根节点
            return 1L;
        return powers[0];
    }

    private void computePowers() {
        //powers数组 存储每层节点含有的叶子结点数量，第0为根节点，为所有叶子节点数；最后为1，即最后一层就是叶子节点。
        /*Computes an array storing the powers of kFactor*/
        powers = new long[depth + 1];
        long cur = 1;
        for (int i = powers.length - 1; i >= 0; i--) {
            powers[i] = cur;
            cur *= kFactor;
        }
    }

    private SeedNode getRelevantNode(long[] keyInterval, long keyNr) {
        // 在给出的key区间范围内，寻找可以计算特定key的对应节点。可以是密钥直接对应的叶节点，也可以是可生成该密钥的祖先节点。
        // 注意，这里relevantSeeds列表应该是令牌的表示方法并且和key区间严格对应，不能存在可以生成key区间以外key的节点。
        /* Compute the relevant Node from the relevantSeeds which
           is necessary to compute a specific key */
        long keyId = keyNr;
        keyId -= keyInterval[0];
        for (SeedNode htn : relevantSeeds) {
            int currDepth = htn.getDepth();
            long amount = powers[currDepth];
            if (keyId < amount) {
                return htn;
            }
            keyId -= amount;
        }
        return relevantSeeds.get(0);
    }

    private int[] computePath(int depth, long keyId) {
        //计算从叶子向上到特定深度节点的路径，实际上就是在指定深度上，如何到达生成密钥的叶节点
        /*Computing Path from leaf up towards specific node specified by its depth*/
        int[] path = new int[this.depth - depth];  //总深度-要到达的深度，即路径要跳多少步
        long curId = keyId;
        long nextId; // 对应的上层节点的Nr
        for (int d = this.depth; d > depth; d--) {
            nextId = curId / kFactor;
            path[d - depth - 1] = (int) curId % kFactor;  //值为0或1，分别代表左、右
            curId = nextId;
        }
        return path;  // 格式为0 1组成的数组，即从指定深度节点开始，到叶子结点的左/右路径。
    }

    private int[] computePathFromRoot(int depth, long nodeNr) {
        //计算根节点到特定深度节点的路径，输入为节点在所在深度上的编号Nr
        /*Compute Path from rootSeed to a specific node*/
        int[] path = new int[depth];
        long curId = nodeNr;
        long nextId;
        for (int d = depth; d > 0; d--) {
            nextId = curId / kFactor;
            path[d - 1] = (int) curId % kFactor;  //值为0或1，分别代表左、右
            curId = nextId;
        }
        return path;  // 格式为0 1组成的数组，即从根节点开始，到指定结点的左/右路径。
    }

    private long[] getKeyInterval(SeedNode node) {
        //计算输入节点对应的密钥间隔，返回值为最后一层（叶子结点）的Nr编号
        /*returns the interval [from, to] of keys we can compute with a certain SeedNode.*/
        int curDepth = node.getDepth();
        long curId = node.getNodeNr();
        long from = curId * powers[curDepth];
        long to = from + powers[curDepth] - 1;
        long[] interval = {from, to};
        return interval;
    }

    private byte[][] getNodeSeeds(SeedNode node, long from, long to) {
        int[] pathFrom = computePath(node.getDepth(), from);  //给定一个上层节点node，分别计算node到叶子节点from和to的路径
        int[] pathTo = computePath(node.getDepth(), to);

        byte[][] result = new byte[(int) (to - from + 1)][];  //byte[节点数量][]

        ArrayList<byte[]> previousSeeds = new ArrayList<byte[]>();
        ArrayList<byte[]> nextSeeds;
        previousSeeds.add(node.getSeed());

        for (int i = 0; i < this.depth - node.getDepth(); i++) {
            nextSeeds = new ArrayList<byte[]>(previousSeeds.size() * kFactor);

            for (int j = 0; j < previousSeeds.size(); j++) {
                int fromk = 0;
                int tok = kFactor - 1;
                if (j == 0)
                    fromk = pathFrom[i];
                if (j == previousSeeds.size() - 1)
                    tok = pathTo[i];
                for (int k = fromk; k <= tok; k++) {
                    nextSeeds.add(prg.apply(previousSeeds.get(j), k));  // 给定一个节点a，
                }                                                       // 其左孩子节点b的种子，即为apply(a,0)的加密结果；
            }                                                           // 其右孩子节点c的种子，即为apply(a,1)的加密结果
            previousSeeds = nextSeeds;
        }

        for (int i = 0; i < previousSeeds.size(); i++) {
            result[i] = previousSeeds.get(i);
        }
        return result;  // 返回的是从from到to所有叶子结点的种子
    }

    private SeedNode reveal(int depth, long nodeNr) {  // 返回指定深度和Nr的节点
        byte[] cur = rootSeed;
        if (depth == 0) {  // 根节点
            return new SeedNode(cur, 0, 0);
        }
        int[] path = computePathFromRoot(depth, nodeNr);  // 计算根节点到指定节点的路径
        cur = prg.multiApply(rootSeed, path);  // muliApply计算指定节点的seed
        return new SeedNode(cur, depth, nodeNr);  // 返回该节点
    }

    public ArrayList<SeedNode> sortNodeArray(ArrayList<SeedNode> list) {  // 对列表中的节点进行排序，按照包含密钥的先后顺序
        Collections.sort(list, new Comparator<SeedNode>() {
            @Override
            public int compare(SeedNode node1, SeedNode node2) {
                long[] interval1 = getKeyInterval(node1);
                long[] interval2 = getKeyInterval(node2);
                return Long.compare(interval1[0], interval2[0]);
            }
        });
        return list;
    }

    public BigInteger getKey(long id, int keyBits) {
        return KeyUtil.deriveKeyBI(prg, getSeed(id), keyBits);
    }

    /**
     * Returns the key with the given identifier
     *
     * @param id the key identifier
     * @return the corresponding key
     */

    public byte[] getSeed(long id) throws InvalidKeyDerivation {
        checkValidAccess(id);  // 确保id在允许访问的keyId范围内
        SeedNode seedNode = getRelevantNode(keyInterval, id);  // 获取相关节点列表中，可以生成该id密钥的节点。
        int curDepth = seedNode.getDepth();  // 节点深度        // 可能是密钥直接对应的叶节点，也可能是可生成该密钥的祖先节点。
        byte[] curSeed = seedNode.getSeed();  // 节点种子

        if (curDepth != depth) {  // 说明获得的节点不是叶子结点
            /*if seedNode is not already a leaf.*/
            int[] path = computePath(curDepth, id);  // 计算当前节点到叶节点的路径
            curSeed = prg.multiApply(curSeed, path);  // 根据当前种子和路径计算叶子结点的种子
        }
        /*Derive Key from leaf seed value*/
        return curSeed;
    }

    public IPRG getPRG() {
        return this.prg;
    }

//    /**
//     * Returns the keys in the given range (Inclusive)
//     *
//     * @param fromValue the first key identifier in the range
//     * @param toValue   the last key identifier in the range
//     * @return the key range
//     */

//    public byte[][] getSeeds(long fromValue, long toValue) throws InvalidKeyDerivation {
//        checkValidAccess(fromValue);
//        checkValidAccess(toValue);
//        long from = fromValue;
//        long to = toValue;
//        long curId = 0; /* describes how many keys we already have */
//        byte[][] result = new byte[(int) (to - from + 1)][];
//
//        for (SeedNode node : relevantSeeds) {
//            long[] nodeKeyInterval = getKeyInterval(node);
//            long curFrom = nodeKeyInterval[0];
//            long curTo = nodeKeyInterval[1];
//
//            if (from <= curTo && to >= curFrom) {
//                /* node is relevant for at least one key */
//                curFrom = max(from, curFrom);
//                curTo = min(to, curTo);
//                long amountOfKeys = curTo - curFrom + 1; /*nr of  desired keys we get from current node*/
//                byte[][] keys = getNodeSeeds(node, curFrom, curTo);
//
//                for (long i = curId; i < curId + amountOfKeys; i++) {
//                    result[(int) i] = keys[(int) (i - curId)];
//                }
//                curId += amountOfKeys;
//            }
//        }
//        return result;
//    }

//    /**
//     * Reveal the relevant SeedNodes to allow computation
//     * of the keys from Interval [from, to].
//     *
//     * @param from specifies start of Interval of keys we would like to reveal.
//     * @param to   specifies end of Interval.
//     * @return ArrayList of HashTreeNodes.
//     */
//    public ArrayList<SeedNode> revealSeeds(long from, long to) {
//        assert isOwner;
//        ArrayList<SeedNode> seedNodes = new ArrayList<>();
//        if (from > to || rootSeed == null)
//            throw new RuntimeException(String.format("%d is not smaller than %d", from, to));
//        for (int d = depth; d >= 0; d--) {
//            for (int i = 0; i < kFactor - 1; i++) {
//                if (from == to) {
//                    SeedNode node = reveal(d, from);
//                    if (!seedNodes.contains(node))
//                        seedNodes.add(node);
//                }
//                if (from % kFactor != 0) {
//                    SeedNode node = reveal(d, from);
//                    if (!seedNodes.contains(node))
//                        seedNodes.add(node);
//                    from++;
//                }
//                if (to % kFactor != kFactor - 1) {
//                    SeedNode node = reveal(d, to);
//                    if (!seedNodes.contains(node))
//                        seedNodes.add(node);
//                    to--;
//                }
//                if (from > to) {
//                    break;
//                }
//            }
//            if (from > to) {
//                break;
//            }
//            from /= kFactor;
//            to /= kFactor;
//        }
//        sortNodeArray(seedNodes);
//        return seedNodes;
//    }
    public int getDepth(){
        return depth;
    }
}