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
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.TypeBind;
import net.siisise.bind.format.TypeFallFormat;
import net.siisise.io.BitPacket;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.lang.Bin;

/**
 * ASN1Object経由の手抜き版 ITU-T X.690 DER
 */
public class ASN1DERFormat extends TypeFallFormat<byte[]> implements TypeBind<byte[]> {

    ASN1Convert cnv = new ASN1Convert();

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

    /**
     * DER Encode.
     * inefinite 対応は外した
     *
     * @deprecated DERをASN1Objectから分離予定
     * @param obj
     * @return DER
     */
    @Deprecated
    public byte[] encodeDER(ASN1Object obj) {
        byte[] body = obj.encodeBody();
        return encodeDER(obj, body);
    }

    /**
     * ASN1 Object をDER変換する.
     *
     * @param tagNo tagId
     * @param body DER encoded body
     * @return DER ヘッダつき
     */
    byte[] encodeDER(ASN1Object obj, byte[] body) {
        Packet pac = encodeLength(body.length);
        pac.backWrite(encodeTagNo(obj));
        pac.write(body);
        // DER infinite なし
//        if ( obj.infinite) {
//            pac.write(EO);
//        }
        return pac.toByteArray();
    }

    byte[] encodeDER(ASN1 asn1, byte[] body) {
        Packet pac = encodeLength(body.length);
        pac.backWrite(encodeTagNo(asn1.tag));
        pac.write(body);
        return pac.toByteArray();
    }

    /**
     * class = 0 汎用
     * struct = false
     *
     * @param tagId
     * @return
     */
    byte[] encodeTagNo(BigInteger tagId) {
        byte[] tagNo = new byte[1];
        tagNo[0] = (byte) tagId.intValue();
        return tagNo;
    }

    /**
     * encode TagNo
     *
     * @param obj
     * @return
     */
    byte[] encodeTagNo(ASN1Object obj) {
        BigInteger tagId = obj.getTag();
        int bitLen = tagId.bitLength();
        byte[] tagNo;

        if (bitLen <= 5) {
            tagNo = new byte[1];
            tagNo[0] = (byte) ((obj.getASN1Class() << 6) | (obj.isStruct() ? 0x20 : 0) | tagId.intValue());
        } else {
            int len = (bitLen + 6) / 7;
            tagNo = new byte[len + 1];
            BigInteger t = tagId;
            for (int i = 0; i < len; i++) {
                tagNo[i + 1] = (byte) (((i == len - 1) ? 0x80 : 0) | t.shiftRight((len - i - 1) * 7).intValue() & 0x7f);
            }
            tagNo[0] = (byte) ((obj.getASN1Class() << 6) | (obj.isStruct() ? 0x20 : 0) | 0x1f);
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
        return encodeDER(ASN1.NULL, new byte[0]);
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
        return encodeDER(ASN1.BOOLEAN, new byte[]{(byte) (bool ? 0xff : 0)});
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
            num = BigInteger.valueOf((long) num);
        }
        if (num instanceof BigInteger) {
            INTEGER i = (INTEGER) cnv.numberFormat(num);
            return encodeDER(i, ((BigInteger) num).toByteArray());
        }
        if (num instanceof BigDecimal ) {
            REAL r = new REAL((BigDecimal)num);
            return encodeDER(r);
        } else if (num instanceof Double || num instanceof Float) {
            REAL r = new REAL((Double)num);
            return encodeDER(r);
        }
        ASN1Object obj = cnv.numberFormat(num);
        return encodeDER(obj);
    }

    /**
     * OCTETSTRING
     *
     * @param bytes
     * @return DER OCTETSTRING
     */
    @Override
    public byte[] byteArrayFormat(byte[] bytes) {
//        OCTETSTRING oct = new OCTETSTRING(bytes);
        return encodeDER(ASN1.OCTETSTRING, bytes);
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
        return encodeDER(ASN1.BITSTRING, bits.toByteArray());
    }

    /**
     * UTF8String にしてみる
     * X.680 3.8.9 3.8.10 3.8.11 3.8.12 3.8.13
     *
     * @param str
     * @return DER UTF8String
     */
    @Override
    public byte[] stringFormat(String str) {
//        ASN1String asn = new ASN1String(ASN1.UTF8String,str);
        return encodeDER(ASN1.UTF8String, str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 並び順が保証されていればSEQUENCEとして使える
     * なければSETとして使える
     *
     * @param map
     * @return DER SEQUENCE / SET
     */
    @Override
    public byte[] mapFormat(Map map) {
        return encodeDER(cnv.mapFormat(map));
    }

    /**
     * SEQUENCE / SEQUENCE OF
     *
     * @param list
     * @return DER SEQUENCE / SEQUENCE OF
     */
    @Override
    public byte[] listFormat(List list) {
        return encodeDER(cnv.listFormat(list));
    }

    Packet list(Collection col) {
        Packet pac = new PacketA();
        for (Object v : col) {
            pac.write(Rebind.valueOf(v, this));
        }
        return pac;
    }

    /**
     * SEQUENCE
     *
     * @param col
     */
    @Override
    public byte[] collectionFormat(Collection col) {
        return encodeDER(cnv.collectionFormat(col));
    }

}
