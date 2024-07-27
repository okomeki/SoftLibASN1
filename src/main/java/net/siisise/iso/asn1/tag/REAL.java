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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import net.siisise.abnf.ABNF;
import net.siisise.abnf.ABNFReg;
import net.siisise.abnf.parser5234.ABNF5234;
import net.siisise.bind.format.TypeFormat;
import net.siisise.block.ReadableBlock;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import net.siisise.lang.Bin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * X.690 202102
 * 8.5 REAL 実数値.
 * 浮動小数点
 * 2進数 / 16進数 / 10進数
 *
 * @param <T>
 */
public class REAL<T extends Number> extends ASN1Object<T> implements ASN1Tag {

    public static final byte PLUS_INFINITY = 0x40;
    public static final byte MINUS_INFINITY = 0x41;
    public static final byte NaN = 0x42;
    public static final byte MINUS_ZERO = 0x43;

    protected T val;

    public REAL() {
        super(ASN1.REAL);
    }
/*
    public REAL(T v) {
        super(ASN1.REAL);
        val = v;
    }
*/
    public REAL(Double v) {
        super(ASN1.REAL);
        val = (T)v;
    }

    public REAL(Float v) {
        super(ASN1.REAL);
        val = (T)(Double)v.doubleValue();
    }

    public REAL(BigDecimal v) {
        super(ASN1.REAL);
        val = (T)v;
    }
    
    @Override
    public T getValue() {
        return val;
    }

    @Override
    public void setValue(T val) {
        this.val = val;
    }

    @Override
    public byte[] encodeBody() {
        if ( val instanceof Double || val instanceof Float) {
            return encodeDoubleBody(val.doubleValue());
        } else if ( val instanceof BigDecimal ) {
            return encodeDecimalBody((BigDecimal)val);
        }
        throw new UnsupportedOperationException();
    }
    
