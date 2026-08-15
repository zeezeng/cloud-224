# 在看（doseeing.com）数据 API 接口文档

> 数据源站点：`https://www.doseeing.com`，覆盖斗鱼 / 虎牙主播数据。
> 本文档所有返回结构均为**实测抓取**（房间 182102「国民大舅哥」，2026-08-15）。
> 金额单位说明：`price` 类字段单位均为 **分**（1 元 = 100 分），`score` 类字段单位通常为元。

---

## 一、通用说明

### 1.1 周期参数（dt / hours）

| 语义 | room_stat 的 `hours` 参数 | rank 等接口的 `dt` 参数 |
|---|---|---|
| 今日 | `today` | `0` |
| 昨日 | `yesterday` | `1` |
| 本月 | `thismonth` | **不支持**（rank 传 `thismonth` 恒返回 fail） |

### 1.2 Cookie / 会员限制

| 接口 | 是否需登录 Cookie | 是否需开通会员 |
|---|---|---|
| `/api/room_stat` | 否（公开） | 否 |
| `/data/api/rank` | **是**（Cookie 失效返回 `status=fail`） | 否（仅支持今日/昨日） |
| `/data/api/room_analzye` | 是 | 否 |
| `/data/api/room_gift_type_api` | 是 | 否 |
| `/data/api/topuser` | 是 | 否 |
| `/data/room/{rid}?type=gift&dt=thismonth` 页面 | 是 | **是**（需开通会员，未开通仅返回 4 个数字） |

> 关键结论：**本月（thismonth）数据只有 `/api/room_stat` 公开接口能拿全**，页面级本月数据需会员。这也是后端用 `room_stat?hours=thismonth` 同步本月的原因。

---

## 二、接口详情

### 2.1 `GET /api/room_stat` — 周期累计统计（公开，无 Cookie）

**用途**：获取主播指定周期的弹幕、礼物累计值，以及房间 / 公会信息。后端今日/昨日/本月同步均用此接口。

**请求**：

```
GET /api/room_stat?room={rid}&hours={today|yesterday|thismonth}
```

| 参数 | 必填 | 说明 |
|---|---|---|
| `room` | 是 | 斗鱼房间号 |
| `hours` | 是 | `today` / `yesterday` / `thismonth` |

**返回（实测，hours=today）**：

```json
{
  "stats": [
    {
      "rid": "182102",
      "chat.pv": 97741,            // 弹幕数（条）
      "chat.uv": 10063,            // 发弹幕人数
      "gift.paid.price": 5022730,  // 付费礼物价值（分）
      "gift.paid.uv": 147,         // 付费送礼人数
      "gift.all.price": 5647370,   // 全部礼物价值（分，含免费鱼丸等）
      "gift.all.uv": 798           // 送礼总人数
    }
  ],
  "room": {
    "rid": "182102",
    "nn": "国民大舅哥",            // 昵称
    "uid": 6431469,
    "ol": 6074112,                 // 在线人气（快照）
    "ts": 1786729803,              // 时间戳（秒）
    "rn": "三点s10.5赛季百人逃杀224熊掌", // 直播间标题
    "gonghui_id": "1z77WeB",       // 公会ID
    "gonghui_name": "熊掌文化",     // 公会名
    "gonghui_update_time": 1786671006,
    "cid_names": "户外,星秀,一起看,...", // 分类
    "category": { "124": 2364152 },      // 分类时长（秒）
    "av": "avatar_v3/...",         // 头像路径
    "rs1": "...",                  // 竖图
    "rs16": "..."                  // 封面图
  },
  "meta": { "count": 155, "unit": "minute" }
}
```

**meta 字段含义（重要）**：

| hours | meta 实测 | 含义 |
|---|---|---|
| `today` | `{count: 155, unit: minute}` | 距当日 0 点已过 155 分钟（随当前时刻变化），**非真实开播时长** |
| `yesterday` | `{count: 1440, unit: minute}` | 昨日完整 1440 分钟（一整天），**非真实开播时长** |
| `thismonth` | `{count: 15, unit: day}` | 本月已过 15 天，**非真实开播时长** |

> **room_stat 没有真实开播时长**：它的 `meta` 表示"周期已经过的时间"，不是主播实际直播时长。
> 真实开播时长需用 `rank` 接口的 `online.minutes`（见 2.2）。
> 公会信息（`gonghui_id` / `gonghui_name`）room_stat 直接返回，**无需 Cookie**。

