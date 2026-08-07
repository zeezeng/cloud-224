# 在看（doseeing）数据 API 接口文档

整理时间：2026-08-08  
来源页面：https://www.doseeing.com/room/182102 （"在看直播排行榜"，斗鱼主播实时数据站）  
页面结构：房间页为 SSR 渲染 + AJAX 局部加载（`/api/*`），数据中心页（`/data/room/{rid}`）与火箭榜（`/api/ruran`）需要登录；但主播基础资料与主播流水收入同步所需的 `room_stat` 公开可用。

说明：本文只整理主播数据查询相关接口。`room_stat`、`room_dots`、`suggest_all` 三个只读 GET 接口已实测可用；需登录接口未实测（未登录时返回 302 跳 `/login`）。

## 1. 基础约定

站点域名：

```text
https://www.doseeing.com
```

请求建议：带浏览器 UA 与 `Referer: https://www.doseeing.com/room/{rid}`（部分接口偶发 502，重试即可）。

### 1.1 时间窗口参数 `hours`

三个核心接口共用，房间页切换按钮对应关系：

| hours 值 | 页面含义 | meta.unit | 实测 meta.count |
| --- | --- | --- | --- |
| `1` | 最近 1 小时 | minute | 60 |
| `8` | 最近 8 小时 | minute | - |
| `24` | 最近 24 小时 | minute | - |
| `today` | 今天（当日 0 点起） | minute | 当天已过分钟数（实测 103） |
| `yesterday` | 昨天 | minute | 1440 |
| `7day` | 近 7 天 | day | 7 |
| `30day` | 近 30 天 | day | 30（需权限） |
| `thismonth` | 本月 | day | 实测公开可用 |

`30day` 在页面显示"需要权限"；`thismonth` 前端按钮也归入权限提示，但实测 `GET /api/room_stat?room=182102&hours=thismonth` 仍可公开返回本月聚合统计，可用于后台本月同步。若后续站点收紧权限，应回退播酱月报。

### 1.2 金额单位

`gift.paid.price`、`gift.all.price` 单位为**分**，前端展示时除以 100（如 `31717350` → 317173.5 元）。`room_dots.gift[]` 前端除以 10 后展示为 LW 值。

## 2. 主播统计（核心接口，已实测）

### 2.1 房间统计

```http
GET /api/room_stat?room={rid}&hours={hours}
```

用途：单个主播在指定时间窗内的弹幕/礼物/热度统计 + 房间实时信息，一次调用拿全。

请求参数：

| 参数 | 类型 | 示例 | 说明 |
| --- | --- | --- | --- |
| `room` | number/string | `182102` | 斗鱼房间号 |
| `hours` | string | `today` | 时间窗口，见 1.1 |

返回结构（实测）：

```json
{
  "stats": [
    {
      "rid": "182102",
      "chat.pv": 27817,           // DM值：弹幕总量
      "chat.uv": 4328,            // 弹幕人数
      "gift.paid.price": 31717350, // SR值：付费礼物金额（分，/100=元）
      "gift.paid.uv": 404,        // 付费送礼人数
      "gift.all.price": 32244370, // LW值：全部礼物金额（分）
      "gift.all.uv": 875          // 送礼人数
    }
  ],
  "room": {
    "rid": "182102",
    "nn": "国民大舅哥",           // 主播昵称
    "uid": 6431469,              // 斗鱼 uid
    "av": "avatar_v3/202105/...", // 头像路径
    "rn": "下午四点前",            // 直播间标题
    "rs1": "https://rpic.douyucdn.cn/...",   // 封面图
    "rs16": "https://rpic.douyucdn.cn/...",
    "ol": 4235520,              // 当前在线热度
    "ts": 1786124522,           // 时间戳
    "category": { "124": 2354292, "181": 120 },  // 分类热度分布 {cid: 热度}
    "cid_names": "户外,星秀,一起看,颜值（横屏）,...", // 分类名列表
    "gonghui_id": "1z77WeB",
    "gonghui_name": "熊掌文化"    // 公会
  },
  "meta": { "count": 60, "unit": "minute" }
}
```

`stats[0]` 字段表：

