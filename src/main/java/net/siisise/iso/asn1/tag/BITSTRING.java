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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.siisise.bind.format.TypeFormat;
import net.siisise.io.BASE64;
import net.siisise.io.BigBitPacket;
import net.siisise.io.BitPacket;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import net.siisise.iso.asn1.ASN1Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * bitstring.
 * 末尾の未使用ビット数0-7を最初に記録、本体を記録.
 * CER/DER 長さが0のとき 1オクテットの0で符号化しますょ
 * X.680 3.8.7 bitstring type
 * X.690 8.6
 */
public class BITSTRING extends ASN1Object<byte[]> implements ASN1Tag {

    private byte[] data;
    /**
     * 全長ビット
     */
    private long bitlen;

    public BITSTRING() {
        super(ASN1.BITSTRING);
    }

    /**
     * バイト単位のデータをビット列として.
     * bit単位データなし
     * @param d データ 複製していない.
     */
    public BITSTRING(byte[] d) {
        this();
        data = d;
        bitlen = data.length * 8L;
    }

    /**
     * バイト列からビット列.
     * d全部使っていること.
     * @param d データ 複製していない.
     * @param len ビット長(全体)
     */
    public BITSTRING(byte[] d, long len) {
        this();
        data = d;
        bitlen = len;
    }

    /**
     * ビット単位データを詰める.
     * @param pac ビット列
     */
    @Deprecated
    public BITSTRING(BitPacket pac) {
        bitlen = pac.bitLength();
        data = new byte[(int)((bitlen + 7L) / 8)];
        pac.read(data); // バイト単位のみ
        int b = (int)(bitlen % 8);
        if ( b != 0 ) { // BigBitPacket / LittleBitPacket 対応 上位ビットから詰める
            data[data.length - 1] = (byte)(pac.readInt(b) << (8 - b));
        }
    }

    /**
     * X.690 DER encode.
     * @return 未使用ビット数 + 本体
     */
    @Override
    public byte[] encodeBody() {
        int len = (int) ((bitlen +7l) / 8);
        byte[] out = new byte[len + 1];
        out[0] = (byte) ((-(bitlen % 8)) & 0x7);
        System.arraycopy(data, 0, out, 1, len);
        return out;
    }

    /**
     * DER decode
     * @param data 未使用ビット数 + 本体 
     */
    @Override
    public void decodeBody( byte[] data ) {
        int unbits = (int) data[0] & 0xff;
        if ( unbits > 7 ) throw new IllegalStateException();

        bitlen = data.length * 8L - 8 - unbits;
        this.data = new byte[data.length - 1];
        System.arraycopy(data, 1, this.data, 0, data.length - 1);
        // BER では不要なビットを掃除する
        this.data[this.data.length - 1] &= 0x100 - ((1<<(unbits)));
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element ele = doc.createElement( ASN1.BITSTRING.name() );
        ele.setAttribute("bitlen", String.valueOf(bitlen));
        BASE64 b64 = new BASE64();
        String val = b64.encode(data);
        ele.setTextContent(val);
        return ele;
    }

    @Override
    public void decodeXML( Element element ) {
        bitlen = Long.parseLong(element.getAttribute("bitlen"));
        data = BASE64.decodeBase(element.getTextContent());
    }

    /**
     * TODO: bit
     *
     * @return
     */
    @Override
    public String toString() {
        try {
            return "BIT STRING len:" + data.length + " " + ASN1Util.toASN1List(data);
        } catch (UnsupportedOperationException e) {
            
        } catch (IOException ex) {
            Logger.getLogger(BITSTRING.class.getName()).log(Level.SEVERE, null, ex);
        }
        BASE64 b64 = new BASE64();
        return "BIT STRING " + b64.encode(data);
    }

    /**
     * 未使用ビット数を考慮しない
     * @return 
     */
    @Override
    public byte[] getValue() {
        return data;
    }

    /**
     * 未使用ビット数を考慮しない
     * @param val */
    @Override
    public void setValue( byte[] val ) {
        data = val;
        bitlen = data.length * 8;
    }

    @Override
    public <V> V rebind(TypeFormat<V> format) {
        BitPacket pac = new BigBitPacket();
        pac.writeBit(data, 0, bitlen);
        return format.bitArrayFormat(pac);
    }
}
