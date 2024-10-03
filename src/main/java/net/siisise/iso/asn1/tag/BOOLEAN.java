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

import java.math.BigInteger;
import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Cls;
import net.siisise.iso.asn1.ASN1Object;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * X.680 3.8.8 boolean type.
 * 8.2 01 
 */
public class BOOLEAN extends ASN1Object<Boolean> {

    private boolean val;

    public BOOLEAN() {
        super(ASN1.BOOLEAN);
    }

    public BOOLEAN(boolean b) {
        val = b;
    }

    /**
     * IMPLICIT用
     * @param cls asn1class
     * @param tag asn1tag
     * @param b boolean value
     */
    public BOOLEAN(ASN1Cls cls, BigInteger tag, boolean b) {
        super(cls, tag);
        val = b;
    }

    /**
     * バイト列デコード.
     * @param data 
     */
    @Override
    public void decodeBody( byte[] data ) {
        if ( data.length != 1) {
            throw new IllegalStateException();
        }
        val = data[0] != 0;
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element bool = doc.createElement( ASN1.BOOLEAN.name() );
        bool.appendChild(doc.createTextNode("" + val));
        return bool;
    }

    @Override
    public <T> T rebind(TypeFormat<T> f) {
        return f.booleanFormat(val);
    }

    @Override
    public void decodeXML( Element element ) {
        val = Boolean.parseBoolean(element.getTextContent());
    }

    @Override
    public String toString() {
        return Boolean.toString(val);
    }

    /**
     * 値の取得.
     * Boolean class
     * @return 
     */
    @Override
    public Boolean getValue() {
        return val;
    }

    @Override
    public void setValue( Boolean v ) {
        val = v;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && ((BOOLEAN)o).val == val;
    }
}
