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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import net.siisise.iso.asn1.ASN1;

/**
 * YYMMDDHHMMSSZ.
 * 内部long型 epoc ミリ秒
 * Date, Instant と変換可能にする
 * とりあえずX.509用対応.
 * X.509 では 2050年以前で使用
 * UTCTime ::= [UNIVERSAL 23] IMPLICIT VisibleString
 */
public class UTCTime extends ASN1String {

    static final ZoneId zone = ZoneId.of("Z");
    static final TimeZone tz = TimeZone.getTimeZone(zone);
    
    static final String YYDATETIME = "yyMMddHHmmss";

    public UTCTime() {
        super(ASN1.UTCTime);
    }

    /*
    public UTCTime(ASN1Cls cls, BigInteger tag) {
        super(cls, tag);
    }
    */
    
    public UTCTime(ASN1 id) {
        super(id);
    }
    
    public UTCTime(ASN1 id, String date) {
        super(id, date);
        from(toDate(date));
    }
    
    public UTCTime(String date) {
        this(ASN1.UTCTime, date);
    }

    public UTCTime(ASN1 id, long date) {
        super(id);
        from(date);
    }
    
    public UTCTime(ASN1 id, GeneralizedTime date) {
        super(id);
        string = date.getValue().substring(2);
    }
    
    public UTCTime(GeneralizedTime date) {
        this(ASN1.UTCTime, date);
    }

    public UTCTime(long date) {
        this(ASN1.UTCTime, date);
    }

    /**
     * ミリ秒に変換.
     * java.time.Instant の真似.
     * @return エポックミリ秒
     */
    public long toEpocMilli() {
        return toDate(string).getTime();
    }

    /**
     * 範囲が不明.
     * @return 
     */
    public Date toDate() {
        Date date = new Date();
        date.setTime(toEpocMilli());
        return date;
    }

    /**
     * それっぽいのを返す.
     * @return 
     */
    public Instant toInstant() {
        return Instant.ofEpochMilli(toEpocMilli());
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
        string = toString(date);
    }

    public void from(Instant instant) {
        from(instant.toEpochMilli());
    }

    /**
     * 仮.
     * 
     * @deprecated 正確な年が出せない
     * @param date
     * @return 
     */
    @Deprecated
    public static String toString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(YYDATETIME);
        format.setTimeZone(tz);
        String v = format.format(date);
        return v + "Z";
    }

    /**
     * ToDo: X.509 では 1950年 から 2049年まで
     * 
     * @param utc TZ Zのみ
     * @param base 中央日付
     * @return 
     */
    public static Date toDate(String utc, Date base) {
        String[] t = utc.split("\\.",3);
        if ( t.length > 2 ) {
            throw new java.time.format.DateTimeParseException("不明",utc, t[0].length() + t[1].length() + 1);
        } if ( t.length == 2) {
            if ( t[0].length() != 12) {
                throw new java.time.format.DateTimeParseException("長さ",utc, t[0].length());
            }
            int l = t[1].length();
            String tmp = t[1].substring(0,l) + "000";
            tmp = tmp.substring(0,4);
            
            throw new java.lang.IllegalStateException("まだない");
        }
        SimpleDateFormat format = new SimpleDateFormat(YYDATETIME);
        format.setTimeZone(tz);
        
        try {
            return format.parse(utc);
        } catch (ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    public static Date toDate(String utc) {
        return toDate(utc, new Date());
    }

    @Override
    public void setValue(String date) {
        from(toDate(date));
    }
    
}