| 字段 | 页面名 | 含义 |
| --- | --- | --- |
| `chat.pv` | DM值 | 弹幕总量 |
| `chat.uv` | - | 弹幕人数 |
| `gift.paid.price` | SR值 | 付费礼物金额（分） |
| `gift.paid.uv` | - | 付费送礼人数 |
| `gift.all.price` | LW值 | 礼物总值（分，含鱼丸等） |
| `gift.all.uv` | - | 送礼人数 |

`room` 字段表：

| 字段 | 含义 |
| --- | --- |
| `nn` | 主播昵称 |
| `uid` | 斗鱼 uid |
| `rn` | 直播间标题 |
| `ol` | 当前在线热度 |
| `category` | 分类热度分布 `{分类id: 热度}` |
| `cid_names` | 分类名（逗号分隔） |
| `gonghui_id` / `gonghui_name` | 公会编号 / 公会名 |
| `av` / `rs1` / `rs16` | 头像 / 封面（可直接拼 `https://` 使用，`av` 前缀 `apic.douyucdn.cn/upload/`） |

### 2.2 房间时序点数据（图表）

```http
GET /api/room_dots?room={rid}&hours={hours}
```

用途：按分钟（或天）的时间序列，用于折线/柱状图（弹幕 + 礼物）。

返回（实测，`hours=1`）：

```json
{
  "chat": [0,0,0,...],              // 每分钟弹幕量，与 labels 对齐
  "gift": [0,0,0,...],              // 每分钟礼物值（前端 /10 展示为 LW 值）
  "labels": ["00:42","00:43",...],  // 简短时间标签（60 个）
  "fullLabels": ["2026-08-08T00:42:00",...], // 完整时间戳
  "hours": "1",
  "result": "success"
}
```

字段表：

| 字段 | 说明 |
| --- | --- |
| `chat[]` | 每分钟弹幕量 |
| `gift[]` | 每分钟礼物值 |
| `labels[]` / `fullLabels[]` | 时间标签（`fullLabels` 为 ISO 时间戳） |
| `hours` | 回显请求窗口 |

### 2.3 搜索建议

```http
GET /api/suggest_all?type={room|fan}&nickname={关键词}
```

用途：站内搜索框联想（主播/用户）。

返回（实测，`type=room&nickname=国民`）：

```json
{
  "suggest": {
    "room": [
      { "nickname": "国民大舅哥", "level": "936832", "user_id": "182102" },
      { "nickname": "国民小可爱yaya", "level": "94332", "user_id": "7204628" }
    ],
    "fan": [
      { "nickname": "国民小江总", "level": "129", "user_id": "52071911" }
    ]
  }
}
```

| 字段 | 含义 |
| --- | --- |
| `nickname` | 主播昵称 / 用户昵称 |
| `level` | 热度或等级值 |
| `user_id` | room 类型=房间号；fan 类型=用户 uid |

## 3. 登录与权限结论

### 3.1 数据中心页需要登录

实测：

```http
GET https://www.doseeing.com/data/room/182102?type=overview&dt=0
```

未登录返回：

```text
HTTP 302
Location: https://www.doseeing.com/login?redirect=%2Fdata%2Froom%2F182102%3Ftype%3Doverview%26dt%3D0
```

登录页提供两种入口：

| 方式 | 页面行为 | 备注 |
| --- | --- | --- |
| 微信扫码 | `GET /api/login` 获取 `ticket`，轮询 `/weixin_login_callback?ticket=...&ts=...` | 需要人工扫码，成功后依赖服务端写 `connect.sid` Cookie |
| 账号密码表单 | 表单字段 `username` / `password`，页面未暴露明确 JSON 登录回调 | 暂不建议自动化，避免处理验证码/风控 |

后台如需抓取登录态页面，可在 `sys_config_group` 新增/维护 `yunDataSource` 配置，填入 `doseeingCookie`（例如浏览器里的 `connect.sid=...`）。本次后台同步实现优先使用公开 `room_stat`，通常不需要登录 Cookie。

### 3.2 需要登录的接口（未登录 302 → /login）

