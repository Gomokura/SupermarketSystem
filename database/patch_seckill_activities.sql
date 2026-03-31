-- ============================================================
-- 补丁：为仅执行过 all_tables.sql 的库补建秒杀场次表
-- 在 SQL Developer / sqlplus 中以与 application.yml 相同的用户连接后执行（如 SYSTEM）
-- 若提示「名称已由现有对象使用」，说明表或序列已存在，可忽略或跳过对应语句
-- ============================================================

CREATE TABLE SECKILL_ACTIVITIES (
    seckill_id   NUMBER PRIMARY KEY,
    seckill_name VARCHAR2(100) NOT NULL,
    start_time   DATE          NOT NULL,
    end_time     DATE          NOT NULL,
    status       VARCHAR2(20)  DEFAULT 'pending',
    create_time  DATE          DEFAULT SYSDATE
);

CREATE SEQUENCE seq_seckill START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE SECKILL_ACTIVITIES IS '秒杀活动场次表';

-- 可选：插入一条占位数据，避免首页列表为空（可按需删改）
-- INSERT INTO SECKILL_ACTIVITIES (seckill_id, seckill_name, start_time, end_time, status)
-- VALUES (seq_seckill.NEXTVAL, '示例秒杀', SYSDATE, SYSDATE + 1, 'ended');
COMMIT;
