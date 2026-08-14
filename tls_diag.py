import socket, ssl, sys

host = "14.119.108.22"
port = 8501

contexts = {
    "TLSv1.2": ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT),
    "TLSv1.3": ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT),
}
contexts["TLSv1.2"].minimum_version = ssl.TLSVersion.TLSv1_2
contexts["TLSv1.2"].maximum_version = ssl.TLSVersion.TLSv1_2
contexts["TLSv1.3"].minimum_version = ssl.TLSVersion.TLSv1_3
contexts["TLSv1.3"].maximum_version = ssl.TLSVersion.TLSv1_3

for name, ctx in contexts.items():
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    try:
        with socket.create_connection((host, port), timeout=6) as sock:
            with ctx.wrap_socket(sock, server_hostname="danmuproxy.douyu.com") as ss:
                print(f"[{name}] SUCCESS proto={ss.version()} cipher={ss.cipher()}")
    except Exception as e:
        print(f"[{name}] FAIL: {e}")

# TLS1.2 auto cipher - print server-selected manually
print("=== TLSv1.2 full handshake with default ciphers ===")
ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ctx.minimum_version = ssl.TLSVersion.TLSv1_2
ctx.maximum_version = ssl.TLSVersion.TLSv1_2
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE
try:
    with socket.create_connection((host, port), timeout=6) as sock:
        with ctx.wrap_socket(sock, server_hostname="danmuproxy.douyu.com") as ss:
            print(f"SUCCESS proto={ss.version()} cipher={ss.cipher()}")
except Exception as e:
    print(f"FAIL: {e}")