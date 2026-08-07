# 播酱数据 API 接口文档

整理时间：2026-08-07  
来源页面：https://www.bojianger.com/anchor-list-pro.html  
前端资源：

- `https://s.bojianger.com/bojianger_pro/dist/js/base.js?cdfe2fdba6fa15f4e6a7`
- `https://s.bojianger.com/bojianger_pro/dist/js/anchor-list-pro.js?cdfe2fdba6fa15f4e6a7`
- 同版本 `bojianger_pro/dist/js/*.js` 页面分包

说明：本文只整理公开前端资源中能看到的接口，并对只读 GET 接口做了低风险实测。登录、注册、短信、支付、关注、下载等操作类 POST 接口未发送业务请求；`/data/api/auth/*` 接口未登录实测时返回 `{"code":3,"msg":"token为空"}`，因此标记为需要 token。

## 1. 基础约定

基础域名：

```text
https://www.bojianger.com
```

前端统一请求封装：

```js
$.ajax({
  type: method || "get",
  url,
  dataType: "json",
  contentType: "application/json; charset=utf-8",
  data,
  headers: { token: localStorage.getItem("token") }
})
```

通用返回包：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {}
}
```

常见状态码：

| code | 含义 |
| --- | --- |
| `0` | 成功 |
| `1` / `5` | 业务错误，前端走错误回调 |
| `3` / `4` | 需要登录或登录失效 |
| `18` | 需要会员 |

分页返回结构：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "totalCount": 3000,
    "realTotalCount": 53278,
    "cutData": true,
    "pages": 1000,
    "rows": [],
    "hasPreviousPage": false,
    "prePage": 0,
    "hasNextPage": true,
    "nextPage": 2
  }
}
```

`cutData:true` 表示公开列表被截断。实测主播日榜/月榜中 `totalCount` 常为 `3000`，`realTotalCount` 是真实命中数量。

## 2. 主播流水核心接口

### 2.1 主播日报列表

用途：按天查询主播榜单，可按 `yc_gift_value` 排序得到主播礼物总值/流水榜。

```http
GET /data/api/common/anchor_list.do
```

请求参数：

| 参数 | 类型 | 示例 | 说明 |
| --- | --- | --- | --- |
| `date` | string | `2026-08-06` | 日期，`yyyy-MM-dd` |
| `keyword` | string | 空 | 主播关键词 |
| `categoryName` | string | `total` | 分类名，全部传 `total` |
| `categoryId` | number/string | `0` | 分类 ID，全部传 `0` |
| `clubName` | string | `total` | 公会名，全部传 `total` |
| `clubNo` | string | `total` | 公会编号，全部传 `total` |
| `orderBy` | string | `yc_gift_value` | 排序字段 |
| `getType` | string | `all` | `all` 全部；`sub` 只看关注，需登录 |
| `pageNum` | number | `1` | 页码 |
| `pageSize` | number | `20` | 每页数量 |

页面可选排序字段：

| orderBy | 页面含义 |
| --- | --- |
| `audience_count` | 活跃观众 |
| `hn_max` | 峰值热度 |
| `danmu_count` | 弹幕数 |
| `danmu_person_count` | 弹幕人数 |
| `yc_gift_value` | 礼物总值 |
| `gift_person_count` | 礼物人数 |

请求示例：

```http
GET https://www.bojianger.com/data/api/common/anchor_list.do?date=2026-08-06&keyword=&categoryName=total&categoryId=0&clubName=total&clubNo=total&orderBy=yc_gift_value&getType=all&pageNum=1&pageSize=3
```

