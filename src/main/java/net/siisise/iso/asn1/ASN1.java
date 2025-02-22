/*
 * Copyright 2019-2022 Siisise Net.
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
package net.siisise.iso.asn1;

import java.math.BigInteger;
import net.siisise.iso.asn1.tag.ASN1String;
import net.siisise.iso.asn1.tag.BITSTRING;
import net.siisise.iso.asn1.tag.BOOLEAN;
import net.siisise.iso.asn1.tag.EndOfContent;
import net.siisise.iso.asn1.tag.GeneralizedTime;
import net.siisise.iso.asn1.tag.INTEGER;
import net.siisise.iso.asn1.tag.NULL;
import net.siisise.iso.asn1.tag.OBJECTIDENTIFIER;
import net.siisise.iso.asn1.tag.OCTETSTRING;
import net.siisise.iso.asn1.tag.REAL;
import net.siisise.iso.asn1.tag.SEQUENCEList;

/**
 * X.680 8.4 ASN.1 UNIVERSAL class の型.
 * X.208 (1988) → X.680 (1995)
 * 
 * RFC 4049→6019 ASN.1で日付と時刻 とか
 * RFC 5280 Appendix A.
 * RFC 5911 5912 未読
 * JIS X 5603
 * デコーダー込み
 * 名前はXML型に利用する.
 */
public enum ASN1 {
    EndOfContent(0,EndOfContent.class),
    BOOLEAN(0x01,BOOLEAN.class),
    INTEGER(0x02,INTEGER.class),
    BITSTRING(0x03,BITSTRING.class),
    OCTETSTRING(0x04,OCTETSTRING.class),
    NULL(0x05,NULL.class),
    OBJECTIDENTIFIER(0x06,OBJECTIDENTIFIER.class),
    ObjectDescriptor(0x07,null),
    EXTERNAL(0x08,null), // External and Instance-of
    REAL(0x09,REAL.class),
    ENUMERATED(0x0A,null),
    EMBEDDED_POV(0x0B,null), // X.690
    UTF8String(0x0C,ASN1String.class),
    RELATIVE_OID(0x0D,null), // X.690
    UNDEF_0E(0x0e,null), // The time
    UNDEF_0F(0x0f,null), // 予約済み
    SEQUENCE(0x10,SEQUENCEList.class), // Sequence / Sequence-of
    SET(0x11,SEQUENCEList.class), // Set / Set-if
    NumericString(0x12,null),
    PrintableString(0x13,ASN1String.class),
    TeletexString(0x14,ASN1String.class), // 廃止?
    VideotexString(0x15,null), // 廃止?
    IA5String(0x16,ASN1String.class),
    UTCTime(0x17,ASN1String.class),
    GeneralizedTime(0x18,GeneralizedTime.class), // 2050年以降
    GraphicString(0x19,null),
    VisibleString(0x1A,null),
    GeneralString(0x1B,null),
    CharacterString(0x1C,ASN1String.class), // UniversalString 廃止? UCS-4 String
    CHARACTER_STRING(0x1d,null), // X.690
    BMPString(0x1e,ASN1String.class), // 廃止?
    DATE(0x1f, null),
    TIME_OF_DAY(0x20, null),
    DATE_TIME(0x21, null),
    DURATION_respectively(0x22,null),
    OID_IRI(0x23, ASN1String.class),
    RelativeOID_IRI(0x24, ASN1String.class);

    public final BigInteger tag;
    Class<? extends ASN1Tag> coder;

    ASN1(int id, Class<? extends ASN1Tag> dc) {
        tag = BigInteger.valueOf(id);
        coder = dc;
    }

    /**
     * タグIDとclassを格納したもの
     * @param id tag ID
     * @return 該当タグ
     */
    public static ASN1 valueOf(int id) {
        if (id > 0x24) {
            return null;
        }
        return ASN1.values()[id];
    }

}
