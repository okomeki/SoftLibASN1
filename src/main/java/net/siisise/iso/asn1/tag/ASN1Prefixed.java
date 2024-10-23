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
package net.siisise.iso.asn1.tag;

import java.math.BigInteger;
import net.siisise.bind.format.TypeFormat;
import net.siisise.io.Input;
import net.siisise.iso.asn1.ASN1Cls;
import net.siisise.iso.asn1.ASN1StructMap;
import net.siisise.iso.asn1.ASN1Tag;
import net.siisise.iso.asn1.ASN1Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 拡張型.
 * BER / CER / DER 型で名前を持っていないための処置っぽいのでJSONなどでは省略してもよさそう。
 * [2] などのパターン
 * 0 cls
 * 1 tag
 * 2 data
 * cls tagは持ち
 * IMPLICIT / EXPLICIT
 * EXPLICIT の場合限定
 * tag を key としても重複して持ち
 * data を value として保持
 * @param <T>
 */
public class ASN1Prefixed<T extends ASN1Tag> extends ASN1StructMap<T> {
    
    /**
     * とりあえずEXPLICIT限定
     */
    final boolean implicit = false;
    T base;

    /**
     * EXPLICIT.
     * @param cls
     * @param tag 
     */
    public ASN1Prefixed(ASN1Cls cls, BigInteger tag) {
        super(cls, tag);
    }

    /**
     * 
     * @param cls ASN.1 class
     * @param tag tag 番号
     * @param asn 中身
     */
    public ASN1Prefixed(ASN1Cls cls, BigInteger tag, T asn) {
        super(cls, tag);
        put(tag.toString(), asn);
        base = asn;
    }

    /**
     * 名前をつけないと CONTEXT-SPECIFIC [1]
     * @param tag 
     */
    public ASN1Prefixed(BigInteger tag) {
        this(ASN1Cls.CONTEXT_SPECIFIC, tag);
    }

    /**
     * Context-Specific [tag] 構造.
     * @param tag 番号
     * @param asn 中身
     */
    public ASN1Prefixed(BigInteger tag, T asn) {
        this(ASN1Cls.CONTEXT_SPECIFIC, tag, asn);
    }

    public ASN1Prefixed(int tag) {
        this(BigInteger.valueOf(tag));
    }

    /**
     * CONTEXT_SPECIFIC
     * @param tag 番号
     * @param asn 中身
     */
    public ASN1Prefixed(int tag, T asn) {
        this(BigInteger.valueOf(tag), asn);
    }

    /**
     * とりあえずEXPLICIT専用なのでfalse
     * @return false
     */
    public boolean isImplicit() {
        return implicit;
    }

    /**
     * DER本体符号化.
     * @return
     */
    @Override
    public byte[] encodeBody() {
        if ( implicit ) { // 未対応
            throw new UnsupportedOperationException();
//            return base.encodeBody();
        } else {
            return base.encodeAll();
        }
    }

    /**
     * EXPLICIT
     * @param data
     * @param length 
     */
    @Override
    public void decodeBody(Input data, int length) {
        clear();
        if ( implicit ) { // 未対応
            byte[] d = new byte[length];
            data.read(d);
            base = (T)new OCTETSTRING(this.getASN1Cls(), getTag(), d);
        } else {
            base = (T)ASN1Util.toASN1(data);
        }
        put(tag.toString(),base);
    }

    /**
     * 変換.
     * @param <V> 出力型
     * @param format 変換器
     * @return 
     */
    @Override
    public <V> V rebind(TypeFormat<V> format) {
        return format.mapFormat(this);
    }

    @Override
    public Element encodeXML(Document doc) {
        ASN1Tag in = base;
        Element x;
        if ( implicit ) { // IMPLICIT
            x = doc.createElement("IMPLICIT");
        } else { // EXPLICIT
            x = doc.createElement("EXPLICIT");
        }
        x.setAttribute("class",getASN1Cls().name());
        x.setAttribute("tag", getTag().toString());
        x.appendChild(base.encodeXML(doc));
        return x;
    }

    @Override
    public void decodeXML(Element element) {
        clear();
        cls = ASN1Cls.valueOf(element.getAttribute("class"));
        tag = new BigInteger(element.getAttribute("tag"));
        base = (T)ASN1Util.toASN1(element);
        put(tag.toString(), base);
    }

}
