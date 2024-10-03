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

import java.nio.charset.StandardCharsets;
import java.util.Date;
import net.siisise.iso.asn1.ASN1;

/**
 * ASN.1 GeneralizedTime.
 * とりあえず文字列として扱う.
 * TimeZoneは省略
 * 
 */
public class GeneralizedTime extends ASN1String {

    public GeneralizedTime(ASN1 id) {
        super(id);
    }

    public GeneralizedTime() {
        super(ASN1.GeneralizedTime);
    }
    
    /**
     * 時刻設定.
     * 
     * @param date Date 精度ミリ秒
     */
    public void setDate(Date date) {
        setTimestamp(date.getTime());
    }

    public void setTimestamp(long ts) {
        throw new UnsupportedOperationException();
    }
    
    public long getTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decodeBody( byte[] val ) {
//        data = (byte[]) val.clone();
        switch ( ASN1.valueOf(getId()) ) {
        case UTCTime:
        case GeneralizedTime:
            string = new String(val, StandardCharsets.UTF_8);
            break;
        default:
            throw new UnsupportedOperationException( "Unknown GeneralizedTime " + getId() + " yet.");
        }
    }
}
