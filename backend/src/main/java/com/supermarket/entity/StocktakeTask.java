package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("STOCKTAKE_TASKS")
public class StocktakeTask {
    @TableId(type = IdType.AUTO)
    private Integer taskId;
    /** 盘点范围：all全部 / category按分类 */
    private String scope;
    private Integer categoryId;   // scope=category时指定
    /** 状态：pending待盘点 / counting盘点中 / done已完成 */
    private String status;
    private Integer creatorId;
    private Date createTime;
    private Date submitTime;

    // 非数据库字段
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private List<StocktakeItem> items;
}
