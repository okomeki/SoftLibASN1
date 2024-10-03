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

import java.math.BigInteger;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Cls;
import net.siisise.iso.asn1.ASN1StructMap;
import net.siisise.iso.asn1.ASN1Tag;

/**
 * 名前付き SEQUENCE
 *
 * @param <T> 特定のASN.1型
 */
public class SEQUENCEMap<T extends ASN1Tag> extends ASN1StructMap<T> implements SEQUENCE<T> {

    public SEQUENCEMap(ASN1Cls cls, BigInteger tag) {
        super(cls, tag);
    }

    public SEQUENCEMap(ASN1Cls cls, int tag) {
        super(cls, tag);
    }

    public SEQUENCEMap() {
        super(ASN1.SEQUENCE);
    }

    public void put(String key, BigInteger val) {
        put(key, (T) new INTEGER(val));
    }

    public void put(String key, long val) {
        put(key, (T) new INTEGER(val));
    }
}
