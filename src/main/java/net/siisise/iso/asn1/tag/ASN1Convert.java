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
import java.net.URI;
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
import net.siisise.iso.asn1.ASN1Tag;

/**
 * ASN1Tagに変換する.
 * 比較的単純なものに対応する
 * 基本的なものは Universal class 内でなんとか?
 */
public class ASN1Convert implements TypeBind<ASN1Tag> {

    /**
     * ASN.1 の NULL に変換する
     * @return ASN.1 NULL Object
     */
    @Override
    public NULL nullFormat() {
        return new NULL();
    }

    /**
     * BERなどで出てくるかもしれない仮.
     * EndOfContent として扱うことにする (仮)
     * @return EndOfContent
     */
    @Override
    public EndOfContent undefinedFormat() {
        return new EndOfContent();
    }

    /**
     * boolean 型の変換.
     * 
     * @param bool 真偽値
     * @return ASN.1 BOOLEAN true または false
     */
    @Override
    public BOOLEAN booleanFormat(boolean bool) {
        return new BOOLEAN(bool);
    }

    /**
     * 整数型、浮動小数点型の変換.
     *
     * 整数をINTEGERに、実数をREALに.
     * 
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

    /**
     * 文字列の変換.
     * 現状標準的なUTF8Stringに変換する (仮)
     * @param str 文字列
     * @return ASN.1 UTF8String
     */
    @Override
    public ASN1String stringFormat(String str) {
        return new ASN1String(ASN1.UTF8String, str);
    }

    /**
     * 特殊な文字列風の変換.
     * 文字列の特殊タイプはCharSequenceを持つ何かとして変換する
     * 特定の型がある場合はあらかじめ指定してもよい.
     * @param sequence 文字シーケンス全般
     * @return ASN.1 文字列型のいずれか
     */
    @Override
    public ASN1String stringFormat(CharSequence sequence) {
        if ( sequence instanceof ASN1String ){
            return new ASN1String(ASN1.valueOf(((ASN1String)sequence).getId()), ((ASN1String) sequence).getValue());
        }
        
        return stringFormat( sequence.toString());
    }

    /**
     * SEQUENCE 変換.
     * 名前を持つ要素なので SEQUENCEMap型として名前を持ったまま変換しておく.
     * 
     * Class などでは名前を捨てて順序だけ使う.
     * @param map Mapっぽい要素
     * @return  ASN.1 SEQUENCE (Map)型
     */
    @Override
    public SEQUENCE mapFormat(Map map) {
        SEQUENCEMap seq = new SEQUENCEMap();
        for ( Map.Entry e : ((Map<?,?>)map).entrySet() ) {
            seq.put((String)e.getKey(), (ASN1Tag)Rebind.valueOf(e.getValue(),this));
        }
        
        return seq;//collectionFormat(map.values());
    }

    /**
     * バイナリ列 OCTETSTRING変換.
     * 
     * OCTETSTRINGと対応する.
     * 
     * @param bytes バイト列
     * @return ASN.1 OCTETSTRING
     */
    @Override
    public OCTETSTRING byteArrayFormat(byte[] bytes) {
        return new OCTETSTRING(bytes);
    }
    
    /**
     * SET / SET OF 変換.
     * DER型ではソート対象になる.
     * 
     * @param col 集合
     * @return ASN.1 SET / SET OFタイプに対応する SEQUENCEList
     */
    @Override
    public SEQUENCEList setFormat(Set col) {
        SEQUENCEList set = SEQUENCEList.SET();
        for (Object v : col ) {
            ASN1Tag o = Rebind.valueOf(v, this);
            set.add(o);
        }
        List list = set.getValue();
        // 要 ソート
        Collections.sort(list);
        return set;
    }

    /**
     * SEQUENCE / SEQUENCE OF 変換.
     * @param col List集合
     * @return ASN.1 SEQUENCE / SEQUENCE OF に対応する SEQUENSEList
     */
    @Override
    public SEQUENCEList listFormat(List col) {
        SEQUENCEList seq = new SEQUENCEList();
        for (Object v : col ) {
            ASN1Tag o = Rebind.valueOf(v, this);
            seq.add(o);
        }
        return seq;
    }
    
    /**
     * SEQUENCE / SEQUENCE OF / SET / SET OF 変換.
     * List と Set の場合、SEQUENCE系とSET系に振り分ける.
     * その他の場合はSEQUENCE系に振り分ける.
     * @param col Collection全般
     * @return ASN.1 SEQUENCEList系要素
     */
    @Override
    public SEQUENCEList collectionFormat(Collection col) {
        if ( col instanceof List ) {
            return listFormat((List)col);
        } else if ( col instanceof Set ) {
            return setFormat((Set)col);
        } else { // LinkedHashMapのvalues などはCollection
            return listFormat(new ArrayList(col));
        }
    }

    /**
     * 特殊URN OBJECTIDENTIFIER 変換.
     * urn:oid: を OBJECTIDENTIFIER に変換する。
     * その他はURI文字列としてASN1 UTF8String に変換する。(仮)
     * @param uri
     * @return 
     */
    @Override
    public ASN1Object uriFormat(URI uri) {
        String scheme = uri.getScheme();
        if ("urn".equals(scheme)) {
            String u = uri.toString();
            if ( u.startsWith("urn:oid:")) {
                return new OBJECTIDENTIFIER(u.substring(8));
            }
        }
        return stringFormat(uri.toString());
    }
/*
    @Override
    public ASN1Object objectFormat(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/    
}