---

### 2.2 `GET /data/api/rank` — 排名 / 开播时长（需 Cookie，仅今日/昨日）

**用途**：
1. 获取主播真实开播时长 `online.minutes`（覆盖 room_stat 的错误 meta）；
2. 校验登录 Cookie 是否有效（Cookie 失效返回 `status=fail`）。

**请求**：

```
GET /data/api/rank?rids={rid}&dt={0|1}&rank_type=chat_pv
```

| 参数 | 必填 | 说明 |
|---|---|---|
| `rids` | 是 | 斗鱼房间号 |
| `dt` | 是 | `0`=今日，`1`=昨日（不支持本月） |
| `rank_type` | 是 | `chat_pv` 弹幕排名（本系统用） |

**返回（实测，dt=0，携带有效 Cookie）**：

```json
{
  "status": "success",
  "result": {
    "result": [
      {
        "ts": 1786732443,
        "sdate": "2026-08-15T00:00:00",
        "rid": "182102",
        "chat.pv": 97739,             // 弹幕数
        "chat.uv": 10062,             // 弹幕人数
        "gift.all.price": 5647370,    // 全部礼物价值（分）
        "gift.all.uv": 798,
        "gift.paid.price": 5022730,   // 付费礼物价值（分）
        "gift.paid.uv": 147,
        "active.uv": 10394,           // 活跃人数
        "online.minutes": 111,        // ★ 真实开播时长（分钟）
        "start.fan": 940065,          // 期初粉丝数
        "end.fan": 940154,            // 期末粉丝数
        "start.dfrank": 874,          // 期初斗鱼等级
        "end.dfrank": 876,            // 期末斗鱼等级
        "start.frank": 0,             // 期初礼物排名
        "end.frank": 0,               // 期末礼物排名
        "room.gonghui": "熊掌文化",
        "room.nn": "国民大舅哥",
        "room.av": "avatar_v3/...",
        "room.category": { "124": 2364152 },
        "room.cid_names": "户外,星秀,...",
        "tuanbo_name": "国民大舅哥"
      }
    ]
  }
}
```

**Cookie 失效 / 未配置时返回**：

```json
{ "status": "fail", "result": {} }
```

> 后端 `DoseeingClient.checkCookieStatus()` 正是用"有效 Cookie 返回 success+数据、失效返回 fail"这一特性做 Cookie 有效性探测（`dt=0`）。

---

### 2.3 `GET /data/api/room_analzye` — 礼物 / 弹幕金额区间分布（需 Cookie）

**用途**：页面「弹幕 / 礼物」趋势分析图的底层数据，按金额区间统计。

**请求**：

```
GET /data/api/room_analzye?rids={rid}&dt={0|1}&rank_type={gift|chat}
```

| 参数 | 必填 | 说明 |
|---|---|---|
| `rids` | 是 | 房间号 |
| `dt` | 是 | `0` / `1` |
| `rank_type` | 是 | `gift`=礼物区间，`chat`=弹幕区间 |

**返回（实测，rank_type=gift）**：

```json
{
  "status": "success",
  "result": [
    { "bounder": ">=50000",  "pv": 600410,  "uv": 9 },
    { "bounder": ">=100000", "pv": 3727610, "uv": 14 },
    { "bounder": ">=10000",  "pv": 647520,  "uv": 31 },
    { "bounder": ">=1000",   "pv": 39370,   "uv": 21 },
    { "bounder": ">0",       "pv": 7820,    "uv": 72 }
  ]
}
```

| 字段 | 说明 |
|---|---|
| `bounder` | 单笔礼物金额门槛（如 `>=50000` 表示单笔 ≥ 500 元的礼物） |
| `pv` | 该区间累计价值（分） |
| `uv` | 该区间人数 |

---

### 2.4 `GET /data/api/room_gift_type_api/{rid}` — 礼物类型明细（需 Cookie）

**用途**：页面「礼物类型」分布，列出收到的每种礼物数量与价值。

**请求**：

```
GET /data/api/room_gift_type_api/{rid}?dt={0|1}
```

**返回（实测，dt=0，截取前几条）**：

```json
{
  "result": [
    { "gfid": "21621", "count": 209, "paid_price": 2090000, "name": "魔法飞机", "img": "https://gfs-op.douyucdn.cn/..." },
    { "gfid": "21995", "count": 2,   "paid_price": 1000000, "name": "云端之城", "img": "..." },
    { "gfid": "d1",    "count": 2,   "paid_price": 31600,   "name": "开通钻粉1个月", "img": "..." }
  ]
}
```

