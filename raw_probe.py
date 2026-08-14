import socket

ip = "14.119.108.22"
port = 8501

# 尝试明文 HTTP 请求
try:
    s = socket.create_connection((ip, port), timeout=5)
    s.settimeout(5)
    s.sendall(b"GET / HTTP/1.1\r\nHost: danmuproxy.douyu.com\r\nConnection: close\r\n\r\n")
    data = s.recv(200)
    print("HTTP response:", data[:200])
    s.close()
except Exception as e:
    print("HTTP fail:", e)

# 尝试发送斗鱼协议登录包（明文）
def pack(content):
    body = content.encode('utf-8')
    length = 12 + len(body) + 1
    import struct
    return struct.pack('<IHHII', length, 0x02, 0x00, 0, 0) + body + b'\x00'

try:
    s = socket.create_connection((ip, port), timeout=5)
    s.settimeout(5)
    s.sendall(pack("type@=loginreq/roomid@=12866270/"))
    s.sendall(pack("type@=joingroup/rid@=12866270/gid@=-9999/"))
    data = s.recv(200)
    print("Douyu raw response:", data[:200])
    s.close()
except Exception as e:
    print("Douyu raw fail:", e)