返回 `rows[]` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `num` | number | 当前排序名次 |
| `rid` | number/string | 斗鱼房间号 |
| `name` | string | 主播昵称 |
| `date` | string | 统计日期 |
| `avator` | string | 主播头像 URL |
| `room_title` | string | 直播间标题 |
| `room_thumb` | string | 直播封面 |
| `cate_id` / `cate_name` | number/string | 分类 ID / 分类名 |
| `club_no` / `club_name` | string | 公会编号 / 公会名 |
| `audience_count` | number | 活跃观众 |
| `hn_max` | number | 峰值热度 |
| `hn_avg` | number | 平均热度 |
| `hn_now` | number | 当前热度 |
| `danmu_count` | number | 弹幕数量 |
| `danmu_person_count` | number | 弹幕人数 |
| `gift_person_count` | number | 送礼人数 |
| `yc_gift_value` | number | 礼物总值，页面展示为“元” |
| `gift_new_yc` | number | 付费礼物金额，页面详情称“付费礼物” |
| `gift_new_bag` | number | 背包礼物金额 |
| `gift_new_yw` | number | 鱼丸礼物数量 |
| `yw_gift_gx` | number | 老字段，鱼丸贡献相关 |
| `duration` | string | 直播时长描述 |
| `lastStartTime` | string | 最近开播时间 |
| `lived` | boolean | 是否开播过 |
| `room_status` | number | 房间状态 |
| `subcribe` | boolean | 当前用户是否关注 |
| `update_time` | number/string | 数据更新时间 |

实测首条返回片段：

```json
{
  "num": 1,
  "rid": 9999,
  "name": "yyfyyf",
  "cate_name": "DOTA2",
  "club_name": "熊掌文化",
  "audience_count": 73332,
  "danmu_count": 46077,
  "gift_person_count": 12015,
  "yc_gift_value": 327546,
  "gift_new_bag": 313154.1,
  "gift_new_yc": 14391.9,
  "gift_new_yw": 740200
}
```

### 2.2 主播月报列表

用途：按月查询主播榜单，可按 `yc_gift_value` 排序得到月度主播礼物总值/流水榜。

```http
GET /data/api/common/anchor_list_month.do
```

请求参数与日报基本一致，把 `date` 改为：

| 参数 | 类型 | 示例 | 说明 |
| --- | --- | --- | --- |
| `month` | string | `2026-08` | 月份，`yyyy-MM` |

请求示例：

```http
GET https://www.bojianger.com/data/api/common/anchor_list_month.do?month=2026-08&keyword=&categoryName=total&categoryId=0&clubName=total&clubNo=total&orderBy=yc_gift_value&getType=all&pageNum=1&pageSize=2
```

返回 `rows[]` 与日报类似，日期字段变为 `month`，`duration` 为“本月时长”。

实测首条返回片段：

```json
{
  "num": 1,
  "rid": 9999,
  "name": "yyfyyf",
  "month": "2026-08",
  "duration": "本月时长：159小时",
  "yc_gift_value": 2444616.9,
  "gift_new_bag": 2303217.4,
  "gift_new_yc": 141399.5,
  "gift_new_yw": 10748200
}
```

### 2.3 主播日报详情

用途：单主播日报详情，包含总体统计、礼物统计、粉丝牌统计、观众等级统计。前端详情页会调用此接口。

```http
GET /data/api/auth/anchor_detail_total.do
```

权限：需要 `token`，未登录实测返回：

```json
{"code":3,"msg":"token为空"}
```

请求参数：

| 参数 | 类型 | 示例 | 说明 |
| --- | --- | --- | --- |
| `rid` | number/string | `9999` | 房间号 |
| `date` | string | `2026-08-06` | 日期 |

前端模板推断返回 `data` 字段：

| 字段 | 说明 |
| --- | --- |
| `rid` / `name` / `date` / `avator` / `club_name` | 主播基础信息 |
| `total_statistic` | 总体统计对象 |
| `gift_statistic` | 老版礼物统计数组 |
| `yc_gift_statistic` | 鱼翅礼物统计数组 |
| `bag_gift_statistic` | 背包礼物统计数组 |
| `yw_gift_statistic` | 鱼丸礼物统计数组 |
| `bnn_statistic` | 粉丝牌统计数组 |
| `level_statistic` | 观众等级统计数组 |
| `new` | 是否使用新版礼物拆分 |
| `today` | 是否当天 |

`total_statistic` 主要字段：

```json
{
  "duration": "本日时长：...",
  "living": false,
  "room_title": "直播间标题",
  "cate_name": "直播类型",
  "audience_count": 0,
  "hn_max": 0,
  "hn_avg": 0,
  "gift_person_count": 0,
  "gift_new_yc": 0,
  "yc_gift_value": 0,
  "gift_new_yw": 0,
  "yw_gift_gx": 0,
  "danmu_person_count": 0,
  "danmu_count": 0
}
```

