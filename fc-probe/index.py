# -*- coding: utf-8 -*-
"""
阿里云函数计算(FC) 最小连通性验证函数 —— Web函数(Flask)版。

用途：验证阿里云 FC 的出口 IP 能否访问 doseeing.com，确认是否被反爬封禁。
用法：部署后通过 HTTP 触发器访问：
  https://<触发地址>/probe?room=9314167&hours=today&cookie=<可选>

返回：
  - data 含 doseeing 的 stats 数据 -> 阿里云 IP 未被封，方案可行
  - HTTP 403 / 返回验证码 / 超时   -> 阿里云 IP 可能被封，需换思路
"""
import json
import urllib.request
import urllib.error

from flask import Flask
from flask import request
from flask import jsonify

app = Flask(__name__)

BASE_URL = "https://www.doseeing.com"

# hours 参数 -> 在看斗鱼 rank 接口的 dt 参数（dt=0 今日、dt=1 昨日、dt=thismonth 本月）
HOURS_TO_DT = {"today": "0", "yesterday": "1", "thismonth": "thismonth"}


def fetch_online_minutes(room, dt, cookie):
    """抓取在看斗鱼 rank 接口的 online.minutes（周期内真实开播分钟数）；失败返回 None。

    room_stat 接口 meta 的昨日数据返回整个周期的分钟数（1440），并非真实开播时长，
    故以 rank 接口的 online.minutes 为准修正 meta。rank 接口需登录 Cookie。
    """
    url = "%s/data/api/rank?rids=%s&dt=%s&rank_type=chat_pv" % (BASE_URL, room, dt)
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0",
        "Accept": "application/json, text/javascript, */*; q=0.01",
        "Referer": "%s/data/room/%s?type=gift&dt=%s" % (BASE_URL, room, dt),
    }
    if cookie:
        headers["Cookie"] = cookie
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode("utf-8", "ignore"))
        rows = (data.get("result") or {}).get("result") or []
        if not rows:
            return None
        minutes = rows[0].get("online.minutes")
        return int(minutes) if minutes is not None else None
    except Exception:
        return None


@app.route('/', defaults={'path': ''})
@app.route('/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
def probe(path):
    room = request.args.get('room', '9314167')
    platform = request.args.get('platform', 'douyu')
    cookie = request.args.get('cookie', '')

    # 虎牙：请求在看虎牙 rank 接口（dt=0 今日 / 1 昨日 / thismonth 本月），与斗鱼共用同一代理配置
    if platform == 'huya':
        dt = request.args.get('dt', '0')
        url = "%s/huya/data/api/rank?rids=%s&dt=%s&rank_type=chat_pv" % (BASE_URL, room, dt)
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0",
            "Accept": "application/json, text/javascript, */*; q=0.01",
            "Referer": "%s/huya/data/room/%s?type=gift&dt=%s" % (BASE_URL, room, dt),
        }
        if cookie:
            headers["Cookie"] = cookie
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=20) as resp:
                body = resp.read().decode("utf-8", "ignore")
            return jsonify({
                "http_status": resp.status,
                "room": room,
                "dt": dt,
                "platform": "huya",
                "reachable": True,
                "body": body[:20000],
            })
        except urllib.error.HTTPError as e:
            return jsonify({
                "http_status": e.code,
                "reason": str(e.reason),
                "reachable": False,
                "body": e.read().decode("utf-8", "ignore")[:20000],
            }), e.code
        except Exception as e:
            return jsonify({
                "error": type(e).__name__,
                "message": str(e),
                "reachable": False,
            }), 500

    # 默认斗鱼：请求在看 room_stat 接口
    hours = request.args.get('hours', 'today')

    url = "%s/api/room_stat?room=%s&hours=%s" % (BASE_URL, room, hours)
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0",
        "Accept": "application/json, text/javascript, */*; q=0.01",
        "Referer": "%s/room/%s" % (BASE_URL, room),
    }
    if cookie:
        headers["Cookie"] = cookie

    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=20) as resp:
            body = resp.read().decode("utf-8", "ignore")
            # 用 rank 接口的真实开播时长修正 room_stat 的 meta（昨日 count=1440 不可靠）
            try:
                data = json.loads(body)
                dt = HOURS_TO_DT.get(hours)
                if dt is not None:
                    online_minutes = fetch_online_minutes(room, dt, cookie)
                    if online_minutes is not None:
                        data["meta"] = {"count": online_minutes, "unit": "minute"}
                        body = json.dumps(data, ensure_ascii=False)
            except Exception:
                pass  # 解析或修正失败时保留原始 body
            return jsonify({
                "http_status": resp.status,
                "room": room,
                "hours": hours,
                "reachable": True,
                "body": body[:20000],
            })
    except urllib.error.HTTPError as e:
        return jsonify({
            "http_status": e.code,
            "reason": str(e.reason),
            "reachable": False,
            "body": e.read().decode("utf-8", "ignore")[:20000],
        }), e.code
    except Exception as e:
        return jsonify({
            "error": type(e).__name__,
            "message": str(e),
            "reachable": False,
        }), 500


@app.route('/proxy-html', methods=['GET'])
def proxy_html():
    """代理在看站内页面类请求（如虎牙房间数据页 /huya/data/room/{rid}，用于解析公会等资料）。
    仅允许白名单路径，带可选登录 Cookie，返回 {reachable, http_status, body}。
    """
    path = request.args.get('path', '')
    referer = request.args.get('referer', '')
    cookie = request.args.get('cookie', '')
    if not path.startswith('/huya/data/room/'):
        return jsonify({"reachable": False, "http_status": 400, "body": "path not allowed"}), 400
    url = BASE_URL + path
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0",
        "Accept": "text/html,application/xhtml+xml,*/*;q=0.8",
        "Referer": referer or BASE_URL,
    }
    if cookie:
        headers["Cookie"] = cookie
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=20) as resp:
            body = resp.read().decode("utf-8", "ignore")
        return jsonify({
            "http_status": resp.status,
            "path": path,
            "reachable": True,
            "body": body[:20000],
        })
    except urllib.error.HTTPError as e:
        return jsonify({
            "http_status": e.code,
            "reason": str(e.reason),
            "reachable": False,
            "body": e.read().decode("utf-8", "ignore")[:20000],
        }), e.code
    except Exception as e:
        return jsonify({
            "error": type(e).__name__,
            "message": str(e),
            "reachable": False,
        }), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9000)