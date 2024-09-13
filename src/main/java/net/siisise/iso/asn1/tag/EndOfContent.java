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

import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 00 構造の終端.
 * 閉じ括弧相当.
 * BER用なので残らない方がいい.
 */
public class EndOfContent extends ASN1Object implements ASN1Tag {

    public EndOfContent() {
        
    }

    @Override
    public Object getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setValue(Object val) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
/*
    @Override
    public byte[] encodeBody() {
        return new byte[0];
    }
*/
    @Override
    public Element encodeXML(Document doc) {
        return doc.createElement("EndOfContent");
    }

    @Override
    public Object rebind(TypeFormat format) {
        return format.undefinedFormat();
    }

    @Override
    public void decodeXML(Element element) {
    }
    
}