礼物统计数组元素：

```json
{
  "title": "礼物名或分组",
  "desc": "礼物价值/数量描述",
  "percent": "占比"
}
```

### 2.4 主播月报详情

```http
GET /data/api/auth/anchor_detail_total_month.do
```

权限：需要 `token`。

请求参数：

| 参数 | 类型 | 示例 | 说明 |
| --- | --- | --- | --- |
| `rid` | number/string | `9999` | 房间号 |
| `month` | string | `2026-08` | 月份 |

返回结构与日报详情类似，`total_statistic` 中的日期维度变为月。

### 2.5 主播日报导出

```http
GET /data/api/auth/anchor_detail_total_download.do?rid={rid}&date={date}
```

权限：需要 `token`，可能需要会员。前端以 blob 下载，文件名为 `{主播名}_{日期}.xlsx`。

服务端会在响应头 `mark` 标记错误：

| mark | 含义 |
| --- | --- |
| `1` | 需要登录 |
| `2` | 需要开通会员 |

## 3. 主播日志筛选接口

### 3.1 热门分类

```http
GET /data/api/common/get_category_top.do
GET /data/api/common/get_category_top_month.do
```

参数：日报传 `date`，月报传 `month`，其他筛选参数可沿用列表页。

返回：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "top5": [
      {
        "cate_id": 124,
        "cate_name": "户外",
        "game_icon": "https://..."
      }
    ]
  }
}
```

### 3.2 全部分类

```http
GET /data/api/common/get_categorys.do
GET /data/api/common/get_categorys_month.do
```

返回：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "total": [
      {
        "cate_id": 124,
        "cate_name": "户外"
      }
    ]
  }
}
```

### 3.3 热门公会

```http
GET /data/api/common/get_club_top.do
GET /data/api/common/get_club_top_month.do
```

返回：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "top5": [
      {
        "clubNo": "未签约",
        "clubName": "未签约"
      }
    ]
  }
}
```

### 3.4 全部公会

```http
GET /data/api/common/get_club_total.do
GET /data/api/common/get_club_total_month.do
```

返回：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "total": [
      {
        "clubNo": "1z77WeB",
        "clubName": "熊掌文化"
      }
    ]
  }
}
```

### 3.5 分类/公会搜索

```http
GET /data/api/common/search_cate_new.do
GET /data/api/common/search_club_nn.do
```

主播日志弹窗搜索使用的参数：

| 参数 | 示例 | 说明 |
| --- | --- | --- |
| `keyword` | `英雄` / `小象` | 搜索词 |
| `tit` | `p1` | 前端固定 |
| `total` | `false` | 前端固定 |
| `pageNum` | `1` | 页码 |
| `pageSize` | `5` | 每页数量 |

`search_cate_new.do` 返回分页，`rows[]` 字段：

```json
{
  "cate_id": 1,
  "cate_name": "英雄联盟",
  "game_name": "英雄联盟",
  "game_icon": "",
  "anchor_count": 10151526,
  "audience_count": 1690721505,
  "danmu_count": 1940249001,
  "gift_new_yc": 466270918,
  "used_names": ["英雄联盟", ""]
}
```

`search_club_nn.do` 返回分页，`rows[]` 字段：

```json
{
  "clubName": "小象互娱",
  "clubNo": "O533rdD"
}
```

## 4. 公开数据接口清单

以下接口均来自前端服务模块 `/data/api/common/*`，除特别备注外为 `GET`。

