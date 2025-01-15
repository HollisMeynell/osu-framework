---
title: "下载谱面*"
description: "下载 beatmapset 的 .osz 文件"
---

# 下载谱面文件

## 接口路径

`/api/mirror/beatmapset/{sid}`

## 请求类型

- `GET`

## 请求参数

path 参数:

- `sid`* : 谱面 beatmapset id, 必选

url 参数:

- `video` : 是否下载视频文件, 仅支持 `true` / `false`, 默认为 `true`, 可选

## 请求示例

```http request
GET /api/mirror/beatmapset/2?video=false
```

## 响应结果

二进制响应流, 无任何其他包装, 出错时返回[单体数据响应](../README.md#单体数据响应)格式

:::warning

是纯二进制, 并非 base64 编码!

:::
