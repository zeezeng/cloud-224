# -*- coding: utf-8 -*-
"""交叉校验: DB房号匹配到的网页名 与 DB存储的中文名 是否一致。
- 读 s10_5_db_hex.txt: id, hex(anchor_name)
- 读 s10_5_db_room.txt: id, room_id
- 用脚本中的 web_rooms + web_rows 映射 room -> 网页名/状态/开条
- 校验每个 id: DB名 与 网页名 是否相互包含(宽松)
"""
import re

# DB: id -> name
db_name = {}
with open(r"f:\MyCodes\cloud-224\s10_5_db_hex.txt", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line or line.startswith("mysql:"):
            continue
        parts = line.split("\t")
        if len(parts) < 2:
            continue
        try:
            rid = int(parts[0])
        except ValueError:
            continue
        db_name[rid] = bytes.fromhex(parts[1]).decode("utf-8")

# DB: id -> room
db_room = {}
with open(r"f:\MyCodes\cloud-224\s10_5_db_room.txt", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line or line.startswith("mysql:"):
            continue
        parts = line.split("\t")
        if len(parts) < 2:
            continue
        try:
            rid = int(parts[0]); room = int(float(parts[1]))
        except ValueError:
            continue
        db_room[rid] = room

# 网页rooms (顺序与 web_rows 一致)
web_rooms = [
8727436,11518380,12282113,7300160,11492116,12767534,11778421,1965453,11328146,12195609,146235,
12157371,12485490,12866270,12870978,12598324,9046690,9388639,10952213,11969714,734648,12001938,
149634,12739646,12174524,12869858,23233,11418893,10399680,12846805,12805686,12860332,12063011,
12828296,12281372,12500663,12244277,12668179,11747071,9184805,12697808,11862656,12832995,8458376,
12064581,12118254,7983087,12808349,12671327,12842732,12845996,11942374,153243,92233,31224051,
12875865,12743086,12697879,149637,12163259,12836474,12869557,158428,2622942,12543616,12848608,
158267,31262168,12865305,12858414,12739861,12465482,12192999,12858541,12803780,12641916,12877364,
12694712,12814148,12855348,12332904,12878475,12828787,12555783,12618651,12817503,12842675,12867461,
12742373,12867398,12828016,12863562,12834114,12834168,12804285,12859423,12071020,12867077,12870190,
12850930,12874453,12320859,12868598,12828007,12869859,12878199,156324,12803643,
]
# 网页 rows: (队员名, 状态, 开条) 与 s10_5_sync.py 中 web_rows 同一来源
# 这里只取每行第一个名字片段用于松散包含校验，故从 sql 脚本生成的 row 简化为: 队员列文本
web_rows_text = [
"暖妹/暖子","鼠鼠/251","豌豆","小胖","瑶瑶","可可","莓莓","大琳","小哈尼/哈尼","甜筒/筒子/小虎",
"芭拉","迪士尼","李李","苏袜酱/苏袜","美伢","糯糯","雅婷轰天雷凌振","宝宝/电饭煲","灰子铁笛仙马麟","小宁",
"苏西神算子蒋敬","涂涂","豆豆","小aa/小AA","安然","兔小蕾一丈青扈三娘大蕾","王潘","林饭儿/饭儿","集美来拉獨火星孔亮","欣欣/小欣欣",
"小冉","陈开心/宝妈","小静/小鸡/阿鸡","醋醋","小田","车厘子","施施","胡萝卜","小晴","允真",
"栗子","卡比","泥泥","七安","理理","汉堡","小李","陈知含/知含","玩蛇","茶茶",
"七七","啵啵","苏晚星/晚星","麦小麦/小麦","楚楚","婷婷","北极昕矮脚虎王英","鱼儿赛仁贵郭盛","毛毛神医安道全","青允紫髯伯皇甫端",
"雪糕小温侯吕方","葡萄丧门神鲍旭","牛肉嘎嘎酱混世魔王樊瑞","一诺毛头星孔明","集美莱拉独火星孔亮","蘭七八臂哪吒项充","蛋饼飞天大圣李衮","小禾苗玉臂匠金大坚","uu翻江蜃童猛","姜江通臂猿侯健",
"零叁九尾龟陶宗旺","福福铁扇子宋清","Super理理鉄叫子乐和","淦饭团子花项虎龚旺","胡兔兔操刀鬼曹正","帅气螺蛳粉云里金刚宋万","幸运小茵小遮拦穆春","Reign阿狸中箭虎丁得孙","猫猫金眼彪施恩","欧气小熊病大虫薛永",
"稚夏摸着天杜迁","江琪琦打虎将李忠","乌梅酱小霸王周通","下周六鬼脸儿杜兴","小喵干脆面金钱豹子汤隆","下小雨出林龙邹渊","小冻梨独角龙邹润","屺鱼七一枝花蔡庆","茶冻铁臂膊蔡福","小丸子笑面虎朱富",
"拾壹旱地忽律朱贵","小怡催命判官李立","子柔青眼虎李云","晴雨没面目焦挺","江莱活闪婆王定六","沐沐母大虫顾大嫂","小小雨石将军石勇","小one菜园子张青","芋圆母夜叉孙二娘","崽崽小尉迟孙新",
"小梨险道神郁保四","吱吱白日鼠白胜","小喵鼓上蚤时迁","程程程金毛犬段景住",
]
assert len(web_rooms) == len(web_rows_text)

room_to_name = {}
for room, name in zip(web_rooms, web_rows_text):
    room_to_name[room] = name

def norm(s):
    return "".join(ch for ch in s if not ch.isspace()).replace("☆","")

print("=== 按房号逐条校验 DB名 vs 网页名 ===")
mismatch = 0
for rid in sorted(db_name.keys()):
    room = db_room.get(rid)
    dbn = norm(db_name.get(rid, ""))
    if room is None:
        print(f"[NO-ROOM] id={rid} db='{dbn}'")
        mismatch += 1
        continue
    wname = norm(room_to_name.get(room, ""))
    # 宽松校验: DB名 或 网页名 一方能模糊对应
    ok = False
    # 提取公共字面核心(取DB名较长部分)
    if wname and dbn:
        db_main = dbn.lower()
        web_main = wname.lower()
        # 检查 DB名去掉尾部梁山绰号后 是否在网页名出现
        for tok in [db_main, web_main]:
            pass
        # 简单包含检查: DB 名完整出现在网页名，或网页主名字出现在DB名
        if db_main in web_main or web_main in db_main:
            ok = True
    if not ok:
        print(f"[MISMATCH] id={rid} room={room} db='{dbn}' web='{wname}'")
        mismatch += 1
    else:
        print(f"[OK] id={rid} room={room} db='{dbn}' web='{wname}'")

print(f"\n结果: {mismatch} 条不匹配（含无房号）")