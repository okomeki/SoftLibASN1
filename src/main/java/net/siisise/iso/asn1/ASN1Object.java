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
import net.siisise.bind.format.TypeFormat;
import net.siisise.io.Input;
import net.siisise.iso.asn1.tag.ASN1DERFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * アクセサ略
 *
 * @param <T>
 */
public abstract class ASN1Object<T> implements java.lang.Comparable<ASN1Object> {

//    ASN1Syntax syntax;
    private ASN1Cls asn1class = ASN1Cls.UNIVERSAL;
    private BigInteger tag;
    /** 可変長形式 DERでは未使用 */
    protected boolean inefinite = false;

    protected ASN1Object() {
//        asn1class = ASN1Cls.UNIVERSAL;
    }

    /**
     * 拡張
     * @param cls
     * @param tag
     */
    protected ASN1Object( byte cls, BigInteger tag ) {
        asn1class = ASN1Cls.valueOf(cls);
        this.tag = tag;
    }

    protected ASN1Object( ASN1Cls cls, BigInteger tag ) {
        asn1class = cls;
        this.tag = tag;
    }

    protected ASN1Object( ASN1 tag ) {
        asn1class = ASN1Cls.UNIVERSAL;
        this.tag = tag.tag;
    }

    public int getASN1Class() {
        return asn1class.cls;
    }
    
    public ASN1Cls getASN1Cls() {
        return asn1class;
    }

    public boolean isStruct() {
        return false;
    }
    
    abstract public T getValue();
    abstract public void setValue(T val);

    /**
     * ヘッダ書き込みで前後するのであまり使えない
     * @param out
     * @throws java.io.IOException
     */
    public void encodeAll( OutputStream out ) throws IOException {
        out.write(encodeAll());
    }

    /**
     * ASN.1 DER encode
     * @return 
     */
    public byte[] encodeAll() {
        ASN1DERFormat format = new ASN1DERFormat();
//        return rebind(format);
        byte[] body = encodeBody();
        return format.encodeDER(this, body);
    }

    /**
     * DER encoded body.
     * 署名などで使用する.
     * rebind でなんとかしたい
     * @return DER encoded value
     */
    public abstract byte[] encodeBody();
    
//    static final byte[] INFLEN = {(byte)0x80};
//    static final byte[] EO = {0,0};

    /**
     * デコーダから呼ばれるのみ
     * OCTETSTRINGでも struct の場合は ASN1Struct を使うので length が -1 になることはないはず
     * @param in
     * @param length
     */
    public void decodeBody( Input in, int length ) {
        byte[] data = new byte[length];
        in.read(data);
        decodeBody(data);
    }

    public void decodeBody( byte[] data ) {
        throw new UnsupportedOperationException("Not supported " + getTag() + " yet.");
    }

    /**
     * タグとデータを書き
     * @param doc
     * @return  */
    abstract public Element encodeXML( Document doc );

    /**
     * 符号化.
     * @param <V> 出力型
     * @param format 書式
     * @return 変換出力
     */
    abstract public <V> V rebind(TypeFormat<V> format);

    /** データのみ読む
     * @param element */
    abstract public void decodeXML( Element element );

    /**
     * コンストラクタで指定するかオーバーライドするかどちらか
     * @return 
     */
    public int getId() {
        return tag.intValue();
    }

    public BigInteger getTag() {
        if ( tag == null ) {
            return BigInteger.valueOf(getId());
        }
        return tag;
    }
    
    @Override
    public int compareTo( ASN1Object o ) {
        if ( getASN1Class() != o.getASN1Class() ) {
            return getASN1Class() - o.getASN1Class();
        }
        if ( getId() != o.getId() ) {
            return getId() - o.getId();
        }
        if ( inefinite != o.inefinite) {
            return ( inefinite ? 1 : 0 ) - ( o.inefinite ? 1 : 0 );
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass() &&
                this.getASN1Class() == ((ASN1Object)o).getASN1Class() &&
                this.getId() == ((ASN1Object)o).getId() &&
                this.inefinite == ((ASN1Object)o).inefinite;
    }
}
