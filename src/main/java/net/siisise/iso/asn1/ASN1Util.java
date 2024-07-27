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
package net.siisise.iso.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.siisise.block.ReadableBlock;
import net.siisise.xml.TrXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XML系を中心に分離
 * ASN1 Object
 * BIN ASN1 binaly
 * XML XML Object
 */
public class ASN1Util {

    public static ASN1Object BERtoASN1(byte[] src) {
        return new ASN1X690BER().decode(ReadableBlock.wrap(src));
    }

    public static ASN1Object toASN1(byte[] src) throws IOException {
//        return ASN1Decoder.toASN1( new ByteArrayInputStream(src));
        return toASN1(ReadableBlock.wrap(src));
    }

    public static ASN1Object toASN1(ReadableBlock block) {
        return new ASN1X690DER().decode(block);
    }

    /**
     * ASN1 → XMLObj
     * XER ではない
     *
     * @param top
     * @return ASN.1 Original XML format.
     * @throws ParserConfigurationException
     */
    public static Document toXML(ASN1Object top) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return toXML(top, builder);
    }

    /**
     * ITU-T X.693 / ISO/IEC 8825-4 にしたい?
     * XER ではない
     *
     * @param top
     * @param builder
     * @return ASN.1 Original XML format.
     */
    public static Document toXML(ASN1Object top, DocumentBuilder builder) {
        Document doc = builder.newDocument();
        Element ele = top.encodeXML(doc);
        doc.appendChild(ele);
        return doc;
    }

    /**
     * XMLObj → XMLtext
     * 単純なXMLのテキスト化
     *
     * @param doc
     * @return
     * @throws TransformerException
     */
    public static String toString(Document doc) throws TransformerException {
        return TrXML.plane(doc);
    }

    /**
     * XML text
     * @param top
     * @return XML text
     * @throws ParserConfigurationException
     * @throws TransformerException 
     */
    public static String toXMLString(ASN1Object top) throws ParserConfigurationException, TransformerException {
        return toString(toXML(top));
    }

    /**
     * XMLObj → ASN1
     *
     * @param doc XML Document
     * @return ASN.1 Object
     */
    public static ASN1Object toASN1(Document doc) {
        Element ele = doc.getDocumentElement();
        return toASN1(ele);
    }

    /**
     * XMLObj → ASN1
     *
     * @param ele root 要素 (独自)
     * @return ASN.1 Object
     */
    public static ASN1Object toASN1(Element ele) {
        ASN1Object root;

        // tag to object
        String tagName = ele.getTagName();
        ASN1 t;
        if ("struct".equals(tagName)) {
            //String c = ele.getAttribute("class");
            String tag = ele.getAttribute("tag");
            root = new ASN1Struct((byte) 2, new BigInteger(tag));
        } else {
            t = ASN1.valueOf(tagName);
            String struct = ele.getAttribute("struct");
            if (struct != null && Boolean.parseBoolean(struct)) {
                root = new ASN1Struct((byte) 0, t.tag);
            } else {
                root = ASN1Decoder.decodeTag(t.tag);
            }
        }

        // toASN1
        root.decodeXML(ele);

        return root;
    }

    public static List<ASN1Object> toASN1List(InputStream in) throws IOException {
        List<ASN1Object> asnobjs = new ArrayList<>();
        while (in.available() > 0) {
//            System.out.println("available:" + in.available());
            asnobjs.add(ASN1Decoder.toASN1(in));
        }
        return asnobjs;
    }

    public static List<ASN1Object> toASN1List(byte[] src) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        List<ASN1Object> ao;
        ao = toASN1List(in);
        in.close();
        return ao;
    }

}
