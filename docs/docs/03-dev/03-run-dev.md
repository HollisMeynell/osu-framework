# 开发配置

## 前置条件

- git, 用于拉取代码
- 安装 java jdk, 且 jdk 版本在 21 以上, 推荐使用:
  - [graalvm JDK](https://www.graalvm.org/downloads/) 启动速度快
  - [Microsoft JDK](https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21) 在 windows 下一键安装省心省力 
- 安装 Postgresql 数据库并给予登陆权限

## 安装 java

仅需 jdk 21 版本及以上

windows 下安装 Microsoft JDK 下载后双击安装即可.

linux / mac os 使用自带的包管理器安装.

## 数据库配置

- 创建专用数据用户:

```sql
create user <数据库用户名> with password '<数据库密码>';
```

- 创建对应数据库:

```sql
create database <数据库名> owner <数据库用户名>; 
```

- 给予权限: 

```sql
grant all privileges on database <数据库名> to <数据库用户名>;
```

## 配置文件

在项目根目录下创建 `config.toml` 文件, [文档](./02-config.md#参考)

## 运行

在项目根目录下执行

```shell
shell/run-app.sh
```

:::warning

一定是在项目根目录下执行, 否则会找不到 配置文件 / 运行文件

若出现运行失败, 提示 `rust` 相关错误, 请下载对应的 [lib](https://github.com/HollisMeynell/osu-framework/actions/workflows/BuildRs.yaml) 文件到 `spring-osu-extended/src/main/resources/lib` 目录

也可以自行安装 rust 环境, 构建脚本会自动构建 native lib.

:::
