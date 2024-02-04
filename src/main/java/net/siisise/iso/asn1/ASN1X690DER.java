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
package net.siisise.iso.asn1;

import java.math.BigInteger;
import net.siisise.block.ReadableBlock;
import net.siisise.io.Input;
import net.siisise.iso.asn1.tag.OCTETSTRING;

/**
 * ITU-T X.690 DER
 */
public class ASN1X690DER {

    /**
     * DER Decode. BER/CERもだいたいおなじ.
     *
     * @param in 入力元
     * @return ASN1Object
     */
    public ASN1Object decodeDER(ReadableBlock in) {
        // BER/CER/DER
        int identifier = (byte) in.read();
        ASN1Cls cls = ASN1Cls.valueOf((identifier >> 6) & 0x03); // 上位2bit
        boolean constructed = (identifier & 0x20) != 0;
        BigInteger tagNumber = readTag(identifier, in);
        long len = readLength(in);
//        if ( len < 0 && !constructed) { // primitive の不定サイズ 不可 BER
        if (len < 0) { // primitive の不定サイズ 不可 DER structured の不定形もたぶん不可
            throw new java.lang.IllegalStateException("length");
//            contents = in;
        }
        ReadableBlock contents = in.readBlock(len);

        return decode(identifier, cls, constructed, tagNumber, len, contents);
    }

    /**
     *
     * @param identifier
     * @param cls
     * @param constructed
     * @param tag
     * @param len -1 不定形 または 長さ
     * @param in
     * @return
     */
    ASN1Object decode(int identifier, ASN1Cls cls, boolean constructed, BigInteger tag, long len, ReadableBlock in) {
        ASN1Object object;
        switch (cls) {
            case 汎用: // Universal
                object = universal(cls, constructed, tag, len);
                break;
//            case 応用: // Application
//            case コンテキスト特定: // Context-specific
//            case プライベート: // Private
            default:
                object = other(cls, constructed, tag);
        }
        object.decodeBody(in, (int) len);
        return object;
    }

    /**
     *
     * @param identifier
     * @param constructed 解析用
     * @param tag
     * @param length 解析用
     * @return
     */
    ASN1Object universal(ASN1Cls cl, boolean constructed, BigInteger tag, long length) {
        ASN1Object object = decodeTag(tag);
        if (constructed) {
            if (object instanceof ASN1Struct) {
                ((ASN1Struct) object).attrStruct = true;
            } else {
                System.out.println("構造" + cl + tag + constructed);
                throw new UnsupportedOperationException("unsupported " + cl + tag + constructed + " encoding yet.");
            }
//            object = new ASN1Struct(cl, tag);
//        } else {
//                    System.out.println("cl" + cl + "たぐs" + tag);
        }
        if (object == null) {
            System.out.println("そのた 0x" + cl + ":" + constructed + " tag:" + tag.toString(16) + " len:" + length);
            if (length > 0) {
                System.out.println("謎 len:" + length);
            }
            throw new UnsupportedOperationException("unsupported encoding yet.");
        }
        throw new UnsupportedOperationException();
    }

    ASN1Object decodeTag(BigInteger tag) {
        throw new UnsupportedOperationException();
    }

    ASN1Object other(ASN1Cls cls, boolean constructed, BigInteger tag) {
        ASN1Object object;
        if (constructed) {
            object = new ASN1Struct(cls, tag);
        } else {
            object = new OCTETSTRING(); // 仮
//                    throw new java.lang.UnsupportedOperationException("unsupported encoding yet.");
        }
        throw new UnsupportedOperationException();
    }

    /**
     * read tag code
     *
     * @param code
     * @param in
     * @return tag
     */
    static BigInteger readTag(int code, Input in) {
        BigInteger tag;
        if ((code & 0x1f) != 0x1f) {
            // 8.1.2.3
            tag = BigInteger.valueOf(code & 0x1f);
        } else { // 8.1.2.4
            tag = BigInteger.ZERO;
            int d;
            do {
                d = in.read();
                if (tag.compareTo(BigInteger.ZERO) == 0 && d == 0x80) {
                    throw new UnsupportedOperationException("X.690 8.1.2.4.2 c");
                }
                tag = tag.shiftLeft(7);
                tag = tag.or(BigInteger.valueOf(d & 0x7f));
            } while ((d & 0x80) != 0);
        }
        return tag;
    }

    Input toDER(ASN1Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * サイズがほしい
     *
     * @param in
     * @return -1 可変長 0以上 サイズ
     */
    long readLength(Input in) {
        // 8.1.3
        // a) 定型    8.1.3.3  primitive はこっち
        // b) 不定形  8.1.3.6 structure はこっちもあるかも
        long len;
        len = in.read();
        if (len >= 128) {
            long len2 = len & 0x7f;
            if (len == 128 && len2 == 0) { // DER では不可
                return -1; // 可変長
            }
            len = 0;
            for (int cnt = 0; cnt < len2; cnt++) {
                len <<= 8;
                len += in.read();
                //     length++;
            }
        }
        return len;
    }

}
