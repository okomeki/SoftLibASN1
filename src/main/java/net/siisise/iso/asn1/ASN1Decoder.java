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

import java.io.*;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.siisise.io.Input;
import net.siisise.io.StreamFrontPacket;

/**
 * ASN.1 のエンコード/デコード.
 * ITU-T X.680 / ISO/IEC 8824-1
 * ITU-T X.690 BER (Basic Encoding Rules) X.209 ISO 8825-1
 * ITU-T X.690 CER (Canonical Encoding Rules)
 * ITU-T X.690 DER (Distinguished Encoding Rules) X.509 (BERのサブセット)
 * ITU-T X.693 XER / ISO/IEC 8825-4
 * ITU-T X.697 JER / ISO/IEC 8825-8
 * 
 * https://www.itu.int/rec/T-REC-X.690
 * 
 * RFC 5280 Appendix A.
 *
 * CER/DERは署名のためにBERの曖昧性をいくつか取り除くための制約を設けたもの X.500 DER に対応しているつもり
 * 
 * ToDo TypeFormat に変える
 */
public class ASN1Decoder {

    public static ASN1Tag toASN1(InputStream in) {
        return toASN1(new StreamFrontPacket(in));
    }
    
    /**
     * デコーダ
     * @param in soruce
     * @return 某長さを指定しない終端のときはnull
     */
    public static ASN1Tag toASN1(Input in) {
        int code = in.read();
        ASN1Cls cl = ASN1Cls.valueOf((code >> 6) & 0x03); // 上位2bit
        boolean struct = (code & 0x20) != 0; // 構造化フラグ
        BigInteger tag = readTag(code, in);

        int inlen = readLength(in);
        if (inlen < 0 ) {
            throw new java.lang.UnsupportedOperationException();
        }
        if (code == 0 && inlen == 0) { // 終端コード
            return null;
        }
        return decode(cl, struct, tag, in, inlen);
    }

    /**
     * read tag code
     * @param code
     * @param in
     * @return tag
     */
    static BigInteger readTag(int code, Input in) {
        BigInteger tag;
        if ((code & 0x1f) != 0x1f) {
            tag = BigInteger.valueOf(code & 0x1f);
        } else {
            tag = BigInteger.ZERO;
            int d;
            do {
                d = in.read();
                if ( tag.compareTo(BigInteger.ZERO) == 0 && d == 0x80 ) {
                    throw new UnsupportedOperationException("X.690 8.1.2.4.2 c");
                }
                tag = tag.shiftLeft(7);
                tag = tag.or(BigInteger.valueOf(d & 0x7f));
            } while ((d & 0x80) != 0);
        }
        return tag;
    }

    /**
     * 長さフィールドの読み取り
     *
     * @return -1は可変長
     */
    static int readLength(Input in) {
        int len;
        len = in.read();
        if (len >= 128) {
            int len2 = len & 0x7f;
            if (len == 128 && len2 == 0) {
                //     System.out.println("可変長");
                return -1;
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

    /**
     * バイナリからObject に
     *
     * @param cl クラス 00:UNIVERSAL(汎用) 01:APPLICATION(応用) 10:Context-specific(コンテキスト特定) 11:PRIVATE(私用)
     * @param struct 構造化フラグ
     * @param tag タグ番号
     */
    static ASN1Tag decode(ASN1Cls cl, boolean struct, BigInteger tag, Input in, int length) {
        ASN1Tag object;
        switch (cl) {
            case UNIVERSAL: // 汎用
                object = universal(struct, tag, length);
                break;
            case CONTEXT_SPECIFIC: // Context-specific [2]
            case APPLICATION: // 応用 [Application 2]
            case PRIVATE: // 私用
                object = other(cl, struct, tag);
                break;
            default:
                throw new java.lang.UnsupportedOperationException("unsupported encoding yet.");
        }
        object.decodeBody(in, length);
        return object;
    }

    /**
     * UNIVERSAL Universal Object を生成するだけ
     * @param cl 参考 class universal 汎用確定済み
     * @param struct 構造?
     * @param tag タグ番号
     * @param length 参考 長さ
     * @return 
     */
    static ASN1Tag universal(boolean struct, BigInteger tag, int length) {
        ASN1Tag object = decodeTag(tag);
        if (struct) {
            if ( !(object instanceof ASN1Struct) ) {  // CER 文字列など
//                ((ASN1Struct)object). = true;
                System.out.println("構造 universal " + tag + struct);
                throw new UnsupportedOperationException("unsupported Universal Object yet.");
            }
//                    object = new ASN1Struct(cl, tag);
        } else {
//                    System.out.println("cl" + cl + "たぐs" + tag);
        }
        if (object == null) {
            System.out.println("そのた universal :" + struct + " tag:" + tag.toString(16) + " len:" + length);
            if (length > 0) {
                System.out.println("謎 0x");
            }
            throw new UnsupportedOperationException("unsupported encoding yet.");
        }
        return object;
    }
    
    /**
     * コンテクスト特定,
     * APPLICATION (Application) PRIVATE (Private)
     * 例
     * VisibleString Length Contents 
     * 1A             05     "abcde"
     * 
     * [2] 0xA2
     * [Application 3] Length Contents
     * 0x43             05     "abcde"
     * 
     * @param cl
     * @param struct
     * @param tag
     * @return 
     */
    static ASN1Tag other(ASN1Cls cl, boolean struct, BigInteger tag) {
        //Object object;
        if (struct) {
            return new ASN1StructList(cl, tag);
        } else {
            throw new UnsupportedOperationException("unsupported encoding yet.");
//            return new OCTETSTRING(); // 仮
        }
        
    }

    /**
     * タグをコードから分解
     *
     * @param tag 0x00 - 0x1e
     * @return
     */
    static ASN1Tag decodeTag(BigInteger tag) {
        int tagid = tag.intValue();

        ASN1 tagAndClass = ASN1.valueOf(tagid);
        if ( tagAndClass == null ) return null;
        ASN1Tag object;
        Class<? extends ASN1Tag> decodeClass;

        if ((decodeClass = tagAndClass.coder) != null) {
            try {
                try {
                    object = decodeClass.getConstructor(ASN1.class).newInstance(tagAndClass);
                } catch (NoSuchMethodException e) {
                    object = decodeClass.getConstructor().newInstance();
                }
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
                Logger.getLogger(ASN1Decoder.class.getName()).log(Level.SEVERE, null, ex);
                object = null;
            }
            return object;
        } else {
            return null;
        }
    }

    /**
     * PEM
     */
//    private static Map<String,Object> base64Read(String path) throws IOException {
//        PEM pem = new PEM("NEW CERTIFICATE REQUEST");
//        return pem.load( path);
//    }

}
