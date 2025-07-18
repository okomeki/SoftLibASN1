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
package net.siisise.iso.asn1;

import java.math.BigInteger;
import net.siisise.block.ReadableBlock;
import net.siisise.io.Input;
import net.siisise.iso.asn1.tag.ASN1DERFormat;

/**
 * ITU-T X.690 DER.
 * 新しいデコーダ
 */
public class ASN1X690DER extends ASN1X690 implements ASN1X690DEC {

    public ReadableBlock toDER(ASN1Tag obj) {
        return ReadableBlock.wrap((byte[])obj.rebind(new ASN1DERFormat()));
//        return ReadableBlock.wrap(Rebind.valueOf(obj, new ASN1DERFormat()));
    }

    /**
     * DER Decode.
     * BER/CERもだいたいおなじ.
     *
     * @param in 入力元
     * @return ASN1Object
     */
    @Override
    public ASN1Tag decode(Input in) {
        int identifier = (byte) in.read();
        ASN1Cls cls = ASN1Cls.valueOf((identifier >> 6) & 0x03); // 上位2bit
        boolean constructed = (identifier & 0x20) != 0;
        BigInteger tagNumber = readTag(identifier, in);
        long len = readLength(in);
        if (len < 0) { // primitive の不定サイズ 不可 DER structured の不定形も不可
            throw new java.lang.IllegalStateException("length");
//            contents = in;
        }
        ReadableBlock contents = subBlock(in, len);

        return decode(identifier, cls, constructed, tagNumber, len, contents);
    }

    /**
     * 構造系をデコーダ側でなんとかする.
     * @param struct
     * @param length 解析用
     * @param in body
     * @return 
     */
    @Override
    ASN1Struct decodeUniversalStructBody(ASN1Struct struct, long length, ReadableBlock in) {
//        struct.decodeBody(in);

        while ( in.length() > 0) {
            ASN1Tag o = decode(in);
            struct.add(o);
        }
        return struct;
    }

}
