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
package net.siisise.iso.asn1.tag;

import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * X.680 3.8.51 null
 * 8.1.5 00 EOC
 * 05 NULL
 */
public class NULL extends ASN1Object implements ASN1Tag {

    public NULL() {
        super(ASN1.NULL);
    }

    public NULL( ASN1 id ) {
        super( id );
    }

    /**
     * 本体なし.
     * @return DER NULL BODY
     */
    @Override
    public byte[] encodeBody() {
        return new byte[0];
    }

    /**
     * 本体なし.
     * @param data 
     */
    @Override
    public void decodeBody(byte[] data) {
        if ( data.length != 0 ) {
            throw new java.lang.IllegalStateException();
        }
    }

    @Override
    public Element encodeXML(Document doc) {
        return doc.createElement( ASN1.valueOf(this.getId()).name() );
    }
    
    @Override
    public void decodeXML(Element element) {
    }
    
    @Override
    public String toString() {
        return ASN1.valueOf(this.getId()).name();
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue( Object val ) {
    }

    @Override
    public Object encode(TypeFormat format) {
        return format.nullFormat();
    }
    
}
