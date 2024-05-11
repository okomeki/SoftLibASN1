/*
 * Copyright 2024 okome.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.siisise.iso.asn1.parser;

import net.siisise.abnf.ABNF;
import net.siisise.abnf.ABNFReg;
import net.siisise.abnf.rfc.LDAP2252;

/**
 * RFC 3642 読める形の GSER ABNF.
 */
 public class ASN1GSER3642Reg {

    static final ABNFReg REG = new ABNFReg();

    // 3. Separators
    static final ABNF sp = REG.rule("sp", ABNF.bin(0x20).x());
    static final ABNF msp = REG.rule("msp", ABNF.bin(0x20).ix());
    static final ABNF sep = REG.rule("sep",ABNF.bin(',').c());
    // 4. ASN.1 Built-in Types
    // BIT-STRING
    static final ABNF squote = REG.rule("squote", ABNF.bin(0x27));
    static final ABNF binaryDigit = REG.rule("binary-digit", ABNF.binlist("01"));
    static final ABNF bstring = REG.rule("bstring", squote.pl(binaryDigit.x(), squote, ABNF.bin(0x42)));
    static final ABNF hexadecimalDigit = REG.rule("hexadecimal-digit", ABNF.range(0x30, 0x39).or1(ABNF.range(0x41, 0x46)));
    static final ABNF hstring = REG.rule("hstring", squote.pl(hexadecimalDigit.x(), squote, ABNF.bin(0x48)));
    static final ABNF BIT_STRING = REG.rule("BIT-STRING", bstring.or(hstring));

    // BOOLEAN
    static final ABNF BOOLEAN = REG.rule("BOOLEAN",ABNF.bin("TRUE").or1(ABNF.bin("FALSE")));

    // INTEGER
    static final ABNF nonZeroDigit = REG.rule("non-zero-digit", ABNF.range(0x31,0x39));
    static final ABNF decimalDigit = REG.rule("decimal-digit", ABNF.range(0x30, 0x39));
    static final ABNF positiveNumber = REG.rule("positive-number", nonZeroDigit.pl(decimalDigit.x()));
    static final ABNF INTEGER0MAX = REG.rule("INTEGER-0-MAX", ABNF.bin('0').or(positiveNumber));
    static final ABNF INTEGER1MAX = REG.rule("INTEGER-1-MAX", positiveNumber);
    static final ABNF INTEGER = REG.rule("INTEGER", ABNF.bin('0').or(positiveNumber, ABNF.bin('-').pl(positiveNumber)));
    
    static final ABNF NULL = REG.rule("NULL", ABNF.bin("NULL"));

    static final ABNF oidComponent = REG.rule("oid-component", ABNF.bin('0').or(positiveNumber));
    static final ABNF numericOid = REG.rule("numeric-oid", oidComponent.pl(ABNF.bin('.').pl(oidComponent)));
    static final ABNF OBJECT_IDENTIFIER = REG.rule("OBJECT-IDENTIFIER", numericOid.or(LDAP2252.descr));

    static final ABNF OCTET_STRING = REG.rule("OCTET-STRING", hstring);

    // REAL
    static final ABNF PLUS_INFINITY = REG.rule("PLUS-INFINITY", ABNF.bin("PLUS-INFINITY"));
    static final ABNF MINUS_INFINITY = REG.rule("MINUS-INFINITY", ABNF.bin("MINUS-INFINITY"));
    static final ABNF mantissa = REG.rule("mantissa", positiveNumber.pl(ABNF.bin('.').pl(decimalDigit.x()).c()).or1(ABNF.bin("0.").pl(ABNF.bin('0').x(), positiveNumber)));
    static final ABNF exponent = REG.rule("exponent", ABNF.bin('E').pl(ABNF.bin('0').or(ABNF.bin('-').c().pl(positiveNumber))));
    static final ABNF realnumber = REG.rule("realnumber", mantissa.pl(exponent));
    static final ABNF idMantissa = REG.rule("id-mantissa", ABNF.bin("mantissa"));
    static final ABNF idBase = REG.rule("id-base", ABNF.bin("base"));
    static final ABNF idExponent = REG.rule("id-exponent", ABNF.bin("exponent"));
    static final ABNF realSequenceValue = REG.rule("real-sequence-value", ABNF.bin('{').pl(sp, idMantissa, msp, INTEGER, ABNF.bin(','),
            sp, idBase, msp, ABNF.bin('2').or(ABNF.bin("10")), ABNF.bin(','),
            sp, idExponent, msp, INTEGER, sp, ABNF.bin('}')));
    static final ABNF REAL = REG.rule("REAL", ABNF.bin('0').or1(PLUS_INFINITY, MINUS_INFINITY,
            realnumber, ABNF.bin('-').pl(realnumber), realSequenceValue));

    // CHARACTER-STRING
    static final ABNF idIdentification = REG.rule("id-identification", ABNF.bin("identification"));
    static final ABNF idDataValue = REG.rule("id-data-value", ABNF.bin("data-value"));
    static final ABNF idSyntaxes = REG.rule("id-syntaxes", ABNF.bin("syntaxes"));
    static final ABNF idSyntax = REG.rule("id-syntax", ABNF.bin("syntax"));
    static final ABNF idPresentationContextId = REG.rule("id-presentation-context-id", ABNF.bin("presentation-context-id"));
    static final ABNF idContextNegotiation = REG.rule("id-context-negotiation", ABNF.bin("context-negotiation"));
    static final ABNF idTransferSyntax = REG.rule("id-transfer-syntax", ABNF.bin("transfer-syntax"));
    static final ABNF idFixed = REG.rule("id-fixed", ABNF.bin("fixed"));
    static final ABNF idAbstract = REG.rule("id-abstract", ABNF.bin("abstract"));
    static final ABNF idTransfer = REG.rule("id-transfer", ABNF.bin("transfer"));

    static final ABNF ContextNegotiation = REG.rule("ContextNegotiation", ABNF.bin('{').pl(sp, idPresentationContextId, msp,
            INTEGER, ABNF.bin(','), sp, idTransferSyntax, msp, OBJECT_IDENTIFIER, sp, ABNF.bin('}')));

    static final ABNF Syntaxes = REG.rule("Syntaxes", ABNF.bin('{').pl(sp, idAbstract, msp, OBJECT_IDENTIFIER, ABNF.bin(','),
            sp, idTransfer, msp, OBJECT_IDENTIFIER, sp, ABNF.bin('}')));

    static final ABNF Identification = REG.rule("Identification", idSyntaxes.pl(ABNF.bin(':'), Syntaxes)
            .or(idSyntax.pl(ABNF.bin(':'), OBJECT_IDENTIFIER),
                    idPresentationContextId.pl(ABNF.bin(':'),INTEGER),
                    idContextNegotiation.pl(ABNF.bin(':'),ContextNegotiation),
                    idTransferSyntax.pl( ABNF.bin(':'), OBJECT_IDENTIFIER),
                    idFixed.pl( ABNF.bin(':'), NULL)) );

    static final ABNF CHARACTER_STRING = REG.rule("CHARACTER-STRING", ABNF.bin('{').pl(sp, idIdentification, msp, Identification,
            ABNF.bin(','), sp, idDataValue, msp, OCTET_STRING, sp, ABNF.bin('}')));

    static final ABNF EMBEDDED_PDV = REG.rule("EMBEDDED-PDV", ABNF.bin('{').pl(sp,idIdentification,msp,Identification,ABNF.bin(','),sp,idDataValue,msp,OCTET_STRING,sp,ABNF.bin('}')));

    static final ABNF RELATIVE_OID = REG.rule("RELATIVE-OID", oidComponent.pl(ABNF.bin('.').pl(oidComponent).x()));

    // 5.
    static final ABNF dquote = REG.rule("dquote", ABNF.bin(0x22));
    static final ABNF SafeUTF8Character = REG.rule("SafeUTF8Character", ABNF.binRange(0x00,0x21).or1(ABNF.binRange(0x23,0x7f),
            dquote.pl(dquote),
            ABNF.binRange(0xc0, 0xdf).pl(ABNF.binRange(0x80, 0xbf)),
            ABNF.binRange(0xe0, 0xef).pl(ABNF.binRange(0x80, 0xbf).x(2)),
            ABNF.binRange(0xf0, 0xf7).pl(ABNF.binRange(0x80, 0xbf).x(3))));
    static final ABNF space = REG.rule("space", ABNF.bin(0x20));
    static final ABNF NumericString = REG.rule("NumericString", dquote.pl(decimalDigit.or(space).x(), dquote));
    static final ABNF PrintableCharacter = REG.rule("PrintableCharacter", decimalDigit.or1(space,
            ABNF.range(0x41, 0x5a), ABNF.range(0x61, 0x7a), ABNF.range(0x27, 0x29), ABNF.range(0x2b, 0x2f),
            ABNF.bin(0x3a), ABNF.bin(0x3d), ABNF.bin(0x3f)));
    static final ABNF PrintableString = REG.rule("PrintableString", dquote.pl(PrintableCharacter, dquote));

    static final ABNF SafeVisibleCharacter = REG.rule("SafeVisibleCharacter", ABNF.range(0x20, 0x21).or1(ABNF.range(0x23, 0x7e),dquote.pl(dquote)));
    static final ABNF VisibleString = REG.rule("VisibleString", dquote.pl(SafeVisibleCharacter.x(), dquote));
    static final ABNF ISO646String = REG.rule("ISO646String", VisibleString);

    static final ABNF SafeIA5Character = REG.rule("SafeIA5Character", ABNF.range(0x00, 0x21).or1(ABNF.range(0x23, 0x7f), dquote.pl(dquote)));
    static final ABNF IA5String = REG.rule("IA5String", dquote.pl(SafeIA5Character.x(),dquote));

    static final ABNF StringValue = REG.rule("StringValue", dquote.pl(SafeUTF8Character.x(), dquote));
    static final ABNF BMPString = REG.rule("BMPString", StringValue);
    static final ABNF UniversalString = REG.rule("UniversalString", StringValue);

    static final ABNF TeletexString = REG.rule("TeletexString", StringValue);
    static final ABNF T61String = REG.rule("T61String", StringValue);
    static final ABNF VideotexString = REG.rule("VideotexString", StringValue);
    static final ABNF GraphicString = REG.rule("GraphicString", StringValue);
    static final ABNF GeneralString = REG.rule("GeneralString", StringValue);
    static final ABNF ObjectDescriptor = REG.rule("ObjectDescriptor", GraphicString);

    static final ABNF UTF8String = REG.rule("UTF8String", StringValue);
    
    // 4. ASN.1 Built-in Types

    // EXTERNAL
    static final ABNF idSingleASN1Type = REG.rule("id-single-ASN1-type", ABNF.bin("single-ASN1-type"));
    static final ABNF idOctetAligned = REG.rule("id-octet-aligned", ABNF.bin("octet-aligned"));
    static final ABNF idArbitary = REG.rule("id-arbitary", ABNF.bin("arbitrary"));
    static final ABNF idDirectReference = REG.rule("id-direct-reference", ABNF.bin("direct-reference"));
    static final ABNF idIndirectReference = REG.rule("id-indirect-reference", ABNF.bin("indirect-reference"));
    static final ABNF idDataValueDescriptor = REG.rule("id-data-value-descriptor", ABNF.bin("data-value-descriptor"));
    static final ABNF idEncoding = REG.rule("id-encoding", ABNF.bin("encoding"));
    static final ABNF Encoding = REG.rule("Encoding", idSingleASN1Type.pl(ABNF.bin(':'),ASN1GSER3641Reg.Value).or1(
            idOctetAligned.pl(ABNF.bin(':'),OCTET_STRING), idArbitary.pl(ABNF.bin(':'), BIT_STRING)));
    static final ABNF EXTERNAL = REG.rule("EXTERNAL", ABNF.bin('{').pl(sp.pl(idDirectReference, msp, OBJECT_IDENTIFIER, ABNF.bin(',')).c(),
            sp.pl(idIndirectReference,msp,INTEGER,ABNF.bin(',')).c(),
            sp.pl(idDataValueDescriptor, msp, ObjectDescriptor, ABNF.bin(',')).c(),
            sp, idEncoding, msp, Encoding, sp, ABNF.bin('}')));
    
    // 5
    static final ABNF century = REG.rule("century", ABNF.range(0x30, 0x39).x(2));
    static final ABNF year = REG.rule("year", ABNF.range(0x30, 0x39).x(2));
    static final ABNF month = REG.rule("month", ABNF.bin('0').pl(ABNF.range('1','9')).or1(
    ABNF.bin('1').pl(ABNF.range(0x30, 0x32))));
    static final ABNF day = REG.rule("day", ABNF.bin('0').pl(ABNF.range('1','9')).or1(
            ABNF.range('1','2').pl(ABNF.range('0','9')), ABNF.bin('3').pl(ABNF.range('0','1'))));
    static final ABNF hour = REG.rule("hour", ABNF.range('0','1').pl(ABNF.range('0','9')).or1(ABNF.bin('2').pl(ABNF.range('0','3'))));
    static final ABNF minute = REG.rule("minute", ABNF.range('0', '5').pl(ABNF.range('0','9')));
    static final ABNF second = REG.rule("second", ABNF.range('0', '5').pl(ABNF.range('0','9')).or1(ABNF.bin("60")));
    static final ABNF uDifferential = REG.rule("u-differential", ABNF.bin('-').or1(ABNF.bin('+')).pl(hour, minute));
    static final ABNF UTCTime = REG.rule("UTCTime", dquote.pl(year, month, day, hour, minute, second.c(), ABNF.bin(0x5a).or1(uDifferential).c(), dquote));
    static final ABNF fraction = REG.rule("fraction", ABNF.bin('.').or1(ABNF.bin(',')).pl(ABNF.range(0x30, 0x39).ix()));
    static final ABNF gDifferential = REG.rule("g-differential", ABNF.bin('-').or1(ABNF.bin('+')).pl(hour, minute.c()));
    static final ABNF GeneralizedTime = REG.rule("GeneralizedTime", dquote.pl(century, year, month, day, hour, minute.pl(second.c()).c(), fraction.c(), ABNF.bin(0x5a).or1(gDifferential), dquote));

    // 6.
    static final ABNF AttributeType = REG.rule("AttributeType", OBJECT_IDENTIFIER);
    static final ABNF idTeletexString = REG.rule("id-teletexString", ABNF.bin("teletexString"));
    static final ABNF idPrintableString = REG.rule("id-printableString", ABNF.bin("printableString"));
    static final ABNF idBmpString = REG.rule("id-bmpString", ABNF.bin("bmpString"));
    static final ABNF idUniversalString = REG.rule("id-universalString", ABNF.bin("universalString"));
    static final ABNF iduTF8String = REG.rule("ud-uTF8String", ABNF.bin("uTF8String"));
    static final ABNF DirectoryString = REG.rule("DirectoryString", StringValue.or1(
            idTeletexString.pl(ABNF.bin(':'),TeletexString),
            idPrintableString.pl(ABNF.bin(':'),PrintableString),
            idBmpString.pl(ABNF.bin(':'), BMPString),
            idUniversalString.pl(ABNF.bin(':'), UniversalString),
            iduTF8String.pl(ABNF.bin(':'), UTF8String)));

    static final ABNF RDNSequence = REG.rule("RDNSequence", dquote.pl(SafeUTF8Character.x(),dquote));
    static final ABNF DistinguishedName = REG.rule("DistinguishedName", RDNSequence);
    static final ABNF LocalName = REG.rule("LocalName", RDNSequence);
    
    static final ABNF RelativeDistinguishedName = REG.rule("RelativeDistinguishedName", dquote.pl(SafeUTF8Character.x(), dquote));
    static final ABNF ORAddress = REG.rule("ORAddress", dquote.pl(SafeIA5Character.x(),dquote));
}
