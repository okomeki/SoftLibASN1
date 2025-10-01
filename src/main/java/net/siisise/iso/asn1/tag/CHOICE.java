/*
 * Copyright 2025 okome.
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

import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1Tag;

/**
 * 抽象?
 * X.690 BER/CER/DER 8.13 など
 * MAX SIZE 1のSEQUENCE または name付きIMPLICIT
 * key と tag を持ちたい?
 * MAXサイズが1のSEQUENCEかもしれない
 * @param <T>
 */
public class CHOICE<T extends ASN1Tag> extends SEQUENCEMap<T> {

    /**
     * 書き.
     * 他の選択があれば削除する.
     * @param name 名前
     * @param val 値
     */
    public void put(Object name, T val) {
        clear();
        super.put((String)name, val);
    }

    /**
     * 読み.
     * 仮.
     * @param <V> 型情報
     * @param format 出力形式
     * @return 出力
     */
    @Override
    public <V> V rebind(TypeFormat<V> format) {
        return format.enumFormat(this);
    }
}
