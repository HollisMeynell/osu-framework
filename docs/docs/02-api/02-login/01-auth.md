---
title: "登陆认证"
---

# 登陆认证

## 认证方式

使用 JWT 进行认证, 通过登陆后获得的 token, 在请求头中加入 `Authorization` 字段, 值为 `Bearer ${token}` 进行认证.

:::warning

大部分接口需要登陆后才能访问

部分接口即使登陆后也无法访问

公开接口有严格的请求频率限制

::: 