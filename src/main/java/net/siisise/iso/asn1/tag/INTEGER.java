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
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 際限なく符号付き整数
 * JavaではBigIntegerに相当する
 * DER 頭の9ビットが連続しないこと
 */
public class INTEGER extends ASN1Object<BigInteger> {
    private BigInteger val;
    
    public INTEGER() {
        super( ASN1.INTEGER );
    }
    
    public INTEGER(BigInteger v) {
        super( ASN1.INTEGER);
        val = v;
    }

    public INTEGER(long v) {
        super( ASN1.INTEGER);
        val = BigInteger.valueOf(v);
    }

    @Override
    public byte[] encodeBody() {
        return val.toByteArray();
    }

    @Override
    public void decodeBody(byte[] data) {
        val = new BigInteger( data );
    }

    @Override
    public Element encodeXML(Document doc) {
        Element ele = doc.createElement( ASN1.INTEGER.name() );
        ele.setTextContent(val.toString());
        return ele;
    }

    /**
     * 符号化.
     * @param <T> 出力型
     * @param format 書式
     * @return 変換出力
     */
    @Override
    public <T> T rebind(TypeFormat<T> format) {
        return format.numberFormat(val);
    }

    @Override
    public void decodeXML(Element ele) {
        String txt = ele.getTextContent();
        val = new BigInteger(txt);
    }

    @Override
    public String toString() {
        return "INTEGER " + val.toString();
    }
    
    @Override
    public BigInteger getValue() {
        return val;
    }
    
    public int intValue() {
        return val.intValue();
    }
    
    public long longValue() {
        return val.longValue();
    }
    
    @Override
    public void setValue( BigInteger v ) {
        val = v;
    }
    
    @Override
    public boolean equals(Object o) {
        if ( o == null || !(o instanceof INTEGER) ) {
            return false;
        }
        return ((INTEGER)o).getValue().equals(val);
    }
    
    @Override
    public int compareTo(ASN1Tag o) {
        int i = super.compareTo(o);
        if ( i == 0 ) {
            return val.compareTo(((INTEGER)o).getValue());
        }
        return i;
    }
}
