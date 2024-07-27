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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.TypeFallFormat;
import net.siisise.io.BASE64;
import net.siisise.iso.asn1.ASN1;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 仮書式なので汎用には使えない.
 */
public class ASN1XMLFormat extends TypeFallFormat<Element> {
    
    Document doc;
    
    public ASN1XMLFormat(Document doc) {
        this.doc = doc;
    }

    @Override
    public Element nullFormat() {
        return doc.createElement( ASN1.NULL.name() );
    }

    /**
     * BOOLEAN
     * @param bool
     * @return 
     */
    @Override
    public Element booleanFormat(boolean bool) {
        Element tag = doc.createElement( ASN1.BOOLEAN.name() );
        tag.appendChild(doc.createTextNode("" + bool));
        return tag;
    }

    /**
     * INTEGER / REAL.
     * 振り分けは仮
     * @param num 数値
     * @return XML
     */
    @Override
    public Element numberFormat(Number num) {
        Element ele;
        if ( num instanceof Double || num instanceof Float || num instanceof BigDecimal ) {
            ele = doc.createElement(ASN1.REAL.name());
        } else {
            ele = doc.createElement(ASN1.INTEGER.name());
        }
        ele.setTextContent(num.toString());
        return ele;
    }

    /**
     * デフォルトUTF8String型
     * @param str 文字列
     * @return XML UTF8String
     */
    @Override
    public Element stringFormat(String str) {
        Element ele = doc.createElement("UTF8String");
        ele.setTextContent(str);
        return ele;
    }

    /**
     * 各種文字列型
     * @param seq 文字列
     * @return それっぽい文字列
     */
    @Override
    public Element stringFormat(CharSequence seq) {
        String eleName;
        if ( seq instanceof ASN1String) {
            ASN1String str = (ASN1String)seq;
            ASN1 asn = ASN1.valueOf(str.getTag().intValue());
            eleName = asn.name();
        } else {
            eleName  = "UTF8String";
        }
        Element ele = doc.createElement(eleName);
        ele.setTextContent(seq.toString());
        return ele;
    }

    /**
     * SEQUENCE / SEQUENCE OF
     * @param map
     * @return
     */
    @Override
    public Element mapFormat(Map map) {
        Element ele = doc.createElement("SEQUENCE");
        for ( Map.Entry<?,?> e : ((Map<?,?>)map).entrySet() ) {
            Element ent = Rebind.valueOf(e.getValue(), this);
            ent.setAttribute("name", (String)e.getKey());
            ele.appendChild(ent);
        }
        return ele;
    }

    /**
     * SET / SET OF
     * @param set これくしょん
     * @return XML
     */
    @Override
    public Element setFormat(Set set) {
        Element ele = doc.createElement("SET");
        for ( Object e : set) {
            Element ent = Rebind.valueOf(e, this);
            ele.appendChild(ent);
        }
        return ele;
    }

    /**
     * SEQUENCE / SEQUENCE OF / SET / SET OF.
     * なんとなく分ける
     * @param col 一覧
     * @return XML
     */
    @Override
    public Element collectionFormat(Collection col) {
        if ( col instanceof List ) {
            return listFormat((List)col);
        } else if ( col instanceof Set ) {
            return setFormat((Set)col);
        } else { // LinkedHashMapのvalues などはCollection
            return listFormat(new ArrayList(col));
        }
    }

    /**
     * OCTETSTRING
     * @param data
     * @return 
     */
    @Override
    public Element byteArrayFormat(byte[] data) {
        Element ele = doc.createElement( ASN1.OCTETSTRING.name() );
       // ele.setAttribute("ex", new String(data, StandardCharsets.UTF_8));
        BASE64 b64 = new BASE64();
        String val = b64.encode(data);
        ele.setTextContent(val);
        return ele;
    }
    
}
