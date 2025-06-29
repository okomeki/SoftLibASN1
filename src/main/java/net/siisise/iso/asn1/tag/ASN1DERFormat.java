/*
 * Copyright 2023 okome.
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
package net.siisise.iso.asn1.tag;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.TypeBind;
import net.siisise.bind.format.TypeFallFormat;
import net.siisise.io.BitPacket;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Cls;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import net.siisise.lang.Bin;

/**
 * ITU-T X.690 DER Format.
 * 文字列は型情報があればそれにあわせ、UTF-8出力を標準にする。
 * OBJECTIDENTIFIER を CharSequence に割り振る.
 * SEQUENCEはList、Mapどちらからでも基本的に変換可能。型情報はまだ持っていないのでMapのkeyは捨てる。
 * 
 */
public class ASN1DERFormat extends TypeFallFormat<byte[]> implements TypeBind<byte[]> {

    /**
     * DERは固定で決まる
     * X.690 202102 8.1.3.
     * 8.1.3.6. 不定形式はなし
     * a) 定型
     *
     * @param len
     * @return
     */
    Packet encodeLength(int len) {
        PacketA pac = new PacketA();
//        if ( inefinite ) {
//            pac.write(INFLEN);
//            return pac;
//        }
        pac.dwrite(Bin.toByte(len));
        int i;
        do {
            i = pac.read();
        } while (i == 0 && pac.length() > 1);
        if (i > 0) {
            pac.backWrite(i);
        }
        if (len >= 0x80) {
            pac.backWrite(0x80 + pac.size());
        }
        return pac;
    }

    /*
     * DER Encode.
     * inefinite 対応は外した
     *
     * deprecated DERをASN1Objectから分離予定
     * @param obj
     * @return DER
     */
//    @Deprecated
//    public byte[] encodeUniversal(ASN1Tag obj) {
//        byte[] body = obj.encodeBody();
//        return encodeUniversal(obj, body);
//    }

    /**
     * ASN1 Object をDER変換する.
     *
     * @param obj class struct tag の情報
     * @param contents DER encoded contents
     * @return DER ヘッダつき
     */
    public byte[] encodeDER(ASN1Tag obj, byte[] contents) {
        Packet pac = encodeLength(contents.length);
        pac.backWrite(encodeIdentifier(obj));
        pac.write(contents);
        // DER infinite なし
//        if ( obj.infinite) {
//            pac.write(EO);
//        }
        return pac.toByteArray();
    }

    /**
     * DER をまとめる.
     * タグ, length, 本体 をまとめる
     * class 0:UNIVERSAL
     * @param asn1 型 0 ～ 
     * @param contents DER符号化済み値
     * @return DER符号化
     */
    byte[] encodeUniversal(ASN1 asn1, byte[] contents) {
        Packet pac = encodeLength(contents.length);
        pac.backWrite(encodeTagNo(asn1.tag));
        pac.write(contents);
        return pac.toByteArray();
    }
/*
    byte[] encodeUniversal(ASN1Cls cls, boolean struct, BigInteger tag, byte[] contents) {
        Packet pac = encodeLength(contents.length);
        pac.backWrite(encodeIdentifier(cls, struct, tag));
        pac.write(contents);
        return pac.toByteArray();
    }
*/
    /**
     * encode identifier octets.
     * class | struct | tag から identifier octets を生成する.
     * @param obj class struct tagの情報
     * @return identifier octets
     */
    private byte[] encodeIdentifier(ASN1Tag obj) {
        BigInteger tagId = obj.getTag();
        byte[] identifier = encodeTagNo(tagId);
        identifier[0] |= obj.getASN1Class() << 6 | (obj.isConstructed() ? 0x20 : 0);
        return identifier;
    }

