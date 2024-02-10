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
    
    /**
     * DERは固定で決まる.
     * @param len
     * @return 
     */
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
    
    /**
     * DER Encode.
     * inefinite 対応は外した
     * @param obj
     * @return 
     */
    byte[] encodeDER(ASN1Object obj) {
        byte[] tagNo = encodeTagNo(obj);
        byte[] body = obj.encodeBody();
        
        Packet lengthField = encodeLength(body.length);
        Packet pac = new PacketA();
        pac.write(tagNo);
        pac.write(lengthField);
        pac.write(body);
//        if ( obj.infinite) {
//            pac.write(EO);
//        }
        return pac.toByteArray();
    }

    /**
     * encode TagNo
     * @param obj
     * @return 
     */
    byte[] encodeTagNo(ASN1Object obj) {
        BigInteger tagId = obj.getTag();
        int bitLen = tagId.bitLength();
        byte[] tagNo;

        if ( bitLen <= 5 ) {
            tagNo = new byte[1];
            tagNo[0] = (byte) ((obj.getASN1Class() << 6) | (obj.isStruct() ? 0x20 : 0) | tagId.intValue());
        } else {
            int len = (bitLen + 6) / 7;
            tagNo = new byte[len + 1];
            BigInteger t = tagId;
            for ( int i = 0; i < len; i++ ) {
                tagNo[i + 1] = (byte) (((i == len - 1) ? 0x80 : 0) | t.shiftRight((len - i - 1) * 7).intValue() & 0x7f);
            }
            tagNo[0] = (byte) ((obj.getASN1Class() << 6) | (obj.isStruct() ? 0x20 : 0) | 0x1f);
        }
        return tagNo;
    }

    @Override
    public byte[] nullFormat() {
        return encodeDER(cnv.nullFormat());
    }

    @Override
    public byte[] booleanFormat(boolean bool) {
        return encodeDER(cnv.booleanFormat(bool));
    }

    /**
     * INTEGER
     * @param num
     * @return 
     */
    @Override
    public byte[] numberFormat(Number num) {
        return encodeDER(cnv.numberFormat(num));
    }
    
    @Override
    public byte[] byteArrayFormat(byte[] bytes) {
        return encodeDER(cnv.byteArrayFormat(bytes));
    }

    @Override
    public byte[] stringFormat(String str) {
        return encodeDER(cnv.stringFormat(str));
    }

    /**
     * 並び順が保証されていればSEQUENCEとして使える
     * なければSETとして使える
     * @param map
     * @return 
     */
    @Override
    public byte[] mapFormat(Map map) {
        return encodeDER(cnv.mapFormat(map));
    }

    @Override
    public byte[] listFormat(List list) {
        return encodeDER(cnv.listFormat(list));
    }
    
    /**
     * SEQUENCE
     */
    @Override
    public byte[] collectionFormat(Collection col) {
        return encodeDER(cnv.collectionFormat(col));
    }
    
}
