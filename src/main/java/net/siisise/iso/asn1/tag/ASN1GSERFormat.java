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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.TypeBind;
import net.siisise.bind.format.TypeFallFormat;
import net.siisise.lang.Binary16;

/**
 * RFC 3641
 * RFC 3642
 * 仮対応.
 */
public class ASN1GSERFormat extends TypeFallFormat<String> implements TypeBind<String> {

    @Override
    public String nullFormat() {
        return "NULL";
    }

    @Override
    public String booleanFormat(boolean bool) {
        return bool ? "TRUE" : "FALSE";
    }

    @Override
    public String numberFormat(Number num) {
        if (num instanceof Double || num instanceof Float || num instanceof Binary16) {
            Double d = num.doubleValue();
            if (Double.isInfinite(d)) {
                if ( d == Double.NEGATIVE_INFINITY ) {
                    return "MINUS-INFINITY";
                } else {
                    return "PLUS-INFINITY";
                }
            }
        } 
        return num.toString();
    }

    /**
     * ChoiceOfString.
     * 型情報は持たない
     * NumericString
     * PrintableString
     * TeletexString (T61String)
     * VideotexString
     * IA5String
     * GraphicString
     * VisibleString (ISO646String)
     * GeneralString
     * BMPString (UCS-2)
     * UniversalString (UCS-4)
     * UTF8String
     * 
     * @param str
     * @return 
     */
    @Override
    public String stringFormat(String str) {
        String cnv = str.replace("\"","\"\"");
        return "\"" + cnv + "\"";
    }

    /**
     * SEQUENCE.
     * @param map
     * @return 
     */
    @Override
    public String mapFormat(Map map) {
        StringBuilder out = new StringBuilder();
        out.append("{");
        Set es = map.entrySet();
        boolean l = false;
        for (Object k : map.keySet()) {
            if (l) {
                out.append(", ");
            } else {
                out.append(" ");
                l = true;
            }
            out.append(Rebind.valueOf(k, this));
            out.append(" ");
            Object v = map.get(k);
            out.append(Rebind.valueOf(v, this));
        }
        out.append(" }");
        return out.toString();
    }

    @Override
    public String collectionFormat(Collection col) {
        StringBuilder out = new StringBuilder();
        out.append("[");
        boolean l = false;
        for ( Object v : col ) {
            if (l) {
                out.append(", ");
            } else {
                out.append(" ");
                l = true;
            }
            out.append(Rebind.valueOf(v,this));
        }
        out.append(" ]");
        return out.toString();
    }

    
}