    /**
     * identifier の tag 部分を符号化.
     * class = 0 汎用
     * struct = false
     *
     * @param tagId 0 ～ 31
     * @return
     */
    private byte[] encodeTagNo(BigInteger tagId) {
        int bitLen = tagId.bitLength();
        byte[] tagNo;
        if ( bitLen <= 5 && tagId.intValue() < 31) { // 0 - 30
            tagNo = new byte[1];
            tagNo[0] = (byte) tagId.intValue();
        } else {
            int len = (bitLen + 6) / 7;
            tagNo = new byte[len + 1];
            BigInteger t = tagId;
            for (int i = 0; i < len; i++) {
                tagNo[i + 1] = (byte) (((i == len - 1) ? 0x80 : 0) | t.shiftRight((len - i - 1) * 7).intValue() & 0x7f);
            }
            tagNo[0] = (byte) 0x1f;
        }
        return tagNo;
    }

    /**
     * NULL
     *
     * @return DER NULL
     */
    @Override
    public byte[] nullFormat() {
        return encodeUniversal(ASN1.NULL, new byte[0]);
    }

    /**
     * X.690 202102 8.2.
     * 0x01
     *
     * @param bool
     * @return DER BOOLEAN
     */
    @Override
    public byte[] booleanFormat(boolean bool) {
        return encodeUniversal(ASN1.BOOLEAN, new byte[]{(byte) (bool ? 0xff : 0)});
    }

    /**
     * X.690.
     * 8.3. 整数値 INTEGER
     * 8.5. 実数値 REAL
     *
     * @param num
     * @return DER NUMBER
     */
    @Override
    public byte[] numberFormat(Number num) {
        if (num instanceof Integer || num instanceof Long || num instanceof Short || num instanceof Byte) {
            num = BigInteger.valueOf(num.longValue());
        }
        if (num instanceof BigInteger) {
            return encodeUniversal(ASN1.INTEGER, ((BigInteger) num).toByteArray());
        } else if (num instanceof BigDecimal ) {
            return encodeUniversal(ASN1.REAL, encodeDecimalBody((BigDecimal)num));
        } else if (num instanceof Double || num instanceof Float) {
            return encodeUniversal(ASN1.REAL, encodeDoubleBody(num.doubleValue()));
        }
        throw new UnsupportedOperationException();
    }

    static final BigInteger TEN = BigInteger.valueOf(10);
    static final int NR3HEAD = 0x03;
    
    public byte[] encodeDecimalBody(BigDecimal val) {
        
        if ( val.signum() == 0 ) {
            return new byte[0];
        }
        
        // unscaledValue * 10^-scale
        int scale = val.scale(); // + 小数点以下の桁数
        BigInteger us = val.unscaledValue();
        while ( us.mod(TEN).compareTo(BigInteger.ZERO) == 0 ) {
            scale--;
            us = us.divide(TEN);
        }
        StringBuilder m = new StringBuilder();
        m.append(us.toString());
        //int nl = n.length();
        m.append(".E");
        if ( scale == 0 ) {
            m.append("+0");
        } else {
            m.append(Integer.toString(scale));
        }
        
        Packet pac = new PacketA();
        pac.write(NR3HEAD); // 0000 0011
        pac.write(m.toString().getBytes(StandardCharsets.ISO_8859_1));
        return pac.toByteArray();
    }

    static final byte PLUS_INFINITY = 0x40;
    static final byte MINUS_INFINITY = 0x41;
    static final byte NaN = 0x42;
    static final byte MINUS_ZERO = 0x43;

