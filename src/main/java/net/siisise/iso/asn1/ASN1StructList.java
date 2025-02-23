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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.siisise.bind.format.TypeFormat;
import net.siisise.io.Input;
import net.siisise.iso.asn1.tag.NULL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ASN1 の構造系.
 * SEQUENCE / SEQUENCE OF / SET / SET OF /
 * SEQUENCE OF / SET / SET OF に適しているが SEQUENCE の省略形としても使う
 * BER, CERでは文字列，OCTETSTRING などにも
 * Context-specific [0] [APPLICATION 0] [PRIVATE 0]
 * Rebind で判定できなさそうなのでListベースで作り直し.
 * @param <T> ASN.1型を限定する
 */
public class ASN1StructList<T extends ASN1Tag> extends ArrayList<T> implements ASN1Struct<T> {

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

    protected ASN1StructList(ASN1Cls cls, BigInteger tag) {
        this.cls = cls;
        this.tag = tag;
    }
    
    protected ASN1StructList(ASN1Cls cls, int tag) {
        this(cls, BigInteger.valueOf(tag));
    }

    protected ASN1StructList(ASN1 tag) {
        this(ASN1Cls.UNIVERSAL, tag.tag);
    }

    @Override
    public ASN1Cls getASN1Cls() {
        return cls;
    }

    /**
     * ASN.1 class情報
     * 0: UNIVERSAL 汎用 1:APPLICATION 応用 2:Context-Specific コンテキスト特定 3:PRIVATE 私用
     * @return 0: UNIVERSAL 汎用 1:APPLICATION 応用 2:Context-Specific コンテキスト特定 3:PRIVATE 私用
     */
    @Override
    public int getASN1Class() {
        return cls.cls;
    }
    
    @Override
    public void setTag(ASN1Cls c, int tag) {
        cls = c;
        this.tag = BigInteger.valueOf(tag);
    }

    /**
     * ASN.1 structed flag
     * @return 0:false: Primitive 1:true:Constructed
     */
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

    /**
     * 特定位置のものを取得する
     *
     * @param offsets
     * @return 対象オブジェクト
     */
    @Override
    public ASN1Tag get(int... offsets) {
        ASN1Tag obj = this;

        for (int i = 0; i < offsets.length; i++) {
            if (obj instanceof ASN1Struct) {
                obj = (ASN1Tag) ((ASN1Struct)obj).get(offsets[i]);
            } else {
                throw new NullPointerException();
                //                    return null;
            }
        }
        return obj;
    }
    
    /**
     * 末尾に追加する.
     * 
     * @param tag tag
     */
    @Override
    public boolean add(T tag) {
        if ( tag == null ) {
            tag = (T)new NULL();
        }
        return super.add(tag);
    }

    /**
     * 同タグでn番目 (UNIVERSAL 限定?)
     *
     * @param tag tag
     * @param index 位置
     * @return 対象オブジェクト
     * @see #tagSize( BigInteger )
     */
    @Override
    public T get(BigInteger tag, int index) {
        for (int n = 0; n < size(); n++) {
            if (get(n).getTag().equals(tag)) {
                index--;
                if (index < 0) {
                    return get(n);
                }
            }
        }
        return null;
    }

    
    @Override
    public ASN1Tag getContextSpecific(String name, int tag, ASN1 universal) {
        return getContextSpecific(tag, universal);
    }

    /**
     * オブジェクトを奥深くに追加/更新する.
     * @param obj 要素
     * @param index 巧妙な位置
     */
    @Override
    public void set(ASN1Tag obj, int... index) {
        if (index.length > 1) {
            int[] idx = new int[index.length - 1];
            System.arraycopy(index, 1, idx, 0, idx.length);
            ((ASN1Struct)get(index[0])).set(obj, idx);
        } else {
            set(index[0], (T)obj);
        }
    }

