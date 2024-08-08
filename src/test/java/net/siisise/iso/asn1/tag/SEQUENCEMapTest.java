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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import net.siisise.iso.asn1.ASN1Tag;
import net.siisise.iso.asn1.ASN1Util;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class SEQUENCEMapTest {
    
    public SEQUENCEMapTest() {
    }

    /**
     * Test of put method, of class SEQUENCEMap.
     */
    @Test
    public void testA() {
        System.out.println("put");
        String key = "a";
        BigInteger val = BigInteger.valueOf(7);
        SEQUENCEMap instance = new SEQUENCEMap();
        instance.put(key, val);
        byte[] enc = instance.encodeAll();
        ASN1Tag n = ASN1Util.toASN1(enc);
        try {
            System.out.println(ASN1Util.toString(ASN1Util.toXML(n)));
        } catch (TransformerException ex) {
            Logger.getLogger(SEQUENCEMapTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