| 接口 | 用途 | 主要参数 | 返回 data 格式 |
| --- | --- | --- | --- |
| `/data/api/common/index.do` | 首页日报总览 | `date` | 对象：`globalDurationStatistic`、`globalHnStatistic`、`globalStatistic`、`globalStatisticList`、`globalTimeStatistic`、`hotCategory`、`today` |
| `/data/api/common/index-pro.do` | 首页月报总览 | `month` | 对象：`anchor_count`、`audience_count`、`cate_count`、`danmu_count`、`gift_new_yc`、`gift_value`、`hn_avg`、`month`、`update_time` |
| `/data/api/common/anchor_list.do` | 主播日报列表 | `date`、筛选、排序、分页 | 分页，`rows[]` 为主播日报对象 |
| `/data/api/common/anchor_list_month.do` | 主播月报列表 | `month`、筛选、排序、分页 | 分页，`rows[]` 为主播月报对象 |
| `/data/api/common/anchor_intime.do` | 实时主播排名 | `date`、`window`、`categoryName`、`orderBy`、分页 | 分页，实测当日可能无 `rows` |
| `/data/api/common/danmu_total.do` | 全站弹幕日志 | `date`、`anchor`、`audience`、`order`、`time`、`duration`、分页 | 对象：`anchor`、`audience`、`page`、`shortTimeDurationlist`、`timeDurationlist` |
| `/data/api/common/hot_danmu_page.do` | 热门弹幕 | `date`、`window`、`categoryName`、`orderBy`、分页 | 分页或空 data；模板字段：`num`、`rid`、`anchor_name`、`cate_name`、`room_title`、`count`、`txt` |
| `/data/api/common/audience_list.do` | 水友日报列表 | `date`、`keyword`、`orderBy`、`getType`、分页 | 分页，`rows[]` 含 `uid`、`audience_name`、`yc_gift_value`、`gift_new_yc`、`danmu_count` |
| `/data/api/common/audience_list_month.do` | 水友月报列表 | `month`、`keyword`、`orderBy`、`getType`、分页 | 分页，`rows[]` 含 `uid`、`audience_name`、`yc_gift_value`、`gift_new_yc`、`danmu_count` |
| `/data/api/common/club_statistic.do` | 公会日报统计 | `date`、`keyword`、`clubNo`、`orderBy`、`getType`、分页 | 分页，`rows[]` 含公会聚合字段 |
| `/data/api/common/club_statistic_month.do` | 公会月报统计 | `month`、`keyword`、`clubNo`、`orderBy`、`getType`、分页 | 分页，`rows[]` 含公会月聚合字段 |
| `/data/api/common/club_statistic_detail.do` | 公会日报详情汇总 | `date`、`clubName`、`clubNo` | 对象，含 `anchor_count`、`audience_count`、`yc_gift_value`、`gift_new_yc` 等 |
| `/data/api/common/club_statistic_detail_month.do` | 公会月报详情汇总 | `month`、`clubName`、`clubNo` | 对象，字段同公会日报详情 |
| `/data/api/common/club_statistic_detail_cate.do` | 公会日报按分类拆分 | `date`、`clubNo`、`categoryId`、排序、分页 | 分页，`rows[]` 含分类 + 公会聚合字段 |
| `/data/api/common/club_statistic_detail_cate_month.do` | 公会月报按分类拆分 | `month`、`clubNo`、`categoryId`、排序、分页 | 分页，`rows[]` 含分类 + 公会聚合字段 |
| `/data/api/common/cate_base_statistic.do` | 类型日报基础统计 | `date`、`categoryId`、`orderBy`、`getType`、分页 | 分页，`rows[]` 含分类聚合字段 |
| `/data/api/common/cate_base_statistic_month.do` | 类型月报基础统计 | `month`、`categoryId`、`orderBy`、`getType`、分页 | 分页，`rows[]` 含分类月聚合字段 |
| `/data/api/common/cate_hn_statistic.do` | 类型热度分布 | `date`、`categoryName`、`orderBy`、分页 | 分页，`rows[]` 含 `count_gt_100w`、`count_100w_10w`、`hn_avg` 等 |
| `/data/api/common/cate_time_statistic.do` | 类型时段统计 | `date`、`categoryName`、`orderBy`、分页 | 分页，`rows[]` 含 `count_0_3`、`hn_avg_0_3` 等时段字段 |
| `/data/api/common/get_category_top.do` | 日报热门分类 | `date` | `{top5: [{cate_id,cate_name,game_icon}]}` |
| `/data/api/common/get_category_top_month.do` | 月报热门分类 | `month` | `{top5: [{cate_id,cate_name,game_icon}]}` |
| `/data/api/common/get_categorys.do` | 日报全部分类 | `date` | `{total: [{cate_id,cate_name}]}` |
| `/data/api/common/get_categorys_month.do` | 月报全部分类 | `month` | `{total: [{cate_id,cate_name}]}` |
| `/data/api/common/get_club_top.do` | 日报热门公会 | `date` | `{top5: [{clubNo,clubName}]}` |
| `/data/api/common/get_club_top_month.do` | 月报热门公会 | `month` | `{top5: [{clubNo,clubName}]}` |
| `/data/api/common/get_club_total.do` | 日报全部公会 | `date` | `{total: [{clubNo,clubName}]}` |
| `/data/api/common/get_club_total_month.do` | 月报全部公会 | `month` | `{total: [{clubNo,clubName}]}` |
| `/data/api/common/search_anchor_new.do` | 搜索主播 | `keyword`、`tit`、`total`、分页 | 分页，`rows[]` 含 `rid`、`name`、`yc_gift_value`、`gift_new_yc` |
| `/data/api/common/search_audience_new.do` | 搜索水友 | `keyword`、`tit`、`total`、分页 | 分页，`rows[]` 含 `uid`、`audience_name`、`yc_gift_value` |
| `/data/api/common/search_club_new.do` | 搜索公会 | `keyword`、`tit`、`total`、分页 | 分页，`rows[]` 含 `club_no`、`club_name`、`gift_new_yc` |
| `/data/api/common/search_club_nn.do` | 搜索公会名/编号 | `keyword`、`tit`、`total`、分页 | 分页，`rows[]` 为 `{clubName,clubNo}` |
| `/data/api/common/search_cate_new.do` | 搜索类型 | `keyword`、`tit`、`total`、分页 | 分页，`rows[]` 含 `cate_id`、`cate_name`、`gift_new_yc` |
| `/data/api/common/search_hot_history.do` | 今日热搜 | `keyword`、分页 | `{hotList: [{keyword,num}]}` |
| `/data/api/common/get_anchor_na.do` | 主播昵称头像 | 前端传参对象 | 前端存在，但实测 `rid=9999` 返回 404，疑似废弃或参数/路由变化 |

