---
title: "获取谱面文件名"
description: "获取在 beatmap 中某个类型的文件名称, 例如获取背景文件 `bg.jpg` 等"
---

# 谱面文件名

## 接口路径

`/api/mirror/fileName/{type}/{bid}`

## 请求参数

path 参数:

- `type`* : 文件类型, 可选值为 `bg`, `song`, `osufile`, 必选
- `bid`* : 谱面 beatmap id, 必选

## 响应结果

纯字符串, 无任何其他包装, 出错时返回[单体数据响应](../README.md#单体数据响应)格式
