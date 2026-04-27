<template>
  <div class="page-container">
    <div class="header">
      <h2>顾客评价管理</h2>
    </div>
    <!-- 筛选 -->
    <el-card shadow="never" style="margin-bottom: 20px;">
      <el-form :inline="true">
        <el-form-item label="星级评分">
          <el-select v-model="queryParams.rating" clearable style="width: 150px">
            <el-option label="5星 (好评)" :value="5" />
            <el-option label="3星 (中评)" :value="3" />
            <el-option label="1星 (差评)" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 列表 -->
    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="reviewId" label="ID" width="80" />
      <el-table-column prop="productId" label="商品ID" width="120" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="rating" label="评分" width="150">
        <template #default="{ row }">
          <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="200" />
      <el-table-column prop="reply" label="商家回复" min-width="200" />
      <el-table-column prop="isHidden" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isHidden ? 'info' : 'success'">{{ row.isHidden ? '已隐藏' : '正常显示' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openReplyDialog(row)">回复</el-button>
          <el-button :type="row.isHidden ? 'success' : 'warning'" link size="small" @click="toggleHide(row)">
            {{ row.isHidden ? "显示" : "隐藏" }}
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row.reviewId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </div>
    <!-- 回复弹窗 -->
    <el-dialog title="回复评价" v-model="replyDialogVisible" width="400px">
      <el-form>
        <el-form-item label="回复内容">
          <el-input type="textarea" v-model="replyContent" rows="4" placeholder="请输入致顾客的回复" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue"
import { reviewsAPI } from "@/api"
import { ElMessage, ElMessageBox } from "element-plus"
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  rating: null
})
const loadData = async () => {
  loading.value = true
  try {
    const res = await reviewsAPI.getAdminList(queryParams.value)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
const handleSearch = () => { queryParams.value.pageNum = 1; loadData() }
const resetSearch = () => { queryParams.value.rating = null; handleSearch() }
// Reply logic
const replyDialogVisible = ref(false)
const currentReviewId = ref(null)
const replyContent = ref("")
const openReplyDialog = (row) => {
  currentReviewId.value = row.reviewId
  replyContent.value = row.reply || ""
  replyDialogVisible.value = true
}
const submitReply = async () => {
  try {
    await reviewsAPI.reply(currentReviewId.value, replyContent.value)
    ElMessage.success("回复成功")
    replyDialogVisible.value = false
    loadData()
  } catch (e) {}
}
const toggleHide = async (row) => {
  try {
    const nextHidden = row.isHidden ? 0 : 1
    await reviewsAPI.toggleVisibility(row.reviewId, nextHidden)
    ElMessage.success("状态已更新")
    loadData()
  } catch (e) {}
}
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm("确定要永久删除此评价吗？", "警告", { type: "error" })
    await reviewsAPI.deleteReview(id)
    ElMessage.success("删除成功")
    loadData()
  } catch (e) {}
}
onMounted(() => loadData())
</script>
<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
