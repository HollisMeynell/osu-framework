# 配置文件

## 文件格式

仅支持使用 `toml` 格式的文件

## 参考

```toml

# 服务配置
[server]
port = 8080
# jwt 密钥
secret = "token"
# 本机地址
localUrl = "localhost:8800"
# 应用暂不支持开启 ssl, 仅当反向代理已经启用 https, 则需要填写 `true`
ssl = false
# (可选) 对下面域名开启跨域
cros = ["localhost:5173"]
# (可选) 管理员的 user id
adminUsers = [17064371]

# 文件存储配置
[mirror]
basePath = "/path/to/osu/"

# osu api 配置
[osu]
redirectUri = "http://localhost:8080/api/oauth"
clientId = 727
clientToken = ""

# (可选) 代理设置
[osu.proxy]
host = "127.0.0.1"
port = 7890

# 数据库配置, 仅支持 postgresql
[database]
url = "jdbc:postgresql://127.0.0.1:5432/database"
username = "*"
password = "*"

```