    /**
     * Double型の精度で IEEE754 format から ASN.1 DER 2進数表記に変換する.
     * F = 0
     * @param v
     * @return ASN.1 REAL型
     */
    public byte[] encodeDoubleBody(double v) {
        long ieee754 = Double.doubleToRawLongBits(v);
        boolean flag = (ieee754 & 0x8000000000000000l) != 0;
        int exp = (int) (ieee754 >>> 52) & 0x7ff; // 符号 - * 2^1 を 0x400 的なところにもってくる
        long m = ieee754 & 0x000fffffffffffffl; // fraction
        switch (exp) {
            case 0:
                // 非正規化 または 0
                if (m == 0) {
                    if (flag) {
                        return new byte[]{MINUS_ZERO};
                    } else {
                        return new byte[0];
                    }
                }
                // 非正規化数
                exp++; // 元の精度に戻すだけ
                break;
            case 0x7ff:
                if (m == 0) { // infinity
                    if (flag) {
                        return new byte[]{MINUS_INFINITY};
                    } else {
                        return new byte[]{PLUS_INFINITY};
                    }
                } else { // NaN
                    return new byte[]{NaN};
                }
            default:
                m |= 0x0010000000000000l; // 最上位ビットをつける
                break;
        }
        // 指数位置の変換
        while ( (m & 1) == 0) {
            m >>>= 1;
            exp++;
        }
        exp -= 52 + 0x3ff; // ビット + 符号

        Packet pac = new PacketA();
        int b0 = (flag ? 0xc0 : 0x80); // bS0000
        if (exp < -128 || 127 < exp) { // exp 2バイトコース
            b0 |= 0x01;
            pac.write(b0);
            pac.write(Bin.toByte((short)exp));
        } else { // 1バイトコース
            pac.write(b0);
            pac.write(exp);
        }
        // m のINTEGER風符号化 長さは残りサイズ
        pac.write(BigInteger.valueOf(m).toByteArray());
        return pac.toByteArray();
    }

    /**
     * OCTETSTRING
     *
     * @param bytes バイト列
     * @return DER OCTETSTRING
     */
    @Override
    public byte[] byteArrayFormat(byte[] bytes) {
        return encodeUniversal(ASN1.OCTETSTRING, bytes);
    }

    /**
     * X.680 3.8.7 ビット列. (仮)
     * X.690 8.6 bitstring値の符号化.
     * 末尾はbit8 (上側)から埋める.
     * @param bits BigBitPacket
     * @return DER BITSTRING
     */
    @Override
    public byte[] bitArrayFormat(BitPacket bits) {
//        BITSTRING bit = new BITSTRING(bits);
        long bitlen = bits.bitLength();
        int nbitlen = (int)(-(bitlen % 8)) & 7;
        bits.backWrite((int) nbitlen); // 未使用ビット
        if ( nbitlen > 0 ) { // ビット位置補正とPadding
            int nbit = bits.backReadInt((int)(bitlen % 8));
            nbit <<= nbitlen;
            bits.write(nbit);
        }
        return encodeUniversal(ASN1.BITSTRING, bits.toByteArray());
    }

