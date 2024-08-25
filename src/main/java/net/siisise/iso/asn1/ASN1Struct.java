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

import java.math.BigInteger;
import java.util.List;
import net.siisise.iso.asn1.tag.INTEGER;

/**
 * SEQUENCE、SETと標準以外の構造
 *
 */
public interface ASN1Struct extends ASN1Tag<List<ASN1Tag>> {

    /**
     * 可変長形式.
     * @return true 長さを持たない構造
     */
    public boolean isInefinite();
    public void setInefinite(boolean inefinite);

    public int size();

    public ASN1Tag get(int offset);
    
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
     * UNIVERSAL 限定? 同じタグで n番目.
     * @param tag タグ
     * @param index 
     * @return 
     */
    public ASN1Tag get(BigInteger tag, int index);

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
    void add(ASN1Tag obj, int... index);
    boolean add(ASN1Tag obj);

    default void add(BigInteger val) {
        add(new INTEGER(val));
    }

    default void add(long val) {
        add(new INTEGER(val));
    }

}
