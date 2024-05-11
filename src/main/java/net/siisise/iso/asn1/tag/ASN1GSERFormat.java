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
import net.siisise.bind.format.TypeBind;
import net.siisise.bind.format.TypeFallFormat;

/**
 * RFC 3641
 * RFC 3642
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String stringFormat(String str) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String mapFormat(Map map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String collectionFormat(Collection col) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