    /**
     * UTF8String にしてみる
     * X.680 3.8.9 3.8.10 3.8.11 3.8.12 3.8.13
     *
     * @param str string
     * @return DER UTF8String
     */
    @Override
    public byte[] stringFormat(String str) {
        return encodeUniversal(ASN1.UTF8String, str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ASN1String で型情報を変換可能にしておく.
     * OBJECTIDENTIFIERを区別する.
     * @param seq ASN1String または その他のCharSequence
     * @return ASN1String または OBJECTIDENTIFIER
     */
    @Override
    public byte[] stringFormat(CharSequence seq) {
        if ( seq instanceof ASN1String ) {
            // 汎用
            ASN1Object<String> v = (ASN1Object)seq;
            ASN1Cls cls = v.getASN1Cls();
            
            String str = v.getValue();
            Charset enc = StandardCharsets.ISO_8859_1; // US_ASCII または ISO_8859_1
            if ( cls == ASN1Cls.UNIVERSAL) {
                BigInteger tag = v.getTag();
                ASN1 asn = ASN1.valueOf(tag.intValue());
                if ( asn == ASN1.UTF8String ) {
                    enc = StandardCharsets.UTF_8;
                } else if ( asn == ASN1.BMPString ) {
                    enc = StandardCharsets.UTF_16BE; // UCS2かもしれない
                }
            }
            return encodeDER((ASN1Tag)seq, str.getBytes(enc));
        } else {
            return stringFormat(seq.toString());
        }
    }

    /**
     * URI処理.
     * @param uri URI3986ではないので注意
     * @return 
     */
    @Override
    public byte[] uriFormat(URI uri) {
        String scheme = uri.getScheme();
        if ("urn".equals(scheme)) {
            String u = uri.toString();
            if ( u.startsWith("urn:oid:")) {
                return oidFormat(u.substring(8));
            }
        }
        return stringFormat(uri.toString());
    }

    /**
     * OBJECTIDENTIFIER の組み立て.
     * @param oid
     * @return OIDのDER形式出力
     */
    private byte[] oidFormat(String oid) {
        List<String> list = Arrays.asList(oid.split("\\."));
        Packet pac = new PacketA();
        pac.write( (byte) (Integer.parseInt(list.get(0)) * 40 + Integer.parseInt(list.get(1))));
        for (int n = 2; n < list.size(); n++) {
            pac.write(encodeOid(list.get(n)));
        }
        return encodeUniversal(ASN1.OBJECTIDENTIFIER, pac.toByteArray());
    }

    private byte[] encodeOid(String val) {
        BigInteger bigVal = new BigInteger(val);
        byte[] data = new byte[bigVal.bitLength() == 0 ? 1 : ((bigVal.bitLength() + 6) / 7)];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (bigVal.shiftRight((data.length - i - 1) * 7).intValue() & 0x7F);
            if (i < data.length - 1) {
                data[i] |= (byte) 0x80;
            }
        }
        return data;
    }

    /**
     * SEQUENCE っぽい系統.
     * SEQUENCEMap, ASN1Prefixed に対応する予定.
     *
     * @param map SEQUENCE になるもの
     * @return DER SEQUENCE
     */
    @Override
    public byte[] mapFormat(Map map) {
        Packet pac = new PacketA();
        for ( Object o : map.values() ) {
            pac.write(Rebind.valueOf(o, this));
        }
        if ( !(map instanceof ASN1Tag) ) { // SEQUENCE Tag
            map = new SEQUENCEMap();
        }
        return encodeDER((ASN1Tag)map, pac.toByteArray());
    }
    
    /**
     * SEQUENCE / SEQUENCE OF
     *
     * @param list 普通のList
     * @return DER SEQUENCE / SEQUENCE OF
     */
    @Override
    public byte[] listFormat(List list) {
        Packet pac = new PacketA();
        for ( Object o : list ) {
            pac.write(Rebind.valueOf(o, this));
        }
        if ( !(list instanceof ASN1Tag) ) {
            list = new SEQUENCEList();
        }
        return encodeDER((ASN1Tag)list, pac.toByteArray());
    }

    /**
     * SET / SET OF.
     * DERではソートされる
     * @param set Set系 collection
     * @return SET
     */
    @Override
    public byte[] setFormat(Set set) {
        ASN1Convert cnv = new ASN1Convert();
        SEQUENCEList<ASN1Tag> seq = SEQUENCEList.SET();
        
        for ( Object o : set ) {
            seq.add(Rebind.valueOf(o, cnv));
//            pac.write(Rebind.valueOf(o, this));
        }
        Collections.sort(seq);
        Packet pac = new PacketA();
        for ( ASN1Tag t : seq ) {
            pac.write((byte[])t.rebind(this));
        }
        return encodeDER(seq, pac.toByteArray());
    }
    
    /**
     * SEQUENCE
     *
     * @param col
     */
    @Override
    public byte[] collectionFormat(Collection col) {
        if ( col instanceof List) {
            return listFormat((List) col);
        } else if ( col instanceof Set) {
            return setFormat((Set)col);
        }
        return listFormat(new ArrayList(col));
    }

}
