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
package net.siisise.iso.asn1.tag;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 文字列の共通処理
 */
public class ASN1String extends ASN1Object<String> implements CharSequence {

    String string;

    public ASN1String( ASN1 id ) {
        super(id);
    }

    public ASN1String( ASN1 id, String str ) {
        super(id);
        string = str;
    }

    @Override
    public void decodeBody( byte[] val ) {
//        data = (byte[]) val.clone();
        switch ( ASN1.valueOf(getId()) ) {
        case UTF8String:
            string = new String(val, StandardCharsets.UTF_8);
            break;
        case CharacterString: { // UniversalString 証明書では廃止
                try {
                    string = new String(val, "utf-32be");
                } catch (UnsupportedEncodingException ex) {
                    throw new IllegalStateException( "Unknown String " + getId() + " yet.");
                }
            }
            break;

        case IA5String:
        case PrintableString:
        case GeneralString:
        case GraphicString:
        case NumericString:
        case TeletexString: // 証明書では廃止
        case VideotexString:
        case VisibleString:
        case UTCTime:
            string = new String(val, StandardCharsets.US_ASCII);
            break;
        case BMPString: // ISO 10646-1 基本多言語面
            string = new String(val, StandardCharsets.UTF_16BE);
            break;
        default:
            throw new UnsupportedOperationException( "Unknown String " + getId() + " yet.");
        }
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element ele = doc.createElement( ASN1.valueOf(getId()).toString() );
        ele.setTextContent(string);
        return ele;
    }

    /**
     * formatに従って符号化する。
     * Rebindではinterfaceで継承しているCharSequence 型がprimitive型として優先されるので注意.
     * @param <V> 出力型
     * @param format 書式
     * @return 出力
     */
    @Override
    public <V> V rebind(TypeFormat<V> format) {
        return format.stringFormat(this);
    }

    @Override
    public void decodeXML( Element ele ) {
        string = ele.getTextContent();
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public String getValue() {
        return string;
    }

    @Override
    public void setValue( String val ) {
        string = val;
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o) && string.equals(((ASN1String)o).string);
    }

    @Override
    public int length() {
        return string.length();
    }

    @Override
    public char charAt(int index) {
        return string.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return string.subSequence(start, end);
    }
}
