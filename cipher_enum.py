import ssl, socket

host = "14.119.108.22"
port = 8501

# 获取所有可用的 TLS1.2 密码
ctx0 = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ctx0.set_ciphers("ALL:@SECLEVEL=0")
all_ciphers = ctx0.get_ciphers()
print(f"total ciphers: {len(all_ciphers)}")

for c in all_ciphers:
    name = c['name']
    try:
        ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
        ctx.minimum_version = ssl.TLSVersion.TLSv1_2
        ctx.maximum_version = ssl.TLSVersion.TLSv1_2
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE
        ctx.set_ciphers(name)
        with socket.create_connection((host, port), timeout=5) as sock:
            with ctx.wrap_socket(sock, server_hostname="danmuproxy.douyu.com") as ss:
                print(f"SUCCESS cipher={name} proto={ss.version()} negotiated={ss.cipher()[0]}")
                break
    except Exception as e:
        pass
else:
    print("NO cipher succeeded (TLSv1.2)")