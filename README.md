# SoftLibASN1

## 概要

ASN.1 の情報を読み書きしたいライブラリです
PEM,DERが使えればいいかな
BER,CERはdecodeのみ対応?未定
独自XMLとの相互変換にも対応しています (XERではありません)
Rebind に対応できそうなので1.1.0-SNAPSHOTで対応中

情報の少ないREAL型に対応してみた (double 2進, BigDecimal 10進)

SoftLibPKI からASN1関連を分けたもの。SoftLibCrypto との依存関係調整。

1.1.0 では SoftLibRebind に対応する予定

## Maven

net.siisise
softlib-asn1
1.0.3

リリース版 1.0.3

次版 1.1.0-SNAPSHOT