    /**
     * オブジェクトを奥深くに追加する.
     * @param obj 要素
     * @param index 巧妙な位置
     */
    @Override
    public void add(ASN1Tag obj, int... index) {
        if (index.length > 1) {
            int[] idx = new int[index.length - 1];
            System.arraycopy(index, 1, idx, 0, idx.length);
            ((ASN1Struct)get(index[0])).add(obj, idx);
        } else {
            add(index[0], (T)obj);
        }
    }


    /**
     * タグ限定サイズ
     *
     * @param tag 特定のタグ
     * @return たぐの含まれる個数
     */
    public int tagSize(BigInteger tag) {
        int count = 0;
        for (ASN1Tag obj : this) {
            if (obj.getTag().equals(tag)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 内容をListとして返す.
     * @return 内容を複製しない新規List
     */
    @Override
    public List<T> getValue() {
        return new ArrayList<>(this);
    }
    
    /**
     * 一括更新.
     * @param list 新しい内容
     */
    @Override
    public void setValue(List<T> list) {
        this.clear();
        this.addAll(list);
    }

    /**
     * バイト列デコード.
     * @param in source
     * @param length
     */
    @Override
    public void decodeBody(Input in, int length) {
        clear();
        if (length >= 0) {
            Input data = in.readPacket(length);
            decodeBody(data);
        } else {
            inefinite = true;
            decodeEOFList(in);
        }
    }

    void decodeBody(Input in) {
        while (in.size() > 0) {
            ASN1Tag o = ASN1Util.toASN1(in);
            add(o);
        }
    }

    /**
     * 長さ不定 decode.
     * @param in 
     */
    void decodeEOFList(Input in) {
        while (in.size() > 0) {
            ASN1Tag o = ASN1Decoder.toASN1(in);
            if (o.getASN1Class() == 0) {
                break;
            }
            add(o);
        }
    }

    /**
     * 比較.
     * class, tag, 内容で比較する
     * @param o 対象
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
        return ASN1Object.compare(encodeAll(), o.encodeAll());
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof ASN1Tag) {
            return compareTo((ASN1Tag)obj) == 0;
        }
        return false;
    }

    /**
     * List または Set として出力する.
     * @param <E> 出力型
     * @param format 出力形式
     * @return 出力
     */
    @Override
    public <E> E rebind(TypeFormat<E> format) {
        if ( getASN1Cls() == ASN1Cls.UNIVERSAL && getTag().equals(ASN1.SET.tag) ) {
            format.setFormat(new HashSet(this));
        }
        return format.listFormat(this);
    }

    @Override
    public Element encodeXML(Document doc) {
        Element xml;
        if ( cls == ASN1Cls.UNIVERSAL && tag.intValue() < ASN1.values().length ) {
            ASN1 n2 = ASN1.valueOf(tag.intValue());
            xml = doc.createElement(n2.name());
        } else {
            xml = doc.createElement("struct");
            if ( cls != ASN1Cls.UNIVERSAL ) {
                xml.setAttribute("class", ""+Integer.toString(cls.cls));
            }
            xml.setAttribute("tag", tag.toString());
        }
        if ( inefinite ) {
            xml.setAttribute("inefinite", "true");
        }
        if (getId() == ASN1.SET.tag.intValue()) {
            Collections.sort(this);
        }
        for ( ASN1Tag obj : this ) {
            if ( obj == null ) {
                obj = new NULL();
            }
            xml.appendChild(obj.encodeXML(doc));
        }
        return xml;
    }

    @Override
    public void decodeXML(Element xml) {
        String inf = xml.getAttribute("inefinite");

        if (inf != null && Boolean.parseBoolean(inf)) {
            inefinite = true;
        }
        NodeList child = xml.getChildNodes();
        for (int i = 0; i < child.getLength(); i++) {
            Node n = child.item(i);
            if (n instanceof Element) {
                ASN1Tag o = ASN1Util.toASN1((Element) n);
                add(o);
            }
        }
    }
}
