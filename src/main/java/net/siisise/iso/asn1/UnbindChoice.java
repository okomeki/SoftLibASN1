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
package net.siisise.iso.asn1;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import net.siisise.bind.TypeUnbind;
import net.siisise.bind.format.TypeFormat;
import net.siisise.bind.unbind.java.UnbindObject;
import net.siisise.iso.asn1.annotation.Choice;

/**
 * Choice型のMapに
 */
public class UnbindChoice implements TypeUnbind {

    static final Type[] def = { Object.class };

    public UnbindChoice() {
    }

    @Override
    public Type[] getSrcTypes() {
        return def;
    }

    @Override
    public <T> T valueOf(Object src, TypeFormat<T> format) {
        
        if (src != null) {
            Class cls = src.getClass();
            Annotation ch = cls.getAnnotation(Choice.class);
            if (ch != null) {
                LinkedHashMap<String, Object> map = UnbindObject.fieldsToMap(src);
                return format.enumFormat(map);
            }
        }
        return (T)this;
    }
    
}
