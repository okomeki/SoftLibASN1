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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.TypeFallFormat;
import net.siisise.io.BASE64;
import net.siisise.io.BitPacket;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Tag;
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
        if ( seq instanceof ASN1String ) {
            return toElement((ASN1String)seq);
        } else {
            return stringFormat(seq.toString());
        }
    }
    
    @Override
    public Element uriFormat(URI uri) {
        String scheme = uri.getScheme();
        if ("urn".equals(scheme)) {
            String u = uri.toString();
            if ( u.startsWith("urn:oid:")) {
                return oidFormat(u.substring(8));
            }
        }
        return stringFormat(uri.toString());
    }
    
    private Element oidFormat(String oid) {
        ASN1Tag tag = new OBJECTIDENTIFIER(oid);
        return toElement(tag);
    }

    private Element toElement(ASN1Tag tag) {
        ASN1 asn = ASN1.valueOf(tag.getTag().intValue());
        String eleName = asn.name();
        Element ele = doc.createElement(eleName);
        ele.setTextContent(tag.getValue().toString());
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

    /**
     * BITSTRING
     * @param bits
     * @return 
     */
    @Override
    public Element bitArrayFormat(BitPacket bits) {
        Element ele = doc.createElement(ASN1.BITSTRING.name());
        long bitlen = bits.bitLength();
        int byteLen = (int)(bitlen / 8); // フルで埋まるバイト
        byte[] data = new byte[(int)(bitlen + 7 / 8)];
        bits.read(data, 0, byteLen);
        
        bits.readBit(data, 0, bitlen);
        int b = (int)bitlen % 8;
        if ( b != 0 ) {
            
        }
        ele.setAttribute("bitlen", String.valueOf(bitlen));
        BASE64 b64 = new BASE64();
        String val = b64.encode(data);
        ele.setTextContent(val);
        return ele;
        
    }
    
}
