# 云团一簇（dongdongne）数据 API 接口文档

整理时间：2026-08-13（复核更新）  
来源站点：https://dongdongne.com/ （站名"云团一簇"，团员金库/乐享用户社区站）  
前端资源：

- `https://dongdongne.com/assets/index-BWBBCwKx.js`（单页应用主包，接口与字段均从此包反推；旧包 `index-m-ZLnmOI.js` 已替换）

说明：本文整理的是云团一簇站内金库/乐享值相关接口。核心 4 个接口（主播/团员列表、详情、金库日志、乐享用户列表）2026-08-13 已实测可用；站点新增的"直播聚合/怪兽来袭"模块接口（多平台房间、弹幕 WebSocket、主播搜索、在看房间数据）当前对外部匿名调用返回 `code:8000` 权限异常，需站点登录态/额外凭据，详见第 6 节。这些接口均不作为本项目"主播礼物流水收入"的主数据源，主播礼物收入请优先使用在看或播酱。

## 1. 基础约定

API 基础域名：

```text
https://api.dongdongne.com
```

请求头（两个接口必需）：

| 头 | 值 | 说明 |
| --- | --- | --- |
| `X-Project` | `182102` | 项目标识，缺失时列表/流水接口返回 `code:8000` |
| `Content-Type` | `application/json` | 仅 POST 需要 |
| `Referer` | `https://dongdongne.com/` | **主播详情 GET 接口必需**，缺失时返回 `code:500`；建议所有请求都带浏览器 UA + Referer |

前端统一请求封装：

```js
// POST 封装（列表/流水）
fetch(url, { method: "POST", mode: "cors",
  headers: { "Content-Type": "application/json", "X-Project": "182102" },
  body: JSON.stringify({ page, size, search }) })

// GET 封装（详情）
fetch(url, { method: "GET", mode: "cors", headers: { "X-Project": "182102" } })
```

通用返回包：

```json
{ "code": 0, "data": {}, "message": "SUCCESS" }
```

状态码：

| code | message | 含义 |
| --- | --- | --- |
| `0` | `SUCCESS` | 成功 |
| `500` | `SERVICE_EXCEPTION` | 服务异常，如详情接口缺 `Referer` |
| `8000` | `FILE_EXCEPTION` | 权限异常：① 缺 `X-Project` 头；② 直播聚合类接口（`/api/douyu/room`、`/api/doseeing/rooms`、`/api/live-search` 等）匿名/外部调用时返回，需登录态或额外凭据 |
| `131002` | `...does not exist...` | 数据不存在（id 错误） |
| `404` | `NOT_FOUND` | 接口不存在/已下线 |

> 实测说明（2026-08-13）：核心金库/乐享值接口用浏览器内 `fetch`（自动带 `Sec-Fetch*` 等浏览器指纹头）可正常返回 `code:0`；用 `curl` 直连时核心列表接口会因缺浏览器指纹头返回 `code:500`/`code:8000`。因此建议用带浏览器 UA + Referer + Origin 的 HTTP 客户端调用。新增的直播聚合类接口即使带 `X-Project` 也返回 `code:8000`，需登录态。

分页请求体（通用）：

```json
{
  "page": 1,
  "size": 12,
  "search": [
    { "key": "enable", "value": "1", "condition": "=", "relationship": "AND", "type": "CONDITION" }
  ]
}
```

分页响应（通用）：

```json
{ "code": 0, "data": { "total": 142, "size": 12, "page": 1, "list": [] } }
```

`search` 数组元素格式：`{ key, value, condition(=、>=、<、LIKE), relationship(AND/OR), type("CONDITION") }`。

## 2. 主播（团员）核心接口

页面：`#treasury`（团员金库），展示全部主播（团员）卡片、金库余额、点开看流水。

### 2.1 主播列表

```http
POST /api/live/player/list
```

请求体：

```json
{
  "page": 1,
  "size": 12,
  "search": [
    { "key": "enable", "value": "1", "condition": "=", "relationship": "AND", "type": "CONDITION" }
  ]
}
```

- `size` 可加大（实测 `size=200` 可一次拉全 140 条，2026-08-13 实测 `total=140`）。
- 按昵称/名称搜索时在 `search` 前追加 LIKE 条件：

```json
{ "key": "nickname", "value": "%关键词%", "condition": "LIKE", "relationship": "OR", "type": "CONDITION" }
{ "key": "name", "value": "%关键词%", "condition": "LIKE", "relationship": "OR", "type": "CONDITION" }
```

