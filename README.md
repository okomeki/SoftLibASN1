# SoftLibASN1

## 概要

ASN.1 の情報を読み書きしたいライブラリです
PEM,DERが使えればいいかな
BER,CERはdecodeのみ対応?未定
独自XMLとの相互変換にも対応しています (XERではありません)
Rebind に対応?

情報の少ないREAL型に対応してみた (double 2進, BigDecimal 10進)

SoftLibPKI からASN1関連を分けたもの。SoftLibCrypto との依存関係調整。

## Maven

Java Module System JDK 11用
~~~
<dependency>
    <groupId>net.siisise</groupId>
    <artifactId>softlib-asn1.module</artifactId>
    <version>1.1.0</version>
    <type>jar</type>
</dependency>
~~~
JDK 8用
~~~
<dependency>
    <groupId>net.siisise</groupId>
    <artifactId>softlib-asn1</artifactId>
    <version>1.1.0</version>
    <type>jar</type>
</dependency>
~~~

リリース版 1.1.0

次版 1.1.1-SNAPSHOT
