# E世界-生存服服务端

## 目录结构

```text
survival/
├── docker-compose.yml
├── Dockerfile
├── db.sh                      # 数据库：初始化 + 迁移
├── env.example
├── docker/docker-entrypoint.sh
├── sql/
│   ├── schema/
│   │   └── init_mc001.sql     # mc001 权威表结构（`./db.sh init-schema` / 可选 compose 初始化）
│   ├── dumps/                 # 仅放带时间戳或含业务数据的备份（如 phpStudy 导出）；
│   └── tools/
│       └── export-phpstudy-mc001.bat   # Windows 下从 phpStudy 导出 mc001 → sql/dumps
└── …（world、plugins、server.properties 等）
```

---

## 环境变量

复制 **`env.example`** 为 **`.env`**，与 **reference** 对齐填写 **`MYSQL_*`**、**`MYSQL_ROOT_PASSWORD`**、**`PUBLISH_PORT`** 等。复用 reference 时，若走**方式 A**（见下），**`MYSQL_PORT`** 须与 **reference 的 `MYSQL_PUBLISH_PORT`** 一致（默认 33060）。

### 与 reference 共用 MySQL：是否要配容器网络？

与 **`reference/docker-compose.yml`** 顶部注释一致，有两种常见做法：

| 方式 | 是否需要「加入 reference 的网络」 | 说明 |
|------|-------------------------------------|------|
| **A（默认）** | **否** | 使用 **`host.docker.internal`** + **reference 映射到宿主机的端口**（如 33060）。**Linux** 下需在 **`docker-compose.yml`** 中保留 **`extra_hosts: host.docker.internal:host-gateway`**（已配置）。 |
| **B（可选）** | **是** | 希望容器内使用 **`mysql:3306`**、不经过宿主机端口时，将 **survival** 挂到 **reference 的 MySQL 所在的外部网络**（`docker network ls` 查看，常为 **`reference_default`** 这类名称），并按 **`docker-compose.yml`** 文末注释取消 **`mysql_shared`** 相关配置，同时 **`.env`** 设 **`MYSQL_HOST=mysql`**、**`MYSQL_PORT=3306`**；可不再依赖 **`extra_hosts`**（仅用于连库时）。 |

---

## 数据库：`./db.sh`

无需在宿主机安装 **`mysql` 客户端**：默认用 **`mysql:8.0`** 镜像执行；若本机已安装且 **`USE_LOCAL_MYSQL_CLI=1`**，则改用本机 **`mysql`**。

| 子命令 | 说明 |
|--------|------|
| `ensure` | 创建库与应用账号（`mysql_native_password`） |
| `init-schema` | 执行 **`sql/schema/init_mc001.sql`** |
| `import [文件]` | 导入备份 SQL；未指定文件时用 **`.env` 的 `MIGRATION_SQL`**（常用 **`sql/dumps/*.sql`**） |
| `bootstrap` | `ensure` + `init-schema`（空库一键） |
| `help` | 简要帮助 |

```bash
cd eworld-survival
cp env.example .env
chmod +x db.sh
./db.sh bootstrap                     # 空库
./db.sh import sql/dumps/某文件.sql   # 导入迁移
```

若 **`docker-compose.yml`** 里已**开启本项目的 `mysql` 服务**，脚本会优先 **`docker compose exec mysql`**，此时 **`.env`** 中应为 **`MYSQL_HOST=mysql`、`MYSQL_PORT=3306`**。

---

## Windows：从 phpStudy 导出

在仍能访问 phpStudy MySQL 的机器上运行 **`sql/tools/export-phpstudy-mc001.bat`**（按需修改 bat 内 **`PHPMYSQL_BIN`**、密码），导出文件落在 **`sql/dumps/`**，再在 Linux 上 **`./db.sh import`**。

---

## 可选：使用独立的 MySQL 数据库实例

见 **`docker-compose.yml`** 注释：取消 **`mysql` 服务、`depends_on`、`volumes`** 等行的注释，并按文件头说明调整 **`.env`** 与 **`survival` 的 `depends_on`/`extra_hosts`**。首次初始化可挂载 **`sql/schema/init_mc001.sql`** 到 **`docker-entrypoint-initdb.d`**（路径已在注释块中）。

---

## 启动游戏服务器

```bash
docker compose build
docker compose up -d
```
