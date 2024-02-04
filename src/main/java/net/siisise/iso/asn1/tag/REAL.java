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
import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * REAL 浮動小数点
 */
public class REAL extends ASN1Object<BigDecimal> implements ASN1Tag {
    private BigDecimal val;
    
    public REAL() {
        super( ASN1.REAL);
    }
    
    public REAL(BigDecimal v) {
        val = v;
    }

    @Override
    public BigDecimal getValue() {
        return val;
    }

    @Override
    public void setValue(BigDecimal val) {
        this.val = val;
    }

    @Override
    public byte[] encodeBody() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element encodeXML(Document doc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <V> V encode(TypeFormat<V> format) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void decodeXML(Element element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
