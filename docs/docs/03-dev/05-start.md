# 启动

## 添加 osu 账号

第一次运行时, 需要添加一个 osu 账号用于访问 web api (下载文件)

- 使用浏览器的无痕模式或者一个新浏览器中打开 [osu](https://osu.ppy.sh/) 官网, 登陆并跳转至个人主页

- 打开浏览器的开发者工具, 点击任意触发加载的元素, 比如展开 bp, 在开发者工具的 network 标签页中找到一个请求,
复制请求头中的 `Set-Cookie` 里的 `osu_session`, 通常以 `%3D` 结尾

- 访问项目的 `/api/login/user/osu` 接口, url 参数为 `code=<osu_session>`,
如果请求响应为 "用户\<osu id\>录入成功" 则添加成功

:::warning

注意: osu_session 结尾包含 `%3D`, 不要将其去除.

::: 