    /**
     * Double型の精度で IEEE754 format から ASN.1 DER 2進数表記に変換する.
     * F = 0
     * @param v
     * @return ASN.1 REAL型
     */
    public byte[] encodeDoubleBody(double v) {
        long ieee754 = Double.doubleToRawLongBits(v);
        boolean flag = (ieee754 & 0x8000000000000000l) != 0;
        int exp = (int) (ieee754 >>> 52) & 0x7ff; // 符号 - * 2^1 を 0x400 的なところにもってくる
        long m = ieee754 & 0x000fffffffffffffl; // fraction
        switch (exp) {
            case 0:
                // 非正規化 または 0
                if (m == 0) {
                    if (flag) {
                        return new byte[]{MINUS_ZERO};
                    } else {
                        return new byte[0];
                    }
                }
                // 非正規化数
                exp++; // 元の精度に戻すだけ
                break;
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
            default:
                m |= 0x0010000000000000l; // 最上位ビットをつける
                break;
        }
        // 指数位置の変換
        while ( (m & 1) == 0) {
            m >>>= 1;
            exp++;
        }
        exp -= 52 + 0x3ff; // ビット + 符号

        Packet pac = new PacketA();
        int b0 = (flag ? 0xc0 : 0x80); // bS0000
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
    
    static final BigInteger TEN = BigInteger.valueOf(10);
    static final int NR3HEAD = 0x03;
    
    public byte[] encodeDecimalBody(BigDecimal val) {
        
        if ( val.signum() == 0 ) {
            return new byte[0];
        }
        
        // unscaledValue * 10^-scale
        int scale = val.scale(); // + 小数点以下の桁数
        BigInteger us = val.unscaledValue();
        while ( us.mod(TEN).compareTo(BigInteger.ZERO) == 0 ) {
            scale--;
            us = us.divide(TEN);
        }
        StringBuilder m = new StringBuilder();
        m.append(us.toString());
        //int nl = n.length();
        m.append(".E");
        if ( scale == 0 ) {
            m.append("+0");
        } else {
            m.append(Integer.toString(scale));
        }
        
        Packet pac = new PacketA();
        pac.write(NR3HEAD); // 0000 0011
        pac.write(m.toString().getBytes(StandardCharsets.ISO_8859_1));
        return pac.toByteArray();
    }

    /**
     * BER 相当も入れておくかもしれない.
     * ToDo: 長さチェック
     * @param src
     */
    @Override
    public void decodeBody(byte[] src) {
        if (src.length == 0) {
            // PLUS_ZERO;
            val = (T) (Double) 0.0;
        } else {
            // 8: 1: バイナリエンコーディング 0: 10進 または SpecialRealValue
            // 7:
            int v = src[0] & 0xff;
            switch (v >> 6) {
                case 0:
                    // 8.5.8. 10進
                    decode10(src);
                    break;
                case 1:
                    switch (v) { // 0x4x - 0x7f
                        case PLUS_INFINITY:
                            val = (T) (Double) Double.POSITIVE_INFINITY;
                            break;
                        case MINUS_INFINITY:
                            val = (T) (Double) Double.NEGATIVE_INFINITY;
                            break;
                        case NaN:
                            val = (T) (Double) Double.NaN;
                            break;
                        case MINUS_ZERO:
                            val = (T) (Double) (-0.0);
                            break;
                        default:
                            throw new java.lang.IllegalStateException("Reserved,");
                    }
                    break;
                default: // 8.5.7.
                    decode2(ReadableBlock.wrap(src));
                    break;
            }
        }
    }

    /**
     * 8.5.7. バイナリエンコーディング
     * 1SBBFFEX
     * ToDo: 不正値
     * @param src 
     */
    private void decode2(ReadableBlock sb) {
        int front = sb.read();
        boolean S = (front & 0x40) != 0;
        int B = (front >> 4) & 3; // 進数 00: base 2 01: base 8 10: base16 11: Reserved
        if ( B == 3 ) {
            throw new IllegalStateException("Reserved");
        } else if ( B != 0 ) { // BER まだない
//            int F = (front >> 2) & 0x03;
            throw new UnsupportedOperationException("Not supported yet.");
        }
        int exl = front & 0x3; // 指数長
        // 00: 1 01: 2 10: 3 11
        if ( exl == 3 ) { // 大きすぎるので未サポート?
            exl = sb.read() + 2;
            if ( exl > 3) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        byte[] x = new byte[exl + 1];
        sb.read(x);
        long exp = new BigInteger(x).intValue(); //フラグあり
        // ToDo: 範囲確認
        byte[] mb = new byte[sb.size()];
        sb.read(mb);
        BigInteger mi = unsign(mb);
        // double化
        while ( mi.bitLength() > 53 ) { // 精度低下, 未対応
            mi = mi.shiftRight(1);
            exp++;
        }
        long m = mi.longValue();
        exp += 52 + 0x3ff; // ビット位置 + フラグ補正
        while ((m & 0x0010000000000000l) == 0l && exp != 1) {
            m <<= 1;
            exp--;
        }
        if ( exp == 1 && (m < 0x0010000000000000l) ) { // 非正規化?
            exp--;
        }
        m &= 0x000fffffffffffffl;
        // ToDo: 範囲以外の非正規化
        //exp--;
        m |= exp << 52;
        m |= S ? 0x8000000000000000l : 0;
        val = (T)(Double)Double.longBitsToDouble(m);
    }

    /**
     * フラグなしバイト列をBigIntegerに.
     * @param x バイト列
     * @return 符号なし整数
     */
    private BigInteger unsign(byte[] x) {
        if ( x.length > 0 && x[0] < 0 ) {
            byte[] t = new byte[x.length + 1];
            System.arraycopy(x,0,t,1,x.length);
            x = t;
        }
        return new BigInteger(x);
    }

    /**
     * 8.5.8. 10進符号化
     * ISO 6903 JIS X 0210
     * @param src 
     */
    private void decode10(byte[] src) {
        int front = src[0] & 0x3f;
        switch ( front ) {
//            case 1: // NR1
//            case 2: // NR2
            case 3: // NR3
                decodeNR3(src);
                break;
            default:
                throw new IllegalStateException("Not supported yet.");
        }
    }

    static final ABNFReg REG = new ABNFReg();

    // ISO 6903 5.2 改
    static final ABNF digit = REG.rule("digit", ABNF5234.DIGIT);
    static final ABNF plus = REG.rule("plus", ABNF.bin('+'));
    static final ABNF minus = REG.rule("minus", ABNF.bin('-'));
    static final ABNF sign = REG.rule("flag", plus.or1(minus));
    static final ABNF decimalMark = REG.rule("decimal-mark", ABNF.bin(',').or1(ABNF.bin('.')));
//    static final ABNF space = REG.rule("space", ABNF5234.SP);
    static final ABNF exponentMark = REG.rule("exponent-mark", ABNF.text("e"));

    // ISO 6903 8.2 NR3 改
    static final ABNF significand = REG.rule("significand", digit.ix().pl(decimalMark,digit.x()).or(digit.x().pl(decimalMark,digit.ix())));
    // + がつくのは0のときだけ
    static final ABNF exponent = REG.rule("exponent", ABNF.bin("+0").or1(minus.c().pl(ABNF5234.DIGIT.ix())));
//    static final ABNF signedNR3 = REG.rule("signed-NR3", space.x().pl(sign.c(), significand, exponentMark, exponent));
    // space なし
    static final ABNF signedNR3 = REG.rule("signed-NR3", minus.c().pl(significand, exponentMark, exponent));
//    static final ABNF unsignedNR3 = REG.rule("unsigned-NR3", space.x().pl(significand, exponentMark, exponent));
//    static final ABNF unsignedNR3 = REG.rule("unsigned-NR3", significand.pl(exponentMark, exponent));
//    static final ABNF NR3 = REG.rule("NR3", unsignedNR3.or1(signedNR3));

    /**
     * 10進 ISO 6903 JIS X 0210 NR3.
     * DER正規化では 仮数.E指数
     * @param src NR3 form
     */
    private void decodeNR3(byte[] src) {
        ReadableBlock rb = ReadableBlock.wrap(src);
        rb.read(); // 解析済みなので捨て
        ReadableBlock nr3 = signedNR3.is(rb);
        if ( rb.size() != 0 ) { // サイズ確認
            throw new java.lang.SecurityException("invalid NR3");
        }
        String nr3str = new String(nr3.toByteArray(), StandardCharsets.ISO_8859_1);
        // 詳細は略
        val = (T)new BigDecimal(nr3str);
    }

    /**
     * XML化.
     * 独自の形式.
     * @param doc base document
     * @return REALのXML値
     */
    @Override
    public Element encodeXML(Document doc) {
        Element ele = doc.createElement( ASN1.valueOf(getId()).toString() );
        if ( val instanceof BigDecimal ) {
            ele.setAttribute("base", "10");
        } else {
            ele.setAttribute("base", "2");
        }
        ele.setTextContent(val.toString());
        return ele;
    }

    @Override
    public <V> V rebind(TypeFormat<V> format) {
        return format.numberFormat(val);
    }

    @Override
    public void decodeXML(Element element) {
        String base = element.getAttribute("base");
        String text = element.getTextContent();
        if ( base.equals("10")) {
            val = (T) new BigDecimal(text);
        } else if ( base.equals("2")) {
            val = (T) Double.valueOf(text);
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