返回 `list[]` 字段（实测）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 主播业务 ID（详情接口路径参数） |
| `pid` / `oid` | number | 项目 ID，均为 `182102` |
| `rid` | string | **房间号**（如国民大舅哥 = `182102`） |
| `name` | string | 主播名（如 `国民大舅哥`） |
| `nickname` | string | 别名/昵称（如 `团长/大舅哥/阿舅/白毛/小毛/毛哥`） |
| `platform` | string | 平台：`DOU_YU`、`HU_YA` 等 |
| `avatar` | string | 头像 URL |
| `num` | number | 金库余额（泡点，详情页金额优先取此字段） |
| `card` | string | 卡片号，**流水按此字段聚合到主播** |
| `online` | number | 在线标记（`1`/`0`，前端展示"在线/离线"） |
| `enable` | number | 启用标记（`1` 启用） |
| `attribute` | string | JSON 字符串，含 `type:"1"`（正式团员）、`breathe`、赛季标记 `isCurrentSeason/currentSeason` 等 |
| `meal` | string | 套餐/配置 JSON（多为 `"{}"`） |
| `redemption` | number | 兑换相关字段 |
| `creationDateTime` / `modificationDateTime` | string | 创建/修改时间（部分记录为 `0001-12-30` 的占位值） |

实测首条片段：

```json
{
  "num": 7100000, "pid": 182102, "oid": 182102,
  "rid": "111", "platform": "DOU_YU", "enable": 1,
  "name": "TEST2", "nickname": "TEXT", "online": 1, "id": 250000,
  "attribute": "{\"type\": \"1\"}", "card": "KDSAKS33"
}
```

注意：前端渲染时会过滤 `attribute.type === "1"` 的主播（当前全部通过）；"当前赛季"筛选是前端按 `attribute` 中的赛季标记过滤，不是接口参数。列表中 `TEXT/TEST`（rid=111）为测试条目。

### 2.2 主播详情

```http
GET /api/live/player/{id}?id={id}
```

- `id` = 列表中的 `id`（如国民大舅哥 `100000`），不是房间号。
- **必须带 `Referer: https://dongdongne.com/` 和浏览器 UA**，否则 `code:500`。
- 返回 `data` 为单个主播对象，字段与列表一致（实测 `id=100000` 返回 `num:1367700, name:"国民大舅哥", rid:"182102", card:"12345678", attribute:'{"type":"1","breathe":"1"}'`）。

### 2.3 金库日志（非主播礼物收入）

```http
POST /api/live/player/log/list
```

用途：团员金库/乐享币的增减流水；这是云团一簇站内泡点口径，**不等同于斗鱼主播礼物流水收入**。主播礼物收入同步请使用在看的 `room_stat` 或播酱的主播日报/月报接口。

请求体示例（查某一天全量流水）：

```json
{
  "page": 1,
  "size": 500,
  "search": [
    { "key": "creationDateTime", "value": "2026-08-08 00:00:00", "condition": ">=", "relationship": "AND", "type": "CONDITION" },
    { "key": "creationDateTime", "value": "2026-08-09 00:00:00", "condition": "<", "relationship": "AND", "type": "CONDITION" }
  ]
}
```

查单个主播流水（按 `card` 过滤）：

```json
{ "page": 1, "size": 10, "search": [
  { "key": "card", "value": "12345678", "condition": "=", "relationship": "AND", "type": "CONDITION" }
] }
```

返回 `list[]` 字段（实测）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 流水记录 ID |
| `pid` / `oid` | number | 项目 ID（`182102`） |
| `rid` | string/null | 关联房间号（用户侧操作时是用户房号；系统操作时为 null） |
| `uid` | string/null | 关联用户（操作者）ID |
| `platform` | string | 平台 |
| `type` | string | 流水类型，见下表 |
| `num` | number | 变动值：正=增加，负=扣减 |
| `card` | string | 主播卡片号（聚合键） |
| `content` | string | 描述，如 `【贝壳想放假】给1000`；系统扣减形如 `【超市】` |
| `creationDateTime` | string | 记录时间 |

`type` 枚举（前端按 type 区分增减方向）：

| 方向 | type 值 |
| --- | --- |
| 增加（plus） | `USER_INCREASE_PLAYER_NUM`、`SYS_OPERATION_INCREASE`、`increase`、`recharge`、`add`、`income`、`team_increase` |
| 扣减（minus） | `SYS_OPERATION_DECREASE`、`USER_DECREASE_PLAYER_NUM`、`decrease`、`consumption`、`consume`、`reduce`、`deduct`、`team_decrease` |
| 特殊 | `SYS_OPERATION`（按 num 正负判断）、`USER_MEAL_EAT`（不计金额） |

实测片段：

