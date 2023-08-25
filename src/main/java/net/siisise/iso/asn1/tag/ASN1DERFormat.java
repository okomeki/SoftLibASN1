/*
 * Copyright 2023 okome.
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

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.siisise.bind.format.TypeBind;
import net.siisise.bind.format.TypeFallFormat;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.lang.Bin;

/**
 * ASN1Object経由の手抜き版
 */
public class ASN1DERFormat extends TypeFallFormat<byte[]> implements TypeBind<byte[]> {
    
    ASN1Convert cnv = new ASN1Convert();
    
    byte[] encodeTag(ASN1Object obj) {
        byte[] tagNo;
        BigInteger tag = obj.getTag();
        int bitLen = tag.bitLength();
        if ( bitLen <= 5 ) {
            tagNo = new byte[1];
            tagNo[0] = (byte) ((obj.getASN1Class() << 6) | (obj.isStruct() ? 0x20 : 0) | tag.intValue());
        } else {
            int len = (bitLen + 6) / 7;
            tagNo = new byte[len + 1];
            BigInteger t = tag;
            for ( int i = 0; i < len; i++ ) {
                tagNo[i + 1] = (byte) (((i == len - 1) ? 0x80 : 0) | t.shiftRight((len - i - 1) * 7).intValue() & 0x7f);
            }
            tagNo[0] = (byte) ((obj.getASN1Class() << 6) | (obj.isStruct() ? 0x20 : 0) | 0x1f);
        }
        return tagNo;
    }

    private Packet encodeLength( int len ) {
        PacketA pac = new PacketA();
//        if ( inefinite ) {
//            pac.write(INFLEN);
//            return pac;
//        }
        pac.write( Bin.toByte(len));
        int i;
        do {
            i = pac.read();
        } while ( i == 0 && pac.length() > 1 );
        if ( i > 0 ) {
            pac.backWrite(i);
        }
        if ( len >= 0x80 ) {
            pac.backWrite(0x80 + pac.size());
        }
        return pac;
    }
    
    byte[] encode(ASN1Object obj) {
        byte[] tag = encodeTag(obj);
        byte[] body = obj.encodeBody();
        return obj.encodeAll();
    }

    @Override
    public byte[] nullFormat() {
        encode(cnv.nullFormat());
        return cnv.nullFormat().encodeAll();
    }

    @Override
    public byte[] booleanFormat(boolean bool) {
        return cnv.booleanFormat(bool).encodeAll();
    }

    /**
     * INTEGER
     * @param num
     * @return 
     */
    @Override
    public byte[] numberFormat(Number num) {
        return cnv.numberFormat(num).encodeAll();
    }
    
    @Override
    public byte[] byteArrayFormat(byte[] bytes) {
        return cnv.byteArrayFormat(bytes).encodeAll();
    }

    @Override
    public byte[] stringFormat(String str) {
        return cnv.stringFormat(str).encodeAll();
    }

    /**
     * 並び順が保証されていればSEQUENCEとして使える
     * なければSETとして使える
     * @param map
     * @return 
     */
    @Override
    public byte[] mapFormat(Map map) {
        return cnv.mapFormat(map).encodeAll();
    }

    @Override
    public byte[] listFormat(List list) {
        return cnv.listFormat(list).encodeAll();
    }
    
    /**
     * SEQUENCE
     */
    @Override
    public byte[] collectionFormat(Collection col) {
        return cnv.collectionFormat(col).encodeAll();
    }
    
}
