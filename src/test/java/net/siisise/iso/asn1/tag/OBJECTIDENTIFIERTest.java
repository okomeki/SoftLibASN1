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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class OBJECTIDENTIFIERTest {
    
    public OBJECTIDENTIFIERTest() {
    }

    /**
     * Test of getValue method, of class OBJECTIDENTIFIER.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        OBJECTIDENTIFIER instance = new OBJECTIDENTIFIER("2.16.840.1.101.3.4.1.1");
        String expResult = "2.16.840.1.101.3.4.1.1";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of sub method, of class OBJECTIDENTIFIER.
     */
    @Test
    public void testSub_StringArr() {
        System.out.println("sub");
        String[] id = {"4.1"};
        OBJECTIDENTIFIER instance = new OBJECTIDENTIFIER("2.16.840.1.101.3");
        OBJECTIDENTIFIER expResult = new OBJECTIDENTIFIER("2.16.840.1.101.3.4.1");
        OBJECTIDENTIFIER result = instance.sub(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of up method, of class OBJECTIDENTIFIER.
     */
    @Test
    public void testUp() {
        System.out.println("up");
        OBJECTIDENTIFIER instance = new OBJECTIDENTIFIER("2.16.840.1.101.3.4.1");
        OBJECTIDENTIFIER expResult = new OBJECTIDENTIFIER("2.16.840.1.101.3.4");
        OBJECTIDENTIFIER result = instance.up();
        assertEquals(expResult, result);
    }
    
}
