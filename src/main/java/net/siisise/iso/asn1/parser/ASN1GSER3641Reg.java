package net.siisise.iso.asn1.parser;

import net.siisise.abnf.ABNF;
import net.siisise.abnf.ABNFReg;
import net.siisise.abnf.rfc.LDAP2252;

/**
 * RFC 3641 GSER とりあえず版
 * RFC 3642
 */
public class ASN1GSER3641Reg {

    static final ABNFReg REG = new ABNFReg();

    static final ABNF dquote = REG.rule("dquote", ABNF.bin(0x22));
    // バイトベースを文字ペースに改変
    static final ABNF SafeUTF8Character = REG.rule("SafeUTF8Character", ABNF.range(0x00, 0x21).or1(ABNF.range(0x23, 0x10ffff), dquote.pl(dquote)));

    // 3.2.
    static final ABNF StringValue = REG.rule("StringValue", dquote.pl(SafeUTF8Character, dquote));
    static final ABNF GeneralizedTimeValue = REG.rule("GeneralizedTimeValue", StringValue);
    static final ABNF UTCTimeValue = REG.rule("UTCTimeValue", StringValue);
    static final ABNF ObjectDescriptorValue = REG.rule("ObjectDescriptorValue", StringValue);
    // 3.4.
    static final ABNF uppercase = REG.rule("uppercase", ABNF.range(0x41, 0x5a));
    static final ABNF lowercase = REG.rule("lowercase", ABNF.range(0x61, 0x7a));
    static final ABNF decimalDigit = REG.rule("decimal-digit", ABNF.range(0x30, 0x39));
    static final ABNF hyphen = REG.rule("hyphen", ABNF.bin('-'));
    static final ABNF alphanumeric = REG.rule("alphanumeric", uppercase.or1(lowercase, decimalDigit));
    static final ABNF identifier = REG.rule("identifier", lowercase.pl(alphanumeric.x(), hyphen.pl(alphanumeric.ix()).x()));
    // 3.5. BIT STRING
    static final ABNF sp = REG.rule("sp", ABNF.bin(0x20).x());
    static final ABNF squote = REG.rule("squote", ABNF.bin(0x27));
    static final ABNF binaryDigit = REG.rule("binary-digit", ABNF.binlist("01"));
    static final ABNF bstring = REG.rule("bstring", squote.pl(binaryDigit.x(), squote, ABNF.bin(0x42)));
    static final ABNF hexadecimalDigit = REG.rule("hexadecimal-digit", ABNF.range(0x30, 0x39).or1(ABNF.range(0x41, 0x46)));
    static final ABNF hstring = REG.rule("hstring", squote.pl(hexadecimalDigit.x(), squote, ABNF.bin(0x48)));
    static final ABNF bitList = REG.rule("bit-list", ABNF.bin('{').pl(sp.pl(identifier, ABNF.bin(',').pl(sp, identifier).x()).c(), sp, ABNF.bin('}')));
    static final ABNF BitStringValue = REG.rule("BitStringValue", bstring.or1(hstring, bitList));
    // 3.6. BOOLEAN
    static final ABNF BooleanValue = REG.rule("BooleanValue", ABNF.bin("TRUE").or1(ABNF.bin("FALSE")));
    // 3.7. ENUMERATED
    static final ABNF EnumeratedValue = REG.rule("EnumeratedValue", identifier);
    // 3.8. INTEGER
    static final ABNF nonZeroDigit = REG.rule("non-zero-digit", ABNF.range(0x31, 0x39));
    static final ABNF positiveNumber = REG.rule("positive-number", nonZeroDigit.pl(decimalDigit.x()));
    static final ABNF IntegerValue = REG.rule("IntegerValue", ABNF.bin('0').or1(positiveNumber, ABNF.bin('-').pl(positiveNumber), identifier));
    // 3.9. NULL
    static final ABNF NullValue = REG.rule("NullValue", ABNF.bin("NULL"));
    // 3.10. OBJECT IDENTIFIER and RELATIVE-OID
    static final ABNF oidComponent = REG.rule("oid-component", ABNF.bin('0').or1(positiveNumber));
    static final ABNF numericOid = REG.rule("numeric-oid", oidComponent.pl(ABNF.bin('.').pl(oidComponent).ix()));
    static final ABNF descr = REG.rule("descr", LDAP2252.descr);
    static final ABNF ObjectIdentifierValue = REG.rule("ObjectIdentifierValue", numericOid.or1(descr));
    static final ABNF RelativeOIDValue = REG.rule("RelativeOIDValue", oidComponent.pl(ABNF.bin(".").pl(oidComponent).x()));
    // 3.11. OCTET STRING
    static final ABNF OctetStringValue = REG.rule("OctetStringValue", hstring);
    // 3.12. CHOICE
    static final ABNF IdentifiedChoiceValue = REG.rule("IdentifiedChoiceValue", identifier.pl(ABNF.bin(":"), REG.ref("Value")));
    static final ABNF ChoiceOfStringValue = REG.rule("ChoiceOfStringValue", StringValue);
    static final ABNF ChoiceValue = REG.rule("ChoiceValue", IdentifiedChoiceValue.or1(ChoiceOfStringValue));
    // 3.13. SEQUENCE and SET
    static final ABNF msp = REG.rule("msp", ABNF.bin(0x20).ix());
    static final ABNF NamedValue = REG.rule("NamedValue", identifier.pl(msp, REG.ref("Value")));
    static final ABNF ComponentList = REG.rule("ComponentList", ABNF.bin('{').pl(sp.pl(NamedValue, ABNF.bin(',').pl(sp, NamedValue).x()).c(), sp, ABNF.bin('}')));
    static final ABNF SequenceValue = REG.rule("SequenceValue", ComponentList);
    static final ABNF SetValue = REG.rule("SetValue", ComponentList);
    // 3.14. SEQUENCE OF and SET OF
    static final ABNF SequenceOfValue = REG.rule("SequenceOfValue", ABNF.bin('{').pl(sp.pl(REG.ref("Value"), ABNF.bin('.').pl(sp, REG.ref("Value")).x()).c(), sp, ABNF.bin('}')));
    static final ABNF SetOfValue = REG.rule("SetOfValue", ABNF.bin('{').pl(sp.pl(REG.ref("Value"), ABNF.bin('.').pl(sp, REG.ref("Value")).x()).c(), sp, ABNF.bin('}')));
    // 3.15. CHARACTER STRING
    static final ABNF CharacterStringValue = REG.rule("CharacterStringValue", SequenceValue);
    // 3.16. EMBEDDED PDV
    static final ABNF EmbeddedPDVValue = REG.rule("EmbeddedPDVValue", SequenceValue);
    // 3.17. EXTERNAL
    static final ABNF ExternalValue = REG.rule("ExternalValue", SequenceValue);
    // 3.18 INSTANCE OF
    static final ABNF InstanceOfValue = REG.rule("InstanceOfValue", SequenceValue);
    // 3.19 REAL
    static final ABNF mantissa = REG.rule("mantissa", positiveNumber.pl(ABNF.bin('.').pl(decimalDigit.x()).c()).or1(ABNF.bin("0.").pl(ABNF.bin('0').x(), positiveNumber)));
    static final ABNF exponent = REG.rule("exponent", ABNF.text("E").pl(ABNF.bin('0').or1(ABNF.bin('-').c().pl(positiveNumber))));
    static final ABNF PlusInfinity = REG.rule("PLUS-INFINITY", ABNF.bin("PLUS-INFINITY"));
    static final ABNF MinusInfinity = REG.rule("MINUS-INFINITY", ABNF.bin("MINUS-INFINITY"));
    static final ABNF realnumber = REG.rule("realnumber", mantissa.pl(exponent));
    static final ABNF RealValue = REG.rule("RealValue", ABNF.bin('0').or1(PlusInfinity, MinusInfinity, realnumber, ABNF.bin('-').pl(realnumber), SequenceValue));
    // 3.20. Variant Encodings
    static final ABNF RDNSequenceValue = REG.rule("RDNSequenceValue", StringValue);
    static final ABNF RelativeDistinguishedNameValue = REG.rule("RelativeDistinguishedNameValue", StringValue);
    static final ABNF ORAddressValue = REG.rule("ORAddressValue", StringValue);
    static final ABNF VariantEncoding = REG.rule("VariantEncoding", RDNSequenceValue.or1(RelativeDistinguishedNameValue, ORAddressValue));

    // 3. Generic String Encoding Rules
    public static final ABNF Value = REG.rule("Value", BitStringValue.or1(BooleanValue, CharacterStringValue, ChoiceValue,
            EmbeddedPDVValue, EnumeratedValue, ExternalValue,
            GeneralizedTimeValue, IntegerValue, InstanceOfValue, NullValue,
            ObjectDescriptorValue, ObjectIdentifierValue, OctetStringValue,
            RealValue, RelativeOIDValue, SequenceOfValue, SequenceValue,
            SetOfValue, SetValue, StringValue, UTCTimeValue, VariantEncoding));

}
