---
title: "接口约定"
description: "接口列表"
---

:::warning

下面所有接口都是针对 [osu 成绩封面生成器](https://a.yasunaori.be/osu-score-cover-generator)

:::

# 接口定义

## 请求格式

所有接口通用前缀 `/api/yasunaori/`,  所有请求均采用 `GET` 方法, 不允许跨域请求.

## 接口列表

| 接口               | 描述     |                       |
|------------------|--------|-----------------------|
| `/user`          | 获取玩家信息 | [详情](./02-user.md)    |
| `/beatmap/{bid}` | 获取谱面信息 | [详情](./03-beatmap.md) |


## 响应结构

下面是接口响应结果的定义

```mermaid
classDiagram
class UserInfo {
    string error?
    int id
    string username
    string avatar_url
    string country_code
    int global_rank
    int country_rank
}

class BeatmapInfo {
    string error?
    int id
    string title
    string titleUnicode
    string artist
    string artistUnicode
    string creator
    string coverUrl
    string status
    string mode
    Stats stats
    Difficulty difficulty
}

class Stats {
    double ar
    double cs
    double hp
    double od
    double bpm
    int length
}

class Difficulty {
    double star
    string name
}

BeatmapInfo ..> Stats
BeatmapInfo ..> Difficulty
```
