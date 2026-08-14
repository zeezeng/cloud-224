import socket, ssl

ips = ["14.119.108.22", "14.119.108.18", "115.227.36.226", "115.227.36.228"]
ports = [8501, 8502, 8503, 8504, 8505, 8506]

def tcp(ip, port):
    try:
        s = socket.create_connection((ip, port), timeout=5)
        s.close()
        return "TCP-OK"
    except Exception as e:
        return f"TCP-FAIL:{e.__class__.__name__}"

def tls(ip, port):
    ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    try:
        with socket.create_connection((ip, port), timeout=5) as sock:
            with ctx.wrap_socket(sock, server_hostname="danmuproxy.douyu.com") as ss:
                return f"TLS-OK proto={ss.version()} cipher={ss.cipher()[0]}"
    except Exception as e:
        return f"TLS-FAIL:{e.__class__.__name__}:{str(e)[:60]}"

for ip in ips:
    for port in ports:
        t = tcp(ip, port)
        if t.startswith("TCP-OK"):
            s = tls(ip, port)
            print(f"{ip}:{port}  {t}  {s}")
        else:
            print(f"{ip}:{port}  {t}")