| 接口 | Method | 用途 | 参数 |
| --- | --- | --- | --- |
| `/api/ruran?room={rid}&hours={hours}` | GET | 火箭榜/贡献榜（礼物榜，含贵族 `user.nl`、等级 `user.level`、`gift.paid.price`） | query |
| `/data/room/{rid}` | GET | 数据中心页面（查看更多数据） | 路径 |
| `/data/fan/{uid}?dt={0|1|7|30|thismonth}` | GET | 用户（水友）数据中心 | 路径 + dt |
| `/data/api/follow` | POST | 关注主播 | `{following: fid, type: 'room'}` |
| `/data/api/unfollow` | POST | 取消关注 | `{following: fid, type: 'room'}` |
| `/api/follow` / `/api/unfollow` | POST | 关注（旧路径，页面代码存在） | - |
| `/vip/follow_settings/create` | POST | VIP 关注设置 | - |

`ruran` 返回 `result[]`（前端模板推断）：`user.nickname`、`uid`、`user.nl`（贵族标识，有值则渲染贵族徽章）、`user.level`、`user.avatar`、`gift.paid.price`（贡献金额，分）。

## 4. 页面/接口映射速查

| 页面数据 | 来源 |
| --- | --- |
| 主播头部（昵称、直播状态、房间号） | SSR 内嵌（页面 HTML） |
| DM值 / SR值 / LW值 统计卡 | `GET /api/room_stat` |
| 弹幕/礼物时序图表 | `GET /api/room_dots` |
| 火箭榜（礼物榜/贵族） | `GET /api/ruran`（需登录） |
| 数据中心 | `/data/room/{rid}`（需登录） |
| 全站排行榜（DM榜/SR榜/LW榜等 `/rank/*`） | 纯 SSR 页面，无公开 JSON API |

## 5. 三站对比与对接建议

| 维度 | 播酱（bojianger） | 在看（doseeing） | 云团一簇（dongdongne） |
| --- | --- | --- | --- |
| 数据范围 | 斗鱼全站主播日报/月报 | 斗鱼全站主播实时/时段 | 单团（pid=182102）约 142 个主播 |
| 主播识别 | `rid` | `rid` + `uid` | `id` + `rid` + `card` |
| 礼物收入口径 | `yc_gift_value`（元，日报） | `gift.paid.price`（分，实时，/100=元） | 金库泡点增减流水（按 card 聚合） |
| 接口形态 | GET + token（详情） | GET 公开（统计/时序）+ 登录（榜单/数据中心） | POST + `X-Project` 头 + Referer |
| 时效 | 日报/月报（次日） | 实时/小时/天/月窗口 | 流水实时（当日可查） |

建议：
- **实时收入/在线状态**用"在看"：`room_stat` 一次调用拿 `gift.paid.price`（付费收入）、`ol`（在线热度）、`rn`（房间名）、`gonghui_name`（公会），无 token、无频次强限制。
- **全站流水榜/月报**用"播酱"：`anchor_list.do` / `anchor_list_month.do`。
- **团内金库/乐享值**用"云团一簇"：`player/list` + `player/log/list`（仅站内泡点/金库，不作为主播礼物收入）。
- 三个来源统一用 `rid`（斗鱼房间号）做关联主键；"在看"与"播酱"覆盖全站，可互相对账（如 `gift.paid.price` vs `gift_new_yc`）。

## 6. 后台接入实现建议

已在后台按以下方式接入：

- `yun_anchor.data_source` 支持 `MANUAL` / `BOJIANG` / `DOSEEING`。
- 新增主播时可选择数据源：`GET /yun/anchor/fetch-preview?anchorId=182102&dataSource=DOSEEING`。
- 批量新增可传：`POST /yun/anchor/batch`，body 为 `{ "anchorIds": ["182102"], "dataSource": "DOSEEING" }`。
- 同步全部可传：`POST /yun/anchor/sync-all?dataSource=DOSEEING`；不传则按每个主播自己的 `dataSource` 同步。
- 单个同步可传：`POST /yun/anchor/{id}/sync?dataSource=BOJIANG|DOSEEING`；不传则按该主播 `dataSource`。
- 在看同步口径：`giftTotalValue = gift.all.price / 100`，`paidGiftValue = gift.paid.price / 100`，`bagGiftValue = (gift.all.price - gift.paid.price) / 100`，`danmuCount = chat.pv`，`danmuUserCount = chat.uv`，`giftUserCount = gift.all.uv`，`activeAudienceCount = room.ol`。
