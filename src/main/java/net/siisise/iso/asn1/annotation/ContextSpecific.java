/*
 * Copyright 2025 okome.
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
package net.siisise.iso.asn1.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import net.siisise.iso.asn1.ASN1Cls;

/**
 * ASN.1 用Java annotation.
 * Explicit / Implicit のタグにするかも
 */
@Target(ElementType.FIELD)
//@Retention(RetentionPolicy.RUNTIME)
public @interface ContextSpecific {
    ASN1Cls cls() default ASN1Cls.CONTEXT_SPECIFIC;
    int tag();
}
