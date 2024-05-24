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

module net.siisise.asn {
    requires java.logging;
    requires java.xml;
    requires net.siisise;
    requires net.siisise.abnf;
    requires net.siisise.abnf.rfc;
    requires net.siisise.rebind;
    requires net.siisise.xml;
    exports net.siisise.iso.asn1;
//    exports net.siisise.iso.asn1.module;
//    exports net.siisise.iso.asn1.parser;
    exports net.siisise.iso.asn1.tag;
}
