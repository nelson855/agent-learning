# 数据库规范
Tags: database, sqlite, rules

任务系统使用 SQLite 作为唯一数据库。
SQLite 是本地文件数据库，数据文件放在 ./data/ 目录。
建表必须使用 CREATE TABLE IF NOT EXISTS，并在启动时自动初始化 schema。
禁止引入 MySQL、PostgreSQL 等外部数据库服务器。
SQL 保持简单可读，不使用 ORM。