### 4.1 聚合统计通用字段

公会、分类统计类 `rows[]` 常见字段：

| 字段 | 说明 |
| --- | --- |
| `anchor_count` | 主播数量 |
| `audience_count` / `audience_count_avg` | 活跃观众总数 / 平均值 |
| `danmu_count` / `danmu_count_avg` | 弹幕总数 / 平均值 |
| `danmu_person_count` / `danmu_person_count_avg` | 弹幕人数总数 / 平均值 |
| `gift_person_count` / `gift_person_count_avg` | 礼物人数总数 / 平均值 |
| `yc_gift_value` / `yc_gift_value_avg` | 礼物总值 / 平均值 |
| `gift_new_yc` / `gift_new_yc_avg` | 付费礼物金额 / 平均值 |
| `duration_total` / `duration_avg` | 直播总时长 / 平均时长 |
| `hn_avg` / `hn_avg_total` | 热度均值 / 总热度 |
| `subcribe` | 当前用户是否关注 |

## 5. 需要登录或会员的详情接口

以下接口均来自 `/data/api/auth/*`，前端用 `GET` 并携带 `headers.token`。

| 接口 | 用途 | 主要参数 | 返回数据格式 |
| --- | --- | --- | --- |
| `/data/api/auth/anchor_detail_total.do` | 主播日报总体统计 | `rid`、`date` | 对象：主播信息、`total_statistic`、礼物/粉丝牌/等级统计数组 |
| `/data/api/auth/anchor_detail_total_month.do` | 主播月报总体统计 | `rid`、`month` | 同上，按月 |
| `/data/api/auth/anchor_detail_time.do` | 主播日报时段统计 | `rid`、`date` | 前端模板为时段列表/图表数据 |
| `/data/api/auth/anchor_detail_time_month.do` | 主播月报每日统计 | `rid`、`month`、分页 | 前端模板为每日列表 |
| `/data/api/auth/anchor_detail_audience.do` | 主播日报水友数据 | `rid`、`date`、`orderBy`、分页 | 分页，水友贡献/弹幕列表 |
| `/data/api/auth/anchor_detail_audience_month.do` | 主播月报水友数据 | `rid`、`month`、`orderBy`、分页 | 分页，水友月贡献/弹幕列表 |
| `/data/api/auth/anchor_detail_danmu.do` | 主播历史弹幕 | `rid`、`uid`、`date`、`order`、`time`、`duration`、分页 | 弹幕分页 |
| `/data/api/auth/audience_detail.do` | 水友日报详情 | `uid`、`date`、`keyword`、排序、分页 | 水友访问房间/贡献数据 |
| `/data/api/auth/audience_detail_month.do` | 水友月报详情 | `uid`、`month`、`keyword`、排序、分页 | 水友月度访问房间/贡献数据 |
| `/data/api/auth/audience_detail_day_month.do` | 水友月报按日详情 | `uid`、`month`、排序、分页 | 月内每日汇总 |
| `/data/api/auth/audience_detail_danmu.do` | 水友日报弹幕 | `uid`、`rid`、`date`、`order`、`time`、`duration`、分页 | 弹幕分页 |
| `/data/api/auth/audience_detail_danmu_month.do` | 水友月报弹幕 | `uid`、`rid`、`month`、`order`、`day`、分页 | 弹幕分页 |
| `/data/api/auth/anchor_detail_total_download.do?rid={rid}&date={date}` | 主播日报导出 xlsx | `rid`、`date` | Blob 文件；响应头 `mark=1/2` 表示登录/会员限制 |

