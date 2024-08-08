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
import net.siisise.bind.format.TypeFormat;
import net.siisise.io.Input;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 型っぽいもの.
 * X.690 BER / CER / DER 
 * @param <T>
 */
public interface ASN1Tag<T> extends java.lang.Comparable<ASN1Tag> {
/*
    public static final int BOOLEAN = 0x01;
    public static final int INTEGER = 0x02;
    public static final int BITSTRING = 0x03;
    public static final int OCTETSTRING = 0x04;
    public static final int NULL = 0x05;
    public static final int OBJECTIDENTIFIER = 0x06;
    public static final int ObjectDescriptor = 0x07;
    public static final int EXTERNAL = 0x08;
    public static final int REAL = 0x09;
    public static final int ENUMERATED = 0x0A;
    public static final int EMBEDDEDPOV = 0x0B; // X.690
    public static final int UTF8String = 0x0C;
    public static final int RELATIVE_OID = 0x0D; // X.690
    public static final int SEQUENCE = 0x10;
    public static final int SET = 0x11;
    public static final int NumericString = 0x12;
    public static final int PrintableString = 0x13;
    public static final int TeletexString = 0x14;
    public static final int VideotexString = 0x15;
    public static final int IA5String = 0x16;
    public static final int UTCTime = 0x17;
    public static final int GeneralizedTime = 0x18; // 2050年以降
    public static final int GraphicString = 0x19;
    public static final int VisibleString = 0x1A;
    public static final int GeneralString = 0x1B;
    public static final int UniversalString = 0x1C; // X.690
    public static final int CharacterString = 0x1D; // X.690
    public static final int BMPString = 0x1E; // X.690
    public static final int 拡張 = 0x1F;
*/
    /**
     * ASN1Cls の値
     * 0: UNIVERSAL
     * 1: APPLICATION
     * 2: CONTEXT-SPECIFIC
     * 3: PRIVATE
     * @return 0 ... 3
     */
    int getASN1Class();
    ASN1Cls getASN1Cls();
    /**
     * 基本型 Primitive または 構造型 Constructed
     * isConstructed に変えるかも.
     * @return true : Constructed, false : Primitive
     * @deprecated 
     */
    @Deprecated
    boolean isStruct();
    BigInteger getTag();
    int getId();
    
    /**
     * 値の取得.
     * @return 値
     */
    T getValue();

    /**
     * 値のセット.
     * @param val
     */
    void setValue(T val);
    
    <V> V rebind(TypeFormat<V> format);
    
    /**
     * ヘッダ付きDER符号化.
     * @return 
     */
    byte[] encodeAll();
    /**
     * ヘッダなしDER符号化.
     * @return 
     */
    byte[] encodeBody();
    void decodeBody(Input in, int length);

// interface で <E> が使えない
//    <E> E rebind(TypeFormat<E> format);
    
    Element encodeXML(Document doc);
    void decodeXML(Element ele);
}
