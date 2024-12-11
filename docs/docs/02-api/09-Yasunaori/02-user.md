---
description: 查询玩家信息的接口
---

# 玩家信息

## 接口路径

`/api/yasunaori/user`

## 请求参数

url参数:

- `uid` : 玩家的 osu!id
- `name`: 玩家的用户名
- `mode`: 游戏模式, 可选值为 `osu`, `taiko`, `fruits`, `mania`

:::warning[参数提示]

其中 `uid` 与 `name` 二者必选其一, 优先使用 `uid`

未提供 `mode` 时, 默认为玩家的主模式

:::

## 请求示例

```http request
GET /api/yasunaori/user?uid=2
```

```http request
GET /api/yasunaori/user?name=peppy&mode=mania
```

## 响应结果

- 正常响应:

```json
{
    "id": 1,
    "username": "Yasunaori",
    "avatar_url": "url",
    "country_code": "CN",
    "global_rank": 1,
    "country_rank": 1
}
```

- 错误响应:

```json
{
    "error": "User not found"
}
```
