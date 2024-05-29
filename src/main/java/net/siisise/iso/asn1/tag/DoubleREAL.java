/*
 * Copyright 2024 okome.
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
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.lang.Bin;

/**
 * IEEE 754 Double型などに対応する.
 * 指数 2^e
 */
public class DoubleREAL extends REAL<Double> {

    public DoubleREAL(double d) {
        super(d);
    }

    /**
     * DERでは 2進数表記に対応、F = 0
     * @return 
     */
    @Override
    public byte[] encodeBody() {
        long ieee754 = Double.doubleToRawLongBits(val);
        boolean flag = (ieee754 & 0x8000000000000000l) != 0;
        int exp = (int) (ieee754 >>> 52) & 0x7ff; // 符号 - * 2^1 を 0x400 的なところにもってくる
        long m = ieee754 & 0x000fffffffffffffl; // fraction
        switch (exp) {
            case 0x7ff:
                if (m == 0) { // infinity
                    if (flag) {
                        return new byte[]{MINUS_INFINITY};
                    } else {
                        return new byte[]{PLUS_INFINITY};
                    }
                } else { // NaN
                    return new byte[]{NaN};
                }
            case 0:
                // 非正規化 または 0
                if (m == 0) {
                    if (flag) {
                        return new byte[]{MINUS_ZERO};
                    } else {
                        return new byte[0];
                    }
                }   // 非正規化数
                // 精度を落とし指数を拡張する
//            int 精度 = 52;
                exp++; // 元の精度
                /*
                do {
                    exp--;
                //  精度--;
                    m <<= 1;
                } while ( (m & 0x0010000000000000l) == 0);
                // m &= 0x000fffffffffffffl; // 一旦、正規化に寄せる
                */
                break;
            default:
                m |= 0x0010000000000000l; // 最上位ビットをつける
                break;
        }
        while ( (m & 1) == 0) {
            m >>>= 1;
            exp++;
        }
        exp-=52;
        
        Packet pac = new PacketA();
        int b0 = (flag ? 0xc0 : 0x80); // bS0000
        exp -= 0x3ff;
        if (exp < -128 || 127 < exp) { // exp 2バイトコース
            b0 |= 0x01;
            pac.write(b0);
            pac.write(Bin.toByte((short)exp));
        } else { // 1バイトコース
            pac.write(b0);
            pac.write(exp);
        }
        // m のINTEGER風符号化 長さは残りサイズ
        pac.write(BigInteger.valueOf(m).toByteArray());
        return pac.toByteArray();
    }
}
