-- Issue #19: MySQL 替换 H2 — 数据库初始化脚本
-- 数据库：kskbl（由用户手动创建）
-- 执行方式：mysql -u root -p123456 < docs/sql/issue19-init.sql

CREATE DATABASE IF NOT EXISTS kskbl DEFAULT CHARACTER SET utf8mb4;
USE kskbl;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 存档表
CREATE TABLE IF NOT EXISTS store_text (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    data TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
