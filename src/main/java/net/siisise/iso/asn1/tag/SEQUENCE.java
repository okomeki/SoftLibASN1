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

import java.util.HashSet;
import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Struct;
import net.siisise.iso.asn1.ASN1Tag;

/**
 * SEQUENCE / SEQUENCE OF / SET / SET OF
 * SEQUENCE OF / SET OF は単一集合型 同じ型の要素の集合 区別はなさそう
 */
public class SEQUENCE extends ASN1Struct implements ASN1Tag {

    public SEQUENCE() {
        super(ASN1.SEQUENCE);
    }

    // 仮
    public static SEQUENCE SET() {
        return new SEQUENCE(ASN1.SET);
    }

    public SEQUENCE( ASN1 id ) {
        super(id);
    }

    @Override
    public <V> V rebind(TypeFormat<V> format) {
        if ( this.getTag().equals(ASN1.SEQUENCE.tag) ) {
            return format.listFormat(getValue());
        } else if ( getTag().equals(ASN1.SET.tag)){
            return format.setFormat(new HashSet(getValue()));
        }
        throw new UnsupportedOperationException();
    }
    
}