## 6. 用户关注接口

以下接口均为 `POST`，需要 `token`。属于用户状态变更或用户私有数据，未实测。

| 接口 | 用途 | JSON 参数 |
| --- | --- | --- |
| `/data/api/user/get_subscribes_list.do` | 关注主播列表 | 无 |
| `/data/api/user/get_subscribes_audience.do` | 关注水友列表 | 无 |
| `/data/api/user/get_subscribes_club.do` | 关注公会列表 | 无 |
| `/data/api/user/get_subscribes_cate.do` | 关注类型列表 | 无 |
| `/data/api/user/subscribe.do` | 关注主播 | `{ "rid": 9999 }` |
| `/data/api/user/remove_subscribe.do` | 取消关注主播 | `{ "rid": 9999 }` |
| `/data/api/user/subscribe_audience.do` | 关注水友 | `{ "uid": "..." }` |
| `/data/api/user/remove_subscribe_audience.do` | 取消关注水友 | `{ "uid": "..." }` |
| `/data/api/user/subscribe_club.do` | 关注公会 | `{ "clubNo": "..." }` |
| `/data/api/user/remove_subscribe_club.do` | 取消关注公会 | `{ "clubNo": "..." }` |
| `/data/api/user/subscribe_cate.do` | 关注类型 | `{ "cateId": 1 }` |
| `/data/api/user/remove_subscribe_cate.do` | 取消关注类型 | `{ "cateId": 1 }` |

## 7. 用户、短信、支付接口

以下接口来自前端用户/会员页面。大多为操作类 POST，未实测。

