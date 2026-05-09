package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("INVENTORY_CHECKS")
public class StocktakeTask {
    @TableId(value = "CHECK_ID", type = IdType.INPUT)
    @TableField("check_id")
    private Integer taskId;
    /** 盘点范围：ALL全部 / CATEGORY按分类 */
    @TableField("check_scope")
    private String scope;
    @TableField("scope_category_id")
    private Integer categoryId;
    /** 状态：pending待盘点 / counting盘点中 / done已完成 */
    private String status;
    @TableField("operator_id")
    private Integer creatorId;
    @TableField("create_time")
    private Date createTime;
    @TableField("complete_time")
    private Date submitTime;

    // 非数据库字段
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private List<StocktakeItem> items;
}
