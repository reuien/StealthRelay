
package prg;

public interface IPRG {
    byte[] apply(byte[] prgKey, byte[] input);

    byte[] apply(byte[] prgKey, int input);

    byte[] multiApply(byte[] prgKey, int[] inputs);
}
