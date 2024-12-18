---
description: "查询谱面信息, 支持传递 mod"
---

# 谱面信息

## 接口路径
    
`/api/yasunaori/beatmap/{bid}`

## 请求参数

path 参数:

- `bid`* : 谱面的 beatmap id, 必选

url 参数:

- `mods`: 指定 mod 后的信息, 使用 `HDHR` 格式的字符串, 可选
- `mode`: 游戏模式, 如果谱面可以转换为对应模式则响应转换后的数据,
          可选值为 `osu`, `taiko`, `fruits`, `mania`, 可选

## 请求示例

```http request
GET /api/yasunaori/beatmap/1
```

```http request
GET /api/yasunaori/beatmap/1?mods=hddt&mode=osu
```

## 响应结果

- 正常响应:
```json
{
    "id": 1,
    "title": "Title",
    "title_unicode": "标题",
    "artist": "Artist",
    "artist_unicode": "艺术家",
    "creator": "Mapper Name",
    "cover_url": "url",
    "status": "ranked",
    "mode": "osu",
    "stats": {
        "length": 60,
        "bpm": 120.0,
        "cs": 4.0,
        "ar": 4.0,
        "od": 4.0,
        "hp": 4.0
    },
    "difficulty": {
        "star": 4.0,
        "name": "Difficulty Name"
    }
}
```

- 错误响应:

```json
{
    "error": "User not found"
}
```
