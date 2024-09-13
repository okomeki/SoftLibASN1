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
import java.util.HashSet;
import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Cls;
import net.siisise.iso.asn1.ASN1StructList;

/**
 * 名前なし SEQUENCE / SEQUENCE OF / SET / SET OF.
 * 
 */
public class SEQUENCEList extends ASN1StructList implements SEQUENCE {

    public SEQUENCEList(ASN1Cls cls, BigInteger tag) {
        super(cls, tag);
    }

    /**
     * IMPLICIT / EXPLICIT な構造.
     * @param cls ASN.1 class
     * @param tag ASN.1 tag
     */
    public SEQUENCEList(ASN1Cls cls, int tag) {
        super(cls, tag);
    }

    /**
     * 名前のないSEQUENCE / SEQUENCE OF.
     */
    public SEQUENCEList() {
        super(ASN1.SEQUENCE);
    }

    /**
     * SET / SET OF として構築、
     * ソートあるといいな。
     * @return SET / SET OF っぽい SEQUENCE List
     */
    public static SEQUENCEList SET() {
        return new SEQUENCEList(ASN1Cls.UNIVERSAL, ASN1.SET.tag);
    }

    /**
     * rebind変換。
     * 型に対応する list または set として出力する。
     * @param <V> formatで指定する出力型
     * @param format 出力書式
     * @return 適度に変換された出力
     */
    @Override
    public <V> V rebind(TypeFormat<V> format) {
        if ( getTag().equals(ASN1.SEQUENCE.tag) ) {
            return format.listFormat(this);
        } else if ( getTag().equals(ASN1.SET.tag)){
            return format.setFormat(new HashSet(this));
        }
        throw new UnsupportedOperationException();
    }
}
