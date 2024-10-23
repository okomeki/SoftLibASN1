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
package net.siisise.iso.asn1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.siisise.bind.format.TypeFormat;
import net.siisise.io.Input;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 名前付きで保存できる本来っぽいASN.1 Struct 形式.
 * SEQUENCE などの想定.
 * 基本的にList系と互換性をとりつつMapっぽくする
 * @param <T> ASN.1型を限定する
 */
public class ASN1StructMap<T extends ASN1Tag> extends LinkedHashMap<String,T> implements ASN1Struct<T> {
    
    protected ASN1Cls cls;
    protected BigInteger tag;

    /**
     * 構造型
     */
    protected boolean constructed = true;

    /**
     * 可変長形式
     */
    protected boolean inefinite = false;

    public ASN1StructMap(ASN1Cls cls, BigInteger tag) {
        this.cls = cls;
        this.tag = tag;
    }
    
    public ASN1StructMap(ASN1Cls cls, int tag) {
        this(cls, BigInteger.valueOf(tag));
    }
    
    public ASN1StructMap(ASN1 asn) {
        cls = ASN1Cls.UNIVERSAL;
        tag = asn.tag;
    }

    @Override
    public int getASN1Class() {
        return cls.cls;
    }

    @Override
    public ASN1Cls getASN1Cls() {
        return cls;
    }

    @Override
    public void setTag(ASN1Cls c, int tag) {
        cls = c;
        this.tag = BigInteger.valueOf(tag);
    }

    @Override
    public boolean isConstructed() {
        return constructed;
    }

    /**
     * 可変長形式.
     * @return true 長さを持たない構造
     */
    @Override
    public boolean isInefinite() {
        return inefinite;
    }

    @Override
    public void setInefinite(boolean inefinite) {
        this.inefinite = inefinite;
    }

    @Override
    public BigInteger getTag() {
        return tag;
    }

    @Override
    public int getId() {
        return tag.intValue();
    }

    @Override
    public T get(int offset) {
        return (T)values().toArray()[offset];
    }
    
    /**
     * 特定位置のものを取得する
     *
     * @param offsets 多段位置
     * @return 対象オブジェクト
     */
    @Override
    public ASN1Tag get(int... offsets) {
        ASN1Tag obj = this;

        for (int i = 0; i < offsets.length; i++) {
            if (obj instanceof ASN1Struct) {
                obj = (ASN1Tag) ((ASN1Struct) obj).get(offsets[i]);
            } else {
                throw new NullPointerException();
                //                    return null;
            }
        }
        return obj;
    }

    public T get(String key) {
        return super.get(key);
    }

    /**
     * IMPLICITの型変換つきで取得するなにか.
     * 
     * @param name module上の名前 (BER/CER/DER以外の参照方法)
     * @param tag Context-Specific tag (BER/CER/DERの参照方法)
     * @param universal IMPLICITで省略されている型情報
     * @return 対象
     */
    @Override
    public ASN1Tag getContextSpecific(String name, int tag, ASN1 universal) {
        ASN1Tag val = getContextSpecific(tag, universal);
        if (val == null) {
            return convert(get(name), universal);
        }
        return val;
    }

    @Override
    public List<T> getValue() {
        return new ArrayList<>(values());
    }

    @Override
    public void setValue(List<T> val) {
        clear();
        int i = 0;
        for ( T a : val ) {
            this.put(""+i++, a);
        }
    }

    /**
     * rebindに割り振り.
     * 名前を持つMap構造なのでMapとして扱う.
     * @param <V> formatが持つ出力型
     * @param format 出力 format
     * @return 出力
     */
    @Override
    public <V> V rebind(TypeFormat<V> format) {
        return format.mapFormat(this);
    }

    @Override
    public void decodeBody(Input in, int length) {
        in.readPacket(length);
        ASN1Util.toASN1(in);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element encodeXML(Document doc) {
        Element xml = doc.createElement(ASN1.valueOf(tag.intValue()).name());
        for ( Map.Entry<String,T> e : this.entrySet() ) {
            Element child = e.getValue().encodeXML(doc);
            child.setAttribute("name", e.getKey());
            xml.appendChild(child);
        }
        return xml;
    }

    @Override
    public void decodeXML(Element ele) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo( ASN1Tag o ) {
        if ( getASN1Class() != o.getASN1Class() ) {
            return getASN1Class() - o.getASN1Class();
        }
        if ( getId() != o.getId() ) {
            return getId() - o.getId();
        }
//        if ( inefinite != o.inefinite) {
//            return ( inefinite ? 1 : 0 ) - ( o.inefinite ? 1 : 0 );
//        }
        return Arrays.compare(encodeAll(), o.encodeAll());
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof ASN1Tag) {
            return compareTo((T)obj) == 0;
        }
        return false;
    }

    @Override
    public T get(BigInteger tag, int index) {
        return (T)values().stream().filter(v -> v.getTag().equals(tag)).toArray()[index];
    }

    @Override
    public void set(ASN1Tag obj, int... index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(ASN1Tag obj, int... index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 仮
     * @param obj
     * @return 
     */
    @Override
    public boolean add(T obj) {
        put(""+size(),obj);
        return true;
    }
}
