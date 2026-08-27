
package keyDerivation;

import prg.IPRG;
import prg.PRGFactory;

import java.util.ArrayList;

public class KeyDerivationTreeFactory {
    public static KeyDerivationTree getNewDefaultKDTree(byte[] rootSeed, int depth) {
        return getNewKDTree(PRGFactory.getDefaultPRG(), rootSeed, depth, 2);
    }

    public static KeyDerivationTree getNewDefaultKDTree(ArrayList<SeedNode> nodes, int depth) {
        return new KeyDerivationTree(false, PRGFactory.getDefaultPRG(), nodes, depth, 2);
    }

    public static KeyDerivationTree getNewKDTree(IPRG prf, byte[] rootSeed, int depth, int kFactor) {
        ArrayList<SeedNode> seeds = new ArrayList<SeedNode>();
        seeds.add(new SeedNode(rootSeed, 0, 0));
        return new KeyDerivationTree(true, prf, seeds, depth, kFactor);
    }

    public static KeyDerivationTree getNewDefaultTESTKDTree(IPRG prf, int depth) {
        return KeyDerivationTreeFactory.getNewKDTree(prf, new byte[16], depth, 2);
    }

    public static SeedNode getSeedNode(int bitLen, long nodeNr, byte[] seed) {
        return new SeedNode(seed, bitLen, nodeNr);
    }

}
