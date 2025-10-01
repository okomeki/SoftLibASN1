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
package net.siisise.iso.asn1;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import net.siisise.iso.asn1.tag.ASN1DERFormat;
import net.siisise.iso.asn1.tag.INTEGER;

/**
 * SEQUENCE、SETと標準以外の構造
 *
 * @param <V> ASN.1型を限定する
 */
public interface ASN1Struct<V extends ASN1Tag> extends ASN1Tag<List<V>> {

    /**
     * 可変長形式.
     * @return true 長さを持たない構造
     */
    public boolean isInefinite();
    public void setInefinite(boolean inefinite);

    public int size();

    public V get(int offset);
    
    /**
     * 特定位置のものを取得する
     *
     * @param offsets
     * @return 対象オブジェクト
     */
    public ASN1Tag get(int... offsets);

    /**
     * キャストが便利なだけ
     *
     * @param index
     * @return 対象構造
     */
    public default ASN1Struct getStruct(int... index) {
        return (ASN1Struct) get(index);
    }

    /**
     * UNIVERSAL 限定 要素の取得.
     * 同じタグで n番目.
     * @param tag タグ
     * @param index 同タグ中の位置
     * @return 
     */
    public V get(BigInteger tag, int index);

    /**
     * 要素の取得.
     * @param name 名
     * @param index 全体の位置
     * @return 値
     */
    public V get(String name, int index);
    
    /**
     * 元要素の型で返る
     * @param c Universal以外の型
     * @param tag タグ番号
     * @return 謎型
     */
    public default ASN1Tag get(ASN1Cls c, BigInteger tag) {
        int size = size();
        for (int i = 0; i < size; i++ ) {
            ASN1Tag t = get(i);
            if (t.getASN1Cls() == c && t.getTag().equals(tag)) {
                return t;
            }
        }
        return null;
    }

    public default ASN1Tag get(ASN1Cls c, BigInteger tag, ASN1 universal) {
        ASN1Tag org = get(c, tag);
        // 複製
        return convert(org, universal);
    }
    
    /*
     * Universal
     * 
     * @param name
     * @param tag
     * @return 
     */
//    public default ASN1Tag get(String name, ASN1 tag) {
//        return get(ASN1Cls.UNIVERSAL, tag.tag);
//    }

    /**
     * Context-Specific は番号で取得するとよい.
     * EXPLICIT 用
     * @param tag
     * @return 存在しない場合はnull
     */
    public default ASN1Tag getContextSpecific(int tag) {
        return get(ASN1Cls.CONTEXT_SPECIFIC, BigInteger.valueOf(tag));
    }

    public default ASN1Tag getApplication(int tag) {
        return get(ASN1Cls.APPLICATION, BigInteger.valueOf(tag));
    }

    public default ASN1Tag getPrivate(int tag) {
        return get(ASN1Cls.PRIVATE, BigInteger.valueOf(tag));
    }

    public default ASN1Tag get(ASN1Cls c, String name, int tag) {
        return get(c, BigInteger.valueOf(tag));
    }

    public default ASN1Tag getContextSpecific(String name, int tag) {
        return get(ASN1Cls.CONTEXT_SPECIFIC, name, tag);
    }

    public default ASN1Tag getApplication(String name, int tag) {
        return get(ASN1Cls.APPLICATION, name, tag);
    }

    public default ASN1Tag getPrivate(String name, int tag) {
        return get(ASN1Cls.PRIVATE, name, tag);
    }

    /**
     * ContextSpecific IMPLICIT からの復元
     * @param tag
     * @param universal IMPLICITの元の型
     * @return
     */
    public default ASN1Tag getContextSpecific(int tag, ASN1 universal) {
        return get(ASN1Cls.CONTEXT_SPECIFIC, BigInteger.valueOf(tag), universal);
    }

    public default ASN1Tag getApplication(int tag, ASN1 universal) {
        return get(ASN1Cls.APPLICATION, BigInteger.valueOf(tag), universal);
    }

    public default ASN1Tag getPrivate(int tag, ASN1 universal) {
        return get(ASN1Cls.PRIVATE, BigInteger.valueOf(tag), universal);
    }

    /**
     * 名前またはtagで取得.
     * BER CER DER などはタグを使用する.
     * JER などタグがない環境から変換された場合は名前を使用する。
     * @param c class
     * @param name パラメータ名 JERなど用
     * @param tag Context-Specific タグ DER用
     * @param universal IMPLICITの型変換
     * @return 
     */
    public default ASN1Tag get(ASN1Cls c, String name, int tag, ASN1 universal) {
        return get(c, BigInteger.valueOf(tag), universal);
    }

    public default ASN1Tag getContextSpecific(String name, int tag, ASN1 universal) {
        return get(ASN1Cls.CONTEXT_SPECIFIC, name, tag, universal);
    }

    public default ASN1Tag getApplication(String name, int tag, ASN1 universal) {
        return get(ASN1Cls.APPLICATION, name, tag, universal);
    }

    public default ASN1Tag getPrivate(String name, int tag, ASN1 universal) {
        return get(ASN1Cls.PRIVATE, name, tag, universal);
    }

    /**
     * IMPLICITからの復元
     * @param src
     * @param universal
     * @return 
     */
    default ASN1Tag convert(ASN1Tag src, ASN1 universal) {
        if (src == null) {
            return null;
        }
        ASN1DERFormat derformat = new ASN1DERFormat();
        byte[] der = (byte[]) src.rebind(derformat);
        try {
            ASN1Tag copy = ASN1Util.DERtoASN1(der);
            copy.setTag(ASN1Cls.UNIVERSAL, universal.tag.intValue());
            der = (byte[]) copy.rebind(derformat);
            copy = ASN1Util.DERtoASN1(der);
            return copy;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    /**
     * オブジェクトを奥深くに追加/更新する.
     * @param obj
     * @param index 巧妙な位置
     */
    void set(ASN1Tag obj, int... index);

    /**
     * オブジェクトを奥深くに追加する.
     * @param obj
     * @param index 巧妙な位置
     */
    void add(V obj, int... index);
    boolean add(V obj);

    default void add(BigInteger val) {
        add((V)new INTEGER(val));
    }

    default void add(long val) {
        add((V)new INTEGER(val));
    }

}