| 接口 | Method | 用途 | 参数格式 |
| --- | --- | --- | --- |
| `/user/api/user/login.do` | POST | 登录 | `{ "phone": "...", "password": "..." }`；成功前端把返回值写入 `localStorage.token` |
| `/user/api/user/check_valid.do` | POST | 检查用户名等有效性 | 表单：`type=username`、`str=...` |
| `/user/api/user/register.do` | POST | 注册 | `{ "phone", "code", "username", "password", "platform": 1 }` |
| `/user/api/user/check_login.do` | POST | 检查登录 | 无 |
| `/user/api/user/forget_reset_password.do` | POST | 重置密码 | `{ "phone", "code", "newPassword" }` |
| `/user/api/user/get_user_info.do` | POST | 获取用户信息 | 无，需 token |
| `/user/api/user/club_discount_check.do` | POST | 公会会员折扣/资格校验 | 会员页参数，含 `club_name`、`club_code` |
| `/user/api/user/update_username.do` | POST | 修改用户名 | `{ "username": "..." }` 或前端表单对象 |
| `/user/api/user/update_password.do` | POST | 修改密码 | `{ "password", "passwordNew" }` |
| `/user/api/user/logout.do` | POST | 退出登录 | 无 |
| `/user/api/sms/send_code.do` | POST | 注册短信验证码 | `{ "phone", "token", "sessionId", "sig", "scene" }`，依赖阿里滑块 |
| `/user/api/sms/send_code_resetpass.do` | POST | 重置密码短信验证码 | `{ "phone", "token", "sessionId", "sig", "scene" }`，依赖阿里滑块 |
| `/user/api/pay/ali_pc_pay.do` | POST | 支付宝 PC 会员支付 | 会员页 `listParam` |
| `/user/api/pay/ali_pc_club_pay.do` | POST | 支付宝 PC 公会会员支付 | 会员页 `listParam`，含公会信息 |
| `/user/api/pay/ali_wap_pay.do` | POST | 支付宝 WAP 会员支付 | 会员页 `listParam` |
| `/user/api/pay/ali_wap_club_pay.do` | POST | 支付宝 WAP 公会会员支付 | 会员页 `listParam`，含公会信息 |
| `/user/api/pay/wx_jsapi_pay.do` | POST | 微信 JSAPI 支付 | 会员页 `listParam`，含 `code` |
| `/user/api/pay/wx_native_pay.do` | POST | 微信 Native 支付 | 会员页 `listParam`；成功返回 `{id, codeUrl}` |
| `/user/api/pay/wx_native_pay_club.do` | POST | 微信 Native 公会会员支付 | 会员页 `listParam`，含公会信息；成功返回 `{id, codeUrl}` |
| `/user/api/pay/check_order.do` | POST | 查询支付订单 | 会员页 `listParam`，含 `payId` |
| `/user/api/pay/query_mem_info.do` | GET | 查询会员信息/价格 | 无或 token |
| `/user/api/pay/query_club_mem_info.do` | GET | 查询公会会员信息/价格 | 无或 token |

会员页常见支付参数：

```json
{
  "phone": "",
  "type": "7d",
  "payTypeNew": "aliyun",
  "amount": 0,
  "orderName": "支付会员",
  "orderDesc": "支付会员",
  "returnUrl": "https://www.bojianger.com/user.html",
  "club_name": "",
  "club_code": ""
}
```

## 8. 拉取主播流水的建议

如果目标是“主播流水/礼物总值排行榜”，优先使用公开列表接口：

```http
GET /data/api/common/anchor_list.do?date=2026-08-06&orderBy=yc_gift_value&pageNum=1&pageSize=20...
GET /data/api/common/anchor_list_month.do?month=2026-08&orderBy=yc_gift_value&pageNum=1&pageSize=20...
```

关键字段：

| 字段 | 建议入库名 | 含义 |
| --- | --- | --- |
| `rid` | `room_id` | 房间号 |
| `name` | `anchor_name` | 主播昵称 |
| `date` / `month` | `stat_date` / `stat_month` | 统计周期 |
| `cate_id` / `cate_name` | `category_id` / `category_name` | 分类 |
| `club_no` / `club_name` | `club_no` / `club_name` | 公会 |
| `yc_gift_value` | `gift_total_value` | 礼物总值，页面单位为元 |
| `gift_new_yc` | `paid_gift_value` | 付费礼物金额 |
| `gift_new_bag` | `bag_gift_value` | 背包礼物金额 |
| `gift_new_yw` | `fishball_gift_count` | 鱼丸礼物数量 |
| `gift_person_count` | `gift_user_count` | 送礼人数 |
| `audience_count` | `active_audience_count` | 活跃观众 |
| `danmu_count` | `danmu_count` | 弹幕数 |
| `danmu_person_count` | `danmu_user_count` | 弹幕人数 |

注意事项：

- 公开榜单会截断，`cutData:true` 时无法保证能拉完所有主播。
- `getType=sub` 依赖登录后的关注列表，公开采集建议固定 `getType=all`。
- 详情和下载接口需要 `token`，且可能受会员权限限制。
- 网站接口并非公开开放平台，字段和权限可能随前端版本变化；建议落库时保存原始 JSON。
