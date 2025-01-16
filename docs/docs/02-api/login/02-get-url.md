---
title: "获取登陆连接*"
---

# 获取登陆连接

## 接口路径

`/api/public/getOauthUrl`

## 请求类型

- `GET`

## 请求参数

无

## 响应结果

响应结果为单体数据, data 为一个字符串, 为 osu 登陆的 url

```json
{
  "code": 200,
  "message": "ok",
  "data": "url"
}
```

## 如何登陆

直接跳转至响应结果中的 data 字符串的连接, 等待回调访问,
成功登陆后 osu 网站会将 `code` 添加到 url 参数中,
通过这个 `code` 调用[登陆接口](./03-login.md)获取 token 

