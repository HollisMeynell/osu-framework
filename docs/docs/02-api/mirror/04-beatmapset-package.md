---
title: "打包下载多个谱面"
description: "下载包含多个 beatmapset.osz 文 .zip 文件"
---

# 打包下载谱面文件

## 接口路径

`/api/mirror/beatmapset/all`

## 请求参数

url 参数:

- `sid`* : 谱面 beatmapset id 列表, 支持多个, 必选
- `video` : 是否下载视频文件, 仅支持 `true` / `false`, 默认为 `true`, 可选

## 请求示例

```http request
GET /api/mirror/beatmapset/all?sid=2&sid=3&sid=4
```

## 响应结果

二进制响应流, 无任何其他包装, 出错时返回[单体数据响应](../README.md#单体数据响应)格式

:::warning

是纯二进制, 并非 base64 编码!

:::
