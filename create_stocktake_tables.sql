-- 创建库存盘点任务表
CREATE TABLE STOCKTAKE_TASKS (
    task_id     NUMBER PRIMARY KEY,
    scope       VARCHAR2(20) DEFAULT 'all',
    category_id NUMBER,
    status      VARCHAR2(20) DEFAULT 'pending',
    creator_id  NUMBER,
    create_time DATE DEFAULT SYSDATE,
    submit_time DATE
);
CREATE SEQUENCE seq_stocktake START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE STOCKTAKE_TASKS IS '库存盘点任务表';

-- 创建盘点明细表
CREATE TABLE STOCKTAKE_ITEMS (
    id           NUMBER PRIMARY KEY,
    task_id      NUMBER NOT NULL,
    product_id   NUMBER NOT NULL,
    book_stock   NUMBER NOT NULL,
    actual_stock NUMBER,
    difference   NUMBER,
    diff_reason  VARCHAR2(200),
    CONSTRAINT fk_si_task    FOREIGN KEY (task_id)    REFERENCES STOCKTAKE_TASKS(task_id),
    CONSTRAINT fk_si_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);
CREATE SEQUENCE seq_stocktake_item START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_si_task ON STOCKTAKE_ITEMS(task_id);
COMMENT ON TABLE STOCKTAKE_ITEMS IS '盘点明细表';

COMMIT;
/