package net.siisise.iso.asn1.parser;

import net.siisise.abnf.ABNF;
import net.siisise.abnf.ABNFReg;
import net.siisise.abnf.parser5234.ABNF5234;

/**
 * 読みにくい 仮版
 */
public class ASN1JISX5603 {
    
    static ABNFReg REG = new ABNFReg();

    static ABNF 識別子 = REG.rule("識別子", ABNF5234.ALPHA.or1(ABNF5234.DIGIT));

}
