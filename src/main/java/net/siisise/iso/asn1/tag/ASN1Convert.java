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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.TypeBind;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Struct;

/**
 * 比較的簡単なものに対応する
 */
public class ASN1Convert implements TypeBind<ASN1Object> {

    @Override
    public NULL nullFormat() {
        return new NULL();
    }

    @Override
    public BOOLEAN booleanFormat(boolean bool) {
        return new BOOLEAN(bool);
    }

    /**
     * 整数をINTEGERに、実数をREALに.
     * @param num Java型
     * @return ASN1型 INTEGER または REAL
     */
    @Override
    public ASN1Object numberFormat(Number num) {
        if ( num instanceof Integer || num instanceof Short || num instanceof Byte ) {
            num = num.longValue();
        }
        if ( num instanceof Long ) {
            num = BigInteger.valueOf((long)num);
        }
        if ( num instanceof BigInteger ) {
            return new INTEGER((BigInteger)num);
        }
        if ( num instanceof Double || num instanceof Float) {
            return new REAL(num.doubleValue());
        }
        if ( num instanceof BigDecimal ) {
            return new REAL((BigDecimal)num);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ASN1String stringFormat(String str) {
        return new ASN1String(ASN1.UTF8String, str);
    }

    /**
     * SEQUENCE ?
     * Class などでは名前を捨てて順序だけ使う.
     * LinkedHashMap ならlistFormat にできるかな
     * @param map
     * @return 
     */
    @Override
    public ASN1Struct mapFormat(Map map) {
        return collectionFormat(map.values());
    }

    /**
     * OCTETSTRING
     * @param bytes
     * @return 
     */
    @Override
    public ASN1Object byteArrayFormat(byte[] bytes) {
        return new OCTETSTRING(bytes);
    }
    
    /**
     * SET / SET OF
     * @param col
     * @return 
     */
    @Override
    public ASN1Struct setFormat(Set col) {
        SEQUENCE set = SEQUENCE.SET();
        for (Object v : col ) {
            ASN1Object o = Rebind.valueOf(v, this);
            set.add(o);
        }
        List list = set.getValue();
        // 要 ソート
        Collections.sort(list);
        return set;
    }

    /**
     * SEQUENCE / SEQUENCE OF
     * @param col
     * @return 
     */
    @Override
    public ASN1Struct listFormat(List col) {
        SEQUENCE seq = new SEQUENCE();
        for (Object v : col ) {
            ASN1Object o = Rebind.valueOf(v, this);
            seq.add(o);
        }
        return seq;
    }
    
    /**
     * SEQUENCE / SEQUENCE OF / SET / SET OF
     * @param col
     * @return 
     */
    @Override
    public ASN1Struct collectionFormat(Collection col) {
        if ( col instanceof List ) {
            return listFormat((List)col);
        } else if ( col instanceof Set ) {
            return setFormat((Set)col);
        } else { // LinkedHashMapのvalues などはCollection
            return listFormat(new ArrayList(col));
        }
    }
/*
    @Override
    public ASN1Object objectFormat(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/    
}
