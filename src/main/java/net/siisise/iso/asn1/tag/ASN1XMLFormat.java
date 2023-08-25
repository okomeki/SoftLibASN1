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

import java.util.Collection;
import java.util.Map;
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
     * INTEGER
     * @param num
     * @return 
     */
    @Override
    public Element numberFormat(Number num) {
        Element ele = doc.createElement( ASN1.INTEGER.name() );
        ele.setTextContent(num.toString());
        return ele;
    }

    @Override
    public Element stringFormat(String str) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element mapFormat(Map map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element collectionFormat(Collection col) {
        throw new UnsupportedOperationException("Not supported yet.");
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
