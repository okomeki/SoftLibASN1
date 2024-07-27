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
import java.util.ArrayList;
import java.util.List;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1Cls;
import net.siisise.iso.asn1.ASN1Object;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 拡張型.
 * [2] などのパターンでSEQUENCE ではない単体のもの
 * IMPLICIT を想定
 * @param <V>
 */
public class ASN1Prefixed<V> extends ASN1Object<V> {

    ASN1Object base; // 仮 または ASN1型
    // 型未定義の場合
    V data;

    public ASN1Prefixed(ASN1Cls cls, BigInteger tag) {
        super(cls, tag);
    }

    public ASN1Prefixed(ASN1Cls cls, BigInteger tag, V data) {
        super(cls, tag);
        this.data = data;
    }

    public ASN1Prefixed(ASN1Cls cls, BigInteger tag, ASN1Object asn) {
        super(cls, tag);
        this.base = asn;
    }

    @Override
    public V getValue() {
        return data;
    }

    @Override
    public void setValue(V val) {
        data = val;
    }

    /**
     *
     * @return
     */
    @Override
    public byte[] encodeBody() {
        if (base != null) {
            return base.encodeBody();
        }
        return (byte[]) data;
    }

    @Override
    public void decodeBody(byte[] data) {
        if (base != null) {
            base.decodeBody(data);
        } else {
            this.data = (V) data;
        }
    }

    /**
     * 変換.
     * @param <V> 出力型
     * @param format 変換器
     * @return 
     */
    @Override
    public <V> V rebind(TypeFormat<V> format) {
        List seq = new ArrayList();
        seq.add(base);
        return Rebind.valueOf(seq, format);
    }

    @Override
    public Element encodeXML(Document doc) {
        if ( base != null) {
            Element ele = base.encodeXML(doc);
            ASN1Cls cls = getASN1Cls();
            String name;
            if ( cls != ASN1Cls.CONTEXT_SPECIFIC ) {
                name = cls.name() + " " + base.getId();
            } else {
                name = "" + base.getId();
            }
            ele.setAttribute("IMPLICIT", name);
            return base.encodeXML(doc);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void decodeXML(Element element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
