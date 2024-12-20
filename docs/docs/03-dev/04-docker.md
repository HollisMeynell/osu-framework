# docker 部署

使用 docker 可以快速部署服务, 不过你可能需要一些东西在路由器上加速访问 docker hub 以及 maven 中央仓库

## 前置条件

- 安装 docker 以及 docker-compose 工具

## 使用 docker-compose 部署

### 拉取代码到本地

执行以下命令拉取代码到本地

```shell
git clone https://github.com/HollisMeynell/osu-framework.git
```

### 配置环境

- 创建一个空目录, **最好不要与项目代码在一起**
- 在目录下创建[配置文件](02-config.md#参考)
- 根据需要修改 `docker-compose.yml` 文件 (基本上只需要修改端口 PORT 即可)

:::warning[关于配置]

在使用 docker 部署时, 环境变量对 PORT / DATABASE 配置优先级最高,
在配置文件中配置的 server.port / datavase.* 会被忽略

:::

### 运行

在工作目录下执行

```shell
docker-compose -f <项目路径> up -d
```