```json
{
  "num": 100000, "pid": 182102, "oid": 182102,
  "type": "USER_INCREASE_PLAYER_NUM", "rid": "21095475",
  "platform": "DOU_YU", "content": "【贝壳想放假】给1000",
  "uid": "182102", "card": "CCE8A09E", "creationDateTime": "2026-08-08 01:16:44"
}
```

## 3. 乐享用户接口

页面：`#users`（乐享用户），用户乐享值排行榜，支持昵称搜索。

### 3.1 用户列表

```http
POST /api/live/user/list
```

请求体：

```json
{ "page": 1, "size": 60 }
```

按昵称/名称搜索时追加 LIKE 条件（同 2.1 的格式，key 为 `nickname`/`name`）。

返回 `list[]` 字段（实测）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 用户记录 ID |
| `uid` | string | 平台用户 ID |
| `room` | string | 所属房间号 |
| `platform` | string | 平台 |
| `nickname` | string | 昵称（如 `脚皇`） |
| `name` | string | 名称（如 `用脚点礼物`） |
| `avatar` | string | 头像 |
| `pointsNum1` | number | 本期/基础乐享值 |
| `pointsNum2` | number | 当前期乐享值 |
| `pointsTotal` | number | **乐享值合计（排行榜排序字段）** |
| `remarks` | string | 备注 |

实测首条片段：

```json
{
  "room": "182102", "platform": "DOU_YU", "uid": "488801793",
  "name": "用脚点礼物", "nickname": "脚皇",
  "pointsNum1": 600200, "pointsNum2": 17992580, "pointsTotal": 18592780
}
```

## 4. 站点内容模块接口（仅供参考，实测 404）

以下接口存在于前端代码（`dy{pid}` 子服务），实测全部返回 `code:404 NOT_FOUND`，疑似已下线或对调用来源有限制：

| 接口 | Method | 用途 | 请求体 |
| --- | --- | --- | --- |
| `/dy{pid}/fashion/count?pid={pid}` | GET | OOTD 图集计数 | 无 |
| `/dy{pid}/fashion/list?pid={pid}` | POST | OOTD 图集列表 | `{page,size,search:[],sort:[{content:"creationDateTime",condition:"ASC"},{content:"id",condition:"ASC"}]}` |
| `/dy{pid}/football/video/list?pid={pid}` | POST | 足球视频列表 | `{page,size:500,search:[]}` |
| `/dy{pid}/investment/count?pid={pid}` | GET | 投资计数 | 无 |
| `/dy{pid}/investment/list?pid={pid}` | POST | 投资列表 | `{page,size:500,search:[]}` |

其中 `pid = 182102`（前端常量），相关图片资源存于腾讯 COS：`https://kid-1300696070.cos.ap-guangzhou.myqcloud.com/chronos_pulse/images/{football|OOTD|OOTD/thumbnail}/`。

## 5. 直播聚合模块接口（新增，2026-08-13 发现；当前需登录态，外部匿名调用返回 8000）

站点在新版前端（`index-BWBBCwKx.js`）中新增了"直播监控/直播聚合 + 怪兽来袭"功能模块，其接口从主包反推得到。这些接口通过 `api.dongdongne.com` 提供，但**对外部匿名调用（含带 `X-Project`）均返回 `code:8000 FILE_EXCEPTION`**，疑似需要站点登录态或额外凭据，当前不可公开使用。列出供参考：

### 5.1 多平台直播间信息（HTTP）

| 接口 | Method | 用途 |
| --- | --- | --- |
| `/api/douyu/room/{roomId}` | GET/POST | 斗鱼直播间信息/弹幕聚合 |
| `/api/huya/room/{roomId}` | GET/POST | 虎牙直播间信息 |
| `/api/bilibili/room/{roomId}` | GET/POST | B 站直播间信息 |
| `/api/douyin/room/{roomId}` | GET/POST | 抖音直播间信息 |
| `/api/xiaohongshu/room/{roomId}` | GET/POST | 小红书直播间信息 |

`roomId` 为各平台房间号（如斗鱼 `182102`）。前端对象 `pe` 定义：`{douyu, huya, bilibili, douyin, xiaohongshu}` → `/api/{platform}/room/{encodeURIComponent(roomId)}`。

### 5.2 多平台弹幕（WebSocket）

| 接口 | 用途 |
| --- | --- |
| `/api/douyu/danmaku/{roomId}` | 斗鱼弹幕 WebSocket |
| `/api/huya/danmaku/{roomId}` | 虎牙弹幕 WebSocket |
| `/api/bilibili/danmaku/{roomId}` | B 站弹幕 WebSocket |
| `/api/douyin/danmaku/{roomId}` | 抖音弹幕 WebSocket |
| `/api/xiaohongshu/danmaku/{roomId}` | 小红书弹幕 WebSocket |

