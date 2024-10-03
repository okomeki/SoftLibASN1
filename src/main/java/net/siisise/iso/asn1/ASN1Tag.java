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
import net.siisise.iso.asn1.tag.ASN1DERFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 型っぽいもの.
 * X.690 BER / CER / DER 
 * 
 * @param <T> 内容型
 */
public interface ASN1Tag<T> extends java.lang.Comparable<ASN1Tag> {
    /**
     * ASN1Cls の値
     * 0: UNIVERSAL
     * 1: APPLICATION
     * 2: CONTEXT-SPECIFIC
     * 3: PRIVATE
     * @return 0 ... 3
     */
    int getASN1Class();

    void setTag(ASN1Cls c, int tag);


    /**
     * tag
     * class を UNIVERSAL にし、tag を設定する.
     * @param tag タグ番号
     */
    default void setUniversal(int tag) {
        setTag(ASN1Cls.UNIVERSAL, tag);
    }

    /**
     * IMPLICIT [tag]
     * class を CONTEXT-SPECIFIC にし、tag を設定する.
     * @param tag タグ番号
     */
    default void setContextSpecific(int tag) {
        setTag(ASN1Cls.CONTEXT_SPECIFIC, tag);
    }

    /**
     * IMPLICIT [APPLICATION tag]
     * class を APPLICATION にし、tag を設定する.
     * @param tag タグ番号
     */
    default void setApplication(int tag) {
        setTag(ASN1Cls.APPLICATION, tag);
    }

    /**
     * IMPLICIT [PRIVATE tag]
     * class を PRIVATE にし、tag を設定する.
     * @param tag タグ番号
     */
    default void setPrivate(int tag) {
        setTag(ASN1Cls.PRIVATE, tag);
    }

    /**
     * 型.
     * @return UNIVERSAL / APPLICATION / CONTEXT-SPECIFIC / PRIVATE
     */
    ASN1Cls getASN1Cls();

    /**
     * 基本型 Primitive または 構造型 Constructed
     *
     * @return true : Constructed, false : Primitive
     */
    boolean isConstructed();
    BigInteger getTag();

    /**
     * tagのint型.
     * @return tag
     */
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

    /**
     * Rebind用型変換.
     * @param <V> 出力型
     * @param format 出力形式
     * @return 変換データ
     */
    <V> V rebind(TypeFormat<V> format);
    
    /**
     * ヘッダ付きDER符号化.
     * @deprecated rebindに統合して廃止していく予定
     * @return ヘッダ含むDER出力
     */
    @Deprecated
    default byte[] encodeAll() {
        ASN1DERFormat format = new ASN1DERFormat();
        return rebind(format);
    }


    /**
     * ヘッダなしDER符号化.
     * @deprecated #rebind(TypeFormat) に移行予定
     * @return DER符号化の識別子、長さのない部分
     */
    @Deprecated
    default byte[] encodeBody() {
        throw new UnsupportedOperationException("廃止");
    }
    void decodeBody(Input in, int length);

    /**
     * タグとデータを書き
     * @param doc
     * @return  */
    Element encodeXML(Document doc);
    /** データのみ読む
     * @param element */
    void decodeXML(Element element);
}