| 字段 | 说明 |
|---|---|
| `gfid` | 礼物 ID |
| `name` | 礼物名 |
| `count` | 收到数量 |
| `paid_price` | 该礼物付费价值（分） |
| `img` | 礼物图标 |

---

### 2.5 `GET /data/api/topuser/{rid}` — 贡献榜（需 Cookie）

**用途**：页面「送礼 / 弹幕贡献榜」。

**请求**：

```
GET /data/api/topuser/{rid}?type={gift|chat}&dt={0|1}
```

**返回（实测，type=gift，截取前几条）**：

```json
{
  "code": 0,
  "msg": "",
  "count": 50,
  "data": [
    {
      "uid": "766887473",
      "rank": 1,
      "score": "7230",                  // 贡献值（元）
      "gift.paid.price": 723000,        // 付费礼物价值（分）
      "gift.all.price": 723000,         // 全部礼物价值（分）
      "gift.paid.price.total": 873490,  // 历史累计付费（分）
      "chat.pv": 9,                     // 弹幕数
      "user.nickname": "寂灭、",
      "user.avatar": "avatar_v3/...",
      "user.level": "59",
      "user.nl": "5",                   // 贵族等级
      "user.sl": "50",                  // 守护等级
      "user.paid": { "rank": 66, "v": 22492420, "ts": 1786723200 }
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| `code` / `msg` | `0` 成功 |
| `count` | 返回条数 |
| `data[]` | 用户贡献明细，`rank` 名次，`score` 贡献值（元） |

---

### 2.6 其它（页面辅助接口）

| 接口 | 用途 |
|---|---|
| `GET /api/suggest_all?type=` | 主播搜索建议（添加主播时联想） |
| `GET /data/api/room_point/{rid}?type=` | 数据点 / 走势明细 |
| `GET /data/api/follow` / `POST /data/api/unfollow` | 登录用户关注 / 取关 |

---

## 三、时长数据说明（重点）

### 3.1 room_stat 有"时长"吗？

room_stat 的 `meta.count` + `meta.unit` 表面像时长，但实测语义是**周期已经过的时间**：

| hours | meta | 说明 |
|---|---|---|
| `today` | `count=155, unit=minute` | 当前时刻距当日 0 点分钟数（随时间递增，**不是开播时长**） |
| `yesterday` | `count=1440, unit=minute` | 一整天 1440 分钟（**不是开播时长**） |
| `thismonth` | `count=15, unit=day` | 本月已过天数（**不是开播时长**） |

所以：**room_stat 没有可用的真实开播时长**。

### 3.2 页面显示的开播时长来自 rank

`https://www.doseeing.com/data/room/182102?type=gift&dt=0` 页面实测引用的接口清单：

```
/data/api/rank?rids=182102&dt=0&rank_type=chat_pv   ← 排名 + 开播时长（online.minutes）
/data/api/room_analzye?rids=182102&dt=0&rank_type=chat
/data/api/room_analzye?rids=182102&dt=0&rank_type=gift
/data/api/room_gift_type_api/182102?dt=0
/data/api/topuser/182102?type=chat&dt=0
/data/api/topuser/182102?type=gift&dt=0
/data/api/room_point/182102?type=
```

页面**没有调用 room_stat**，其"开播时长"字段的数据源正是 `/data/api/rank` 的 `online.minutes`。
（这与后端修正逻辑一致：fc-probe 抓 `rank` 的 `online.minutes` 覆盖 room_stat 的 meta，从而得到真实开播时长。）

---

## 四、本系统（mars）实际用到的接口

| 用途 | 接口 | 是否需 Cookie |
|---|---|---|
| 今日/昨日/本月 礼物+弹幕累计 | `/api/room_stat?hours=...` | 否 |
| 今日/昨日 真实开播时长 | `/data/api/rank?dt=0/1`（经 fc-probe 代理） | 是 |
| 本月开播时长 | 无公开可用来源（需会员），显示 `-` | — |
| Cookie 有效性校验 | `/data/api/rank?dt=0`（`DoseeingClient.checkCookieStatus`） | 是 |
| 虎牙公会信息 | `/huya/data/room/{rid}` 页面解析 | 是 |
| 主播搜索联想 | `/api/suggest_all?type=` | 否 |
