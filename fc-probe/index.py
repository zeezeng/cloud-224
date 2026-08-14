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


@app.route('/', defaults={'path': ''})
@app.route('/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
def probe(path):
    room = request.args.get('room', '9314167')
    hours = request.args.get('hours', 'today')
    cookie = request.args.get('cookie', '')

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
            return jsonify({
                "http_status": resp.status,
                "room": room,
                "hours": hours,
                "reachable": True,
                "body": body[:2000],
            })
    except urllib.error.HTTPError as e:
        return jsonify({
            "http_status": e.code,
            "reason": str(e.reason),
            "reachable": False,
            "body": e.read().decode("utf-8", "ignore")[:2000],
        }), e.code
    except Exception as e:
        return jsonify({
            "error": type(e).__name__,
            "message": str(e),
            "reachable": False,
        }), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9000)