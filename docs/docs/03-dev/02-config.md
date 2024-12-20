# 配置文件

## 文件格式

仅支持使用 `toml` 格式的文件

## 参考

```toml

# 服务配置
[server]
port = 8080
# jwt 密钥, 填写随机字符串
secret = "token"
# 本机地址
localUrl = "localhost:8800"
# 应用暂不支持开启 ssl, 仅当反向代理已经启用 https, 则需要填写 `true`
ssl = false
# (可选) 对下面域名开启跨域
cros = ["localhost:5173"]
# (可选) 管理员的 user id
adminUsers = [17064371]

# osu相关文件的存储配置
[mirror]
basePath = "/path/to/osu/"

# osu api 配置, 在自己的设置页面最底下可以申请
# redirectUri 是回调地址, 请设置成访问前端的 url 并在前端显示加载页面, 同时向后端使用 code 请求登陆接口
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

::::danger[注意]

- server.localUrl 用于访问跨域校验, 设置为实际 url
- osu.redirectUri 是 osu 登陆后的回调地址, 务必严格与 osu 设置的回调地址一致, 尤其是大小写与结尾斜杠都要一致

::::

:::tip[配置说明]

- 如果访问 osu api 时延迟过高, 可以尝试使用代理
- 数据库会使用连接池, 同时连接数大于三, 请确保数据库配置正确

:::

:::tip[2024/12/20 补充]

- 使用 docker 部署时, server.port 以及 database 配置会被忽略, 请使用环境变量配置

:::
