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
package net.siisise.iso.asn1;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import net.siisise.block.OverBlock;
import net.siisise.block.ReadableBlock;
import net.siisise.io.Input;
import net.siisise.iso.asn1.tag.ASN1Prefixed;
import net.siisise.iso.asn1.tag.EndOfContent;
import net.siisise.iso.asn1.tag.OCTETSTRING;

/**
 *
 */
public abstract class ASN1X690 {

    /**
     * class 分け
     * @param identifier cls と constructed 1バイト目 (未使用) 
     * @param cls 1バイト目 MSB 2bit
     * @param constructed 1バイト目 0x20 flag
     * @param tag 残りでタグ
     * @param len -1 不定形 または 長さ
     * @param in 入力可能範囲
     * @return
     */
    ASN1Tag decode(int identifier, ASN1Cls cls, boolean constructed, BigInteger tag, long len, ReadableBlock in) {
        ASN1Tag object;
        switch (cls) {
            case UNIVERSAL: // Universal 汎用
                // body込み?
                object = universal(cls, constructed, tag, len, in);
                if (object instanceof ASN1Struct) {
                    return object;
                }
                break;
//            case 応用: // Application [Application 2]
//            case コンテキスト特定: // Context-specific [2]
//            case PRIVATE: // Private
            default:
                object = other(cls, constructed, tag);
        }
        object.decodeBody(in, (int) len);
        if ( len >= 0 && in.length() != 0) {
                throw new IllegalStateException();
        }
        return object;
    }

    ReadableBlock subBlock(Input in, long len) {
        ReadableBlock contents;
        if ( in instanceof ReadableBlock ) {
            contents = ((ReadableBlock)in).readBlock(len);
        } else {
            OverBlock b = OverBlock.wrap(new byte[(int)len]);
            in.read(b);
            contents = b;
        }
        if (contents.length() < len) {
            throw new java.lang.IllegalStateException("length" + len);
        }
        return contents;
    }

    /**
     * UNIVERSAL 汎用.
     * @param identifier
     * @param constructed 解析用
     * @param tag
     * @param length 解析用 -1 不特定
     * @return
     */
    ASN1Tag universal(ASN1Cls cl, boolean constructed, BigInteger tag, long length, ReadableBlock in) {
        ASN1Tag object = decodeUniversalTag(tag);
        if (object == null || object instanceof EndOfContent ) {
            throw new UnsupportedOperationException("unsupported " + cl + tag + constructed + length + " encoding yet.");
        }
        if (constructed) {
            if (object instanceof ASN1Struct) {
//                ((ASN1Struct) object).attrStruct = true;
                return decodeUniversalStructBody((ASN1Struct) object, length, in);
            } else {
                throw new UnsupportedOperationException("unsupported " + cl + tag + constructed + " encoding yet.");
            }
        } else if ( object instanceof ASN1Struct ) {
            throw new UnsupportedOperationException("unsupported " + cl + tag + constructed + " encoding yet.");
        }
        
        return object;
    }

    /**
     * [2] などと定義するタイプ
     * @param cls
     * @param constructed
     * @param tag
     * @return 
     */
    ASN1Tag other(ASN1Cls cls, boolean constructed, BigInteger tag) {
        ASN1Tag object;
        if (constructed) { // [2] Type 構造として中に Type を持つ EXPLICIT
            object = new ASN1StructList(cls, tag);
        } else { // [2] IMPLICIT Type で Type を上書きする場合
            object = new ASN1Prefixed(cls, tag); // 仮
        }
        return object;
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

    /**
     * 汎用型の生成.
     * @param tag 汎用タグの範囲
     * @return 汎用
     */
    ASN1Tag decodeUniversalTag(BigInteger tag) {
        ASN1 asn = ASN1.valueOf(tag.intValue());
        if ( asn == null || asn.coder == null ) {
//            throw new UnsupportedOperationException();
            return new OCTETSTRING(ASN1Cls.UNIVERSAL,tag);
        }
        Constructor<? extends ASN1Tag>[] constructs = (Constructor<? extends ASN1Tag>[]) asn.coder.getConstructors();
        
        Constructor<? extends ASN1Tag> c0 = null; // パラメータなし コンストラクタ
        Constructor<? extends ASN1Tag> c1 = null; // 型のみ コンストラクタ
        
        for ( Constructor<? extends ASN1Tag> c : constructs ) {
            Type[] ps = c.getGenericParameterTypes();
            if ( ps.length == 0 ) {
                c0 = c;
            } else if ( ps.length == 1 && ps[0] == ASN1.class ) {
                c1 = c;
            }
        }
        try {
            if ( c1 != null ) {
                return c1.newInstance(asn);
            }
            if ( c0 != null) {
                return c0.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new UnsupportedOperationException(ex);
        }
        throw new UnsupportedOperationException();
    }

    abstract ASN1Tag decodeUniversalStructBody(ASN1Struct asN1Struct, long length, ReadableBlock in);

}
