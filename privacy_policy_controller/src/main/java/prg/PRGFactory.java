
package prg;

public class PRGFactory {

    public static IPRG getDefaultPRG() {
        return new PRGAes();
    }

}

