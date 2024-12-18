---
title: "下载谱面内容文件"
description: "获取在 beatmap 中某个类型的文件, 是实际文件数据"
---

# 下载谱面内容

## 接口路径

`/api/mirror/beatmap/{type}/{bid}`

## 请求参数

path 参数:

- `type`* : 文件类型, 可选值为 `bg`, `song`, `osufile`, 必选
- `bid`* : 谱面 beatmap id, 必选

## 响应结果

二进制响应流, 无任何其他包装, 出错时返回[单体数据响应](../01-README.md#单体数据响应)格式

:::warning

是纯二进制, 并非 base64 编码!

:::
