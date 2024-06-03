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

import java.math.BigDecimal;
import net.siisise.block.ReadableBlock;
import net.siisise.iso.asn1.ASN1Decoder;
import net.siisise.lang.Bin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class REALTest {
    
    public REALTest() {
    }
    
    /**
     * Test of encodeBody method, of class REAL.
     */
    @Test
    public void testEncodeBody() {
        System.out.println("REAL encodeBody");
        double d = -1.5;
        REAL instance = new REAL(d);
        byte[] expResult = Bin.toByteArray("0903c0ff03");
        byte[] result = instance.encodeAll();
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);
        
        double dd = (double) ASN1Decoder.toASN1(ReadableBlock.wrap(result)).getValue();
        assertEquals(d, dd);

        d = 1.0;
        instance = new REAL(d);
        expResult = Bin.toByteArray("0903800001");
        result = instance.encodeAll();
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        dd = (double) ASN1Decoder.toASN1(ReadableBlock.wrap(result)).getValue();
        assertEquals(d, dd);

        d = 3.1415;
        instance = new REAL(d);
        expResult = Bin.toByteArray("090980cd1921cac083126f");
        result = instance.encodeAll();
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        dd = (double) ASN1Decoder.toASN1(ReadableBlock.wrap(result)).getValue();
        assertEquals(d, dd);

        d = 1398101.25;
        instance = new REAL(d);
        expResult = Bin.toByteArray("090580fe555555");
        result = instance.encodeAll();
        System.out.println("d:" + d);
        System.out.println("long:" + Double.doubleToRawLongBits(d));
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        REAL real = (REAL) ASN1Decoder.toASN1(ReadableBlock.wrap(expResult));
        Double v = (Double)real.getValue();
        System.out.println("v:" + v);
        System.out.println("long:" + Double.doubleToRawLongBits(v));
        
        assertEquals(d, v);

        d = 0.0625;
        instance = new REAL(d);
        expResult = Bin.toByteArray("090380fc01");
        result = instance.encodeAll();
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        real = (REAL) ASN1Decoder.toASN1(ReadableBlock.wrap(expResult));
        v = (Double)real.getValue();
        assertEquals(d, v);
    }
    
    @Test
    public void testEncodeInfinity() {
        System.out.println("REAL encodeBody Infinity");
        double d = 1.0/0.0;
        REAL instance = new REAL(d);
        byte[] expResult = Bin.toByteArray("090140");
        byte[] result = instance.encodeAll();
//        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        REAL real = (REAL) ASN1Decoder.toASN1(ReadableBlock.wrap(expResult));
        Double v = (Double)real.getValue();
        assertEquals(d, v);

        d = -1.0/0.0;
        instance = new REAL(d);
        expResult = Bin.toByteArray("090141");
        result = instance.encodeAll();
//        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        real = (REAL) ASN1Decoder.toASN1(ReadableBlock.wrap(expResult));
        v = (Double)real.getValue();
        assertEquals(d, v);
    }
    
    @Test
    public void testEncodeNaN() {
        System.out.println("REAL encodeBody NaN");
        float d = (float) (0.0 / 0.0);
        REAL instance = new REAL(d);
        byte[] expResult = Bin.toByteArray("090142");
        byte[] result = instance.encodeAll();
//        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        REAL real = (REAL) ASN1Decoder.toASN1(ReadableBlock.wrap(expResult));
        Double v = (Double)real.getValue();
        assertEquals(d, v);
    }

    @Test
    public void testEncodeZero() {
        System.out.println("REAL encodeBody Zero");
        double d = -0.0;
        REAL instance = new REAL(d);
        byte[] expResult = Bin.toByteArray("090143");
        byte[] result = instance.encodeAll();
//        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);
        
        REAL real = (REAL) ASN1Decoder.toASN1(ReadableBlock.wrap(expResult));
        Double v = (Double)real.getValue();
        assertEquals(d, v);

        d = 0.0;
        instance = new REAL(d);
        expResult = Bin.toByteArray("0900");
        result = instance.encodeAll();
//        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);

        real = (REAL) ASN1Decoder.toASN1(ReadableBlock.wrap(expResult));
        v = (Double)real.getValue();
        assertEquals(d, v);
    }

    /**
     * Test of encodeBody method, of class BigDecimalREAL.
     */
    @Test
    public void testEncodeBigBody() {
        System.out.println("encodeBigBody");
        BigDecimal bd = new BigDecimal("+1.0e+0");
        REAL instance = new REAL(bd);
        byte[] expResult = Bin.toByteArray("090603312e452b30");
        byte[] result = instance.encodeAll();
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of encodeBody method, of class BigDecimalREAL.
     */
    @Test
    public void testEncodeBigBody2() {
        System.out.println("encodeBigBody 3.14");
        BigDecimal d = BigDecimal.valueOf(3.14);
        System.out.println(d);
//        BigDecimal bd = new BigDecimal("+3140.e-3");
        REAL instance = new REAL(d);
        byte[] expResult = Bin.toByteArray("0907033331342e4532");
        byte[] result = instance.encodeAll();
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);
    }
}
