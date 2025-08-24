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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import net.siisise.iso.asn1.ASN1;

/**
 * ASN.1 GeneralizedTime.
 * とりあえず文字列として扱う.
 * TimeZoneは省略
 * 
 */
public class GeneralizedTime extends ASN1String {

    static final ZoneId zone = ZoneId.of("Z");
    static final TimeZone tz = TimeZone.getTimeZone(zone);

    static final String YYYYDATETIMEZ = "yyyyMMddHHmmssZ";

    public GeneralizedTime(ASN1 id) {
        super(id);
    }

    public GeneralizedTime() {
        super(ASN1.GeneralizedTime);
    }
    
    public GeneralizedTime(long time) {
        super(ASN1.GeneralizedTime);
        from(time);
    }

    public GeneralizedTime(String date) {
        super(ASN1.GeneralizedTime);
        setValue(date);
    }
    
    /**
     * 時刻設定.
     * 
     * @param date Date 精度ミリ秒
     */
    public void setDate(Date date) {
        from(date);
    }

    /**
     * 設定.
     * @param ts エポックからのミリ秒
     */
    public void setTimestamp(long ts) {
        from(ts);
    }
    
    public long getTimestamp() {
        return toEpochMilli();
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

    /**
     * ミリ秒に変換.
     * java.time.Instant の真似.
     * @return エポックミリ秒
     */
    public long toEpochMilli() {
        return toDate(string).getTime();
    }
    
    /**
     * それっぽいのを返す.
     * @return 
     */
    public Instant toInstant() {
        return Instant.ofEpochMilli(toEpochMilli());
    }

    /**
     * Date型と同じ
     * @param date 
     */
    public void from(long date) {
        Date da = new Date();
        da.setTime(date);
        string = toString(da);
    }

    public void from(Date date) {
        from(date.getTime());
    }

    public void from(Instant instant) {
        from(instant.toEpochMilli());
    }

    static String toString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(YYYYDATETIMEZ);
        format.setTimeZone(tz);
        return format.format(date);
    }

    static Date toDate(String utc) {
        SimpleDateFormat format = new SimpleDateFormat(YYYYDATETIMEZ);
        format.setTimeZone(tz);
        
        try {
            return format.parse(utc);
        } catch (ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void setValue(String date) {
        from(toDate(date));
    }
}
