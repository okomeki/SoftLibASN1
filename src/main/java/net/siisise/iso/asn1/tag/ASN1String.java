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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import net.siisise.bind.format.TypeFormat;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 文字列の共通処理 (仮).
 * DirectryString ::= {
 *   PrintableString
 *   TeletexString
 *   BMPString
 *   UTF8String
 *   UniversalString
 * }
 * VisibleString
 */
public class ASN1String extends ASN1Object<String> implements CharSequence {

    /**
     * 型指定用。
     * tagとしては利用しない
     */
    ASN1 tag;
    String string;

    public ASN1String( ASN1 id ) {
        super(id);
        tag = id;
    }

    public ASN1String( ASN1 id, String str ) {
        super(id);
        tag = id;
        string = str;
    }

    @Override
    public void decodeBody( byte[] val ) {
//        data = (byte[]) val.clone();
        switch ( tag ) {
        case UTF8String:
            string = new String(val, StandardCharsets.UTF_8);
            break;
        case BMPString: // ISO 10646-1 UCS-2 基本多言語面
            string = new String(val, StandardCharsets.UTF_16BE);
            break;
        case UniversalString:  // UCS-4 CharacterString 証明書では互換用
                string = new String(val, Charset.forName("utf-32be"));
            break;
        case IA5String: // ASCII ITU-T T.50 IRA の旧称 INTERNATIONAL ALPHABET No. 5
        case PrintableString:
        case GeneralString:
        case GraphicString:
        case NumericString:
        case VideotexString:
        case VisibleString:
        case UTCTime: // VisibleString と同じ
            string = new String(val, StandardCharsets.US_ASCII);
            break;
        case TeletexString: // ISO-8859-1 証明書では互換のみ 仮実装 ITU-T T.61 ページ切り替えは未対応 cp1036 cp20261
            string = new String(val, StandardCharsets.ISO_8859_1);
            break;
        default:
            throw new UnsupportedOperationException( "Unknown String " + getId() + " yet.");
        }
    }
    
    /**
     * 符号化
     * tag別があるので残すかも.
     * @return 符号
     */
    @Override
    public byte[] encodeBody() {
        switch ( tag ) {
            case UTF8String:
                return string.getBytes(StandardCharsets.UTF_8);
            case BMPString:
                return string.getBytes(StandardCharsets.UTF_16BE);
            case UniversalString:
                return string.getBytes(Charset.forName("utf-32be"));
            case IA5String:
            case PrintableString:
            case GeneralString:
            case GraphicString:
            case NumericString:
            case VideotexString:
            case VisibleString:
            case UTCTime:
                return string.getBytes(StandardCharsets.US_ASCII);
            case TeletexString:
                return string.getBytes(StandardCharsets.ISO_8859_1);
        }
        throw new UnsupportedOperationException();
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
