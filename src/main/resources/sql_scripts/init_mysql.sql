CREATE USER skillbox_engine;
CREATE DATABASE IF NOT EXISTS linkdex CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
GRANT ALL PRIVILEGES ON DATABASE linkdex TO skillbox_engine;