前端用 WebSocket 连接（`new WebSocket('/api/{platform}/danmaku/{roomId}')`），监听 `danmaku` 事件，字段含 `messageId`、`text`、用户信息等。

### 5.3 主播搜索

```http
GET /api/live-search?platform={platform}&q={关键词}&limit=20
```

用于直播聚合弹层按平台搜索主播/房间。返回结构 `{ success, data: [...] }`（`data` 为数组，含 `roomId`/`platform` 等；失败 `{ success:false, error }`）。

### 5.4 在看房间数据

```http
GET /api/doseeing/rooms?platform={platform}&rids={rid1,rid2,...}&dt={yyyy-MM-dd}
```

- `platform`：`douyu` / `huya`
- `rids`：逗号分隔的房间号（每批最多 100 个）
- `dt`：日期（如 `2026-08-13`）
- 响应头 `Accept: application/json`

用于批量拉取指定房间在指定日期的"在看"数据。

### 5.5 直播流地址

```http
GET /api/live-stream/{platform}/{roomId}?quality={quality}
```

用于获取可播放的直播流地址，参数含平台、房间号、清晰度（`quality`）。

### 5.6 其它

- 怪兽来袭等小游戏模块（`monster-invasion`、`building-defense`、`dojo-cultivation` 等）为前端本地玩法，未见独立公开统计接口。

> 结论：以上直播聚合类接口当前对匿名外部调用不可用（`code:8000`），不建议作为数据源接入；若需接入请先确认站点登录鉴权方式。

## 6. 关键业务口径

### 6.1 "今日金库净贡献" = 当日金库日志按 card 聚合


前端"每日统计"（dailyStats）逻辑（仅用于云团一簇金库净贡献，不作为主播礼物收入）：

1. 调 `POST /api/live/player/log/list`，条件 `creationDateTime >= "今天 00:00:00"` 且 `< "明天 00:00:00"`，`size=500` 分页拉全（上限 20 页）。
2. 按 `card` 分组：`sum(±num)`，其中 type 为增加类取正、扣减类取负。
3. 用 `POST /api/live/player/list` 返回的 `card → 主播(nickname/rid)` 映射出主播。

示例（2026-08-08 实测）：

| 主播 | 房间号 | 今日净贡献 |
| --- | --- | --- |
| 茶茶 | 12064581 | +320000 |
| ⼩静/小鸡/阿鸡 | 11418893 | +180000 |
| 青允 | 12743086 | +170000 |
| 豌豆 | 12282113 | +168400 |
| 雅婷 | 92233 | -250000 |

单位是站内"泡点"而非人民币，也不是斗鱼礼物收入；金额展示时前端按 100 折算（`num/100`，见 `El()` 的 `wl(value, 100)`）。

### 6.2 金库余额字段

- 主播金库余额：`player.num`（详情页金额优先取 `num`，其次 `balance/playerNum/points_num/value`）。
- 用户乐享值：`user.pointsTotal`（页面排序/展示"乐享值"即此字段）。

### 6.3 "当前赛季"筛选

前端本地过滤：解析 `player.attribute` JSON，其中含 `online/isCurrentSeason/currentSeason/is_current_season/current_season` 等键的为"当前赛季"团员；接口本身不支持赛季参数。

### 6.4 测试/占位数据

- 主播列表含测试条目 `TEXT/TEST`（rid=111），如需干净数据可按 `rid != 111` 或 `name` 过滤。
- 部分时间字段为占位值 `0001-12-30`。

## 7. 与播酱数据的定位差异（对接建议）

| 维度 | 播酱（bojianger） | 云团一簇（dongdongne） |
| --- | --- | --- |
| 数据范围 | 斗鱼全站主播日报/月报，含礼物总值、热度、弹幕 | 单个团/项目（pid=182102）内收录的主播（约 140 个） |
| 主播识别 | `rid`（房间号） | `id`（业务 ID）+ `rid`（房间号），靠 `card` 聚合流水 |
| 流水口径 | 礼物总值 `yc_gift_value`（元） | 金库泡点增减流水（站内币，按 100 折算；非主播礼物收入） |
| 接口形态 | GET + token（详情需要） | POST + `X-Project` 头 + Referer（详情需要） |
| 时效性 | 日报/月报（次日更新） | 流水实时（当日可查） |

建议：主播礼物流水收入使用播酱或在看，用 `rid` 对齐；云团一簇只保留团员金库余额、乐享用户和站内金库日志口径。

