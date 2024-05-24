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

import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * X.690 202102
 * 8.5 REAL 実数値.
 * 浮動小数点
 * 2進数 / 16進数 / 10進数
 *
 * @param <T>
 */
public abstract class REAL<T extends Number> extends ASN1Object<T> implements ASN1Tag {

    static final byte PLUS_INFINITY = 0x40;
    static final byte MINUS_INFINITY = 0x41;
    static final byte NaN = 0x42;
    static final byte MINUS_ZERO = 0x43;

    protected T val;

    public REAL() {
        super(ASN1.REAL);
    }

    public REAL(T v) {
        super(ASN1.REAL);
        val = v;
    }

    @Override
    public T getValue() {
        return val;
    }

    @Override
    public void setValue(T val) {
        this.val = val;
    }

    @Override
    public Element encodeXML(Document doc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <V> V encode(TypeFormat<V> format) {
        return format.numberFormat(val);
    }

    @Override
    public void decodeXML(Element element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
