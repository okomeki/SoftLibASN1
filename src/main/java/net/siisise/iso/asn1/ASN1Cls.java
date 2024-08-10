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

/**
 * ASN.1 Class.
 */
public enum ASN1Cls {
    UNIVERSAL(0), // UNIVERSAL Universal
    APPLICATION(1), // APPLICATION Application 例: [Application 3] 0x43 単体 0x67 SEQUENCE? 
    CONTEXT_SPECIFIC(2), // コンテキスト特定 / 文脈固有 / Context-specific [2] 0x82 単体 0xA2 SEQUENCE?
    PRIVATE(3); // 私用 Private

    final byte cls;

    ASN1Cls(int c) {
        cls = (byte) c;
    }
    
    public static ASN1Cls valueOf(int id) {
        if (id >= 0x4) {
            return null;
        }
        return ASN1Cls.values()[id];
    }
}
