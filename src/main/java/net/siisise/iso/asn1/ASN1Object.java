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
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import net.siisise.io.Input;

/**
 * アクセサ略
 *
 * @param <T>
 */
public abstract class ASN1Object<T> implements ASN1Tag<T> {

    private ASN1Cls cls;// = ASN1Cls.UNIVERSAL;
    private BigInteger tag;
    /** 可変長形式 DERでは未使用 */
    protected boolean inefinite = false;

    protected ASN1Object() {
//        asn1class = ASN1Cls.UNIVERSAL;
    }

    /**
     * 拡張
     * @param cls 種別
     * @param tag タグ
     */
    protected ASN1Object( byte cls, BigInteger tag ) {
        this.cls = ASN1Cls.valueOf(cls);
        this.tag = tag;
    }

    /**
     * 
     * @param cls 種別 UNIVERSAL, APPLICATION, CONTEXT_SPECIFIC, PRIVATE
     * @param tag cls によりいろいろ
     */
    protected ASN1Object( ASN1Cls cls, BigInteger tag ) {
        this.cls = cls;
        this.tag = tag;
    }

    /**
     * Universal ASN.1 Object
     * @param tag 型の決まっているタグ
     */
    protected ASN1Object( ASN1 tag ) {
        this(ASN1Cls.UNIVERSAL, tag.tag);
    }

    /**
     * ASN.1 class code
     * @return 
     */
    @Override
    public int getASN1Class() {
        return cls.cls;
    }

    /**
     * ASN.1 class 
     * @return 
     */
    @Override
    public ASN1Cls getASN1Cls() {
        return cls;
    }
    
    @Override
    public void setTag(ASN1Cls c, int tag) {
        cls = c;
        this.tag = BigInteger.valueOf(tag);
    }

    /**
     * CER では文字列分割で構造あり?
     * @return 
     */
    @Override
    public boolean isConstructed() {
        return false;
    }

    /**
     * コンストラクタで指定するかオーバーライドするかどちらか
     * @return 
     */
    @Override
    public int getId() {
        return tag.intValue();
    }

    @Override
    public BigInteger getTag() {
        if ( tag == null ) {
            return BigInteger.valueOf(getId());
        }
        return tag;
    }
//    abstract public T getValue();
//    abstract public void setValue(T val);

    /**
     * ヘッダ書き込みで前後するのであまり使えない
     * @param out
     * @throws java.io.IOException
     */
    public void encodeAll( OutputStream out ) throws IOException {
        out.write(encodeAll());
    }

    /**
     * デコーダから呼ばれるのみ
     * OCTETSTRINGでも struct の場合は ASN1Struct を使うので length が -1 になることはないはず
     * @param in
     * @param length
     */
    @Override
    public void decodeBody( Input in, int length ) {
        byte[] data = new byte[length];
        in.read(data);
        decodeBody(data);
    }

    public void decodeBody( byte[] data ) {
        throw new UnsupportedOperationException("Not supported " + getTag() + " yet.");
    }

    @Override
    public int compareTo( ASN1Tag o ) {
        if ( getASN1Cls() != o.getASN1Cls() ) {
            return getASN1Class() - o.getASN1Class();
        }
        if ( getId() != o.getId() ) {
            return getId() - o.getId();
        }
        return compare(encodeAll(), o.encodeAll());
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass() &&
                this.getASN1Class() == ((ASN1Object)o).getASN1Class() &&
                this.getId() == ((ASN1Object)o).getId() &&
                this.inefinite == ((ASN1Object)o).inefinite;
    }

    static int compare(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        for ( int i = 0; i < len; i++ ) {
            if ( a[i] != b[i] ) {
                int c = (((int)b[i]) & 0xff) - (((int)a[i]) & 0xff);
                return c > 0 ? 1 : -1;
            }
        }
        return (a.length == b.length) ? 0 : ((a.length < b.length ) ? 1 : -1);
    }
}
