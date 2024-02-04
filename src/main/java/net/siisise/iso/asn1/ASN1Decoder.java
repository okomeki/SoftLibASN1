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
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.siisise.io.Input;
import net.siisise.io.StreamFrontPacket;
import net.siisise.iso.asn1.tag.OCTETSTRING;

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
/*
    enum EncodeType {
        BER, // Basic Encoding Rules
        CER, //
        DER, // Distinguished EncodingRules (BER + 正規化 ソート等) -> PEM化可能
        XER,
        JER
    }

    EncodeType encode = EncodeType.DER;
*/
    /*
     * ASN1Cls へ
     */
    // static final String[] 種類 = {"汎用", "応用", "コンテキスト特定", "プライベート"};

    public static ASN1Object toASN1(InputStream in) {
        return toASN1(new StreamFrontPacket(in));
    }
    
    /**
     * デコーダ
     * @param in soruce
     * @return 某長さを指定しない終端のときはnull
     */
    public static ASN1Object toASN1(Input in) {
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
     * @param cl クラス 00:Universal(汎用) 01:Application(応用)
     * 10:Context-specific(コンテキスト特定) 11:Private(プライベート)
     * @param struct 構造化フラグ
     * @param tag タグ番号
     */
    static ASN1Object decode(ASN1Cls cl, boolean struct, BigInteger tag, Input in, int length) {
        ASN1Object object;
        switch (cl) {
            case 汎用: // Universal
                object = universal(cl, struct, tag, length);
                break;
            case コンテキスト特定: // Context-specific
            case 応用: // Application
            case プライベート: // Private
//                System.out.println(cl.toString() + " 目印 " + tag);
                /*
             * if (inlen > 0) { tmp = new byte[inlen]; in.read(tmp); if (tmp[0]
             * == 0x30 || (code & 0x20) != 0) { ASN1 asn = new ASN1();
             * asn.toASN1(tmp); } }
             *
                 */
                object = other(cl, struct, tag);
                if (struct) {
                    object = new ASN1Struct(cl, tag);
                } else {
//                    object = new OCTETSTRING(); // 仮
                    throw new java.lang.UnsupportedOperationException("unsupported encoding yet.");
                }
                break;
            default:
                throw new java.lang.UnsupportedOperationException("unsupported encoding yet.");
        }
        object.decodeBody(in, length);
        return object;
    }

    /**
     * 汎用 Universal Object を生成するだけ
     * @param cl 参考 class
     * @param struct 構造?
     * @param tag タグ番号
     * @param length 参考 長さ
     * @return 
     */
    static ASN1Object universal(ASN1Cls cl, boolean struct, BigInteger tag, int length) {
        ASN1Object object = decodeTag(tag);
        if (struct) {
            if ( object instanceof ASN1Struct ) {
                ((ASN1Struct)object).attrStruct = true;
            } else {
                System.out.println("構造" + cl + tag + struct);
                throw new UnsupportedOperationException("unsupported Universal Object yet.");
            }
//                    object = new ASN1Struct(cl, tag);
        } else {
//                    System.out.println("cl" + cl + "たぐs" + tag);
        }
        if (object == null) {
            System.out.println("そのた 0x" + cl + ":" + struct + " tag:" + tag.toString(16) + " len:" + length);
            if (length > 0) {
                System.out.println("謎 0x");
            }
            throw new UnsupportedOperationException("unsupported encoding yet.");
        }
        return object;
    }
    
    static ASN1Object other(ASN1Cls cl, boolean struct, BigInteger tag) {
        //Object object;
        if (struct) {
            return new ASN1Struct(cl, tag);
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
    static ASN1Object decodeTag(BigInteger tag) {
        int tagid = tag.intValue();
    //    if ( tagid == 12) {
    //        System.err.println("ないもの" + tagid);
    //    }
            
        ASN1 tagAndClass = ASN1.valueOf(tagid);
    //    if ( tagAndClass == null) {
    //        System.err.println("ないもの" + tagid);
    //    }
        ASN1Object object;
        Class<? extends ASN1Object> decodeClass;

//        if (DECODE_CLASSES.length > tagAndClass.tag.longValue()
//               && (decodeClass = DECODE_CLASSES[tagAndClass.tag.intValue()]) != null) {
        if (ASN1.values().length > tagAndClass.tag.longValue()
                && (decodeClass = ASN1.valueOf(tagAndClass.tag.intValue()).coder) != null) {
            /*
             case OBJECTIDENTIFIER: // 0x06
             Class<? extends ASN1Object> cl;
             cl = (Class<ASN1Object>) java.lang.Class.forName("net.siisise.iso.asn1.tag." + tagAndClass.toString());
             object = cl.getConstructor().newInstance();
             break;
             */
            try {
                try {
                    Constructor<? extends ASN1Object> cnst = decodeClass.getConstructor();
                    object = cnst.newInstance();
                } catch (NoSuchMethodException e) {
                    object = decodeClass.getConstructor(ASN1.class).newInstance(tagAndClass);
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
