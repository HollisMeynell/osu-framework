---
sidebar_position: 1.5
title: "公开接口"
description: "无需登陆即可访问的接口"
---

## 公开接口列表

| module | title                                             | url                                 |
|:-------|:--------------------------------------------------|:------------------------------------|
| 登陆     | [获取登陆连接](./login/02-get-url.md)                   | `/api/public/getOauthUrl`           |
| 登陆     | [登陆接口](./login/03-login.md)                       | `/api/user/login`                   |
| 镜像站    | [谱面文件名](./mirror/01-beatmap-name.md)              | `/api/mirror/fileName/{type}/{bid}` |
| 镜像站    | [下载谱面文件内容](./mirror/02-beatmap-file.md)           | `/api/mirror/beatmap/{type}/{bid}`  |
| 镜像站    | [下载 `.osz` 包](./mirror/03-beatmapset-download.md) | `/api/mirror/beatmapset/{sid}`      |
| 镜像站    | [打包下载谱面文件](./mirror/04-beatmapset-package.md)     | `/api/mirror/beatmapset/all`        |
