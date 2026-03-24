<template>
  <div class="page-container">
    <h2>用户管理</h2>
    <el-table :data="users" border>
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '正常' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="toggleStatus(row)">
            {{ row.status === '正常' ? '冻结' : '解冻' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadUsers"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '@/api'

const users = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  try {
    const res = await adminAPI.getUsers({ pageNum: pageNum.value, pageSize: pageSize.value })
    users.value = res.data.records || res.data || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const toggleStatus = async (user) => {
  const newStatus = user.status === '正常' ? '冻结' : '正常'
  try {
    await adminAPI.updateUserStatus(user.userId, newStatus)
    ElMessage.success('操作成功')
    loadUsers()
  } catch (error) {
    console.error(error)
  }
}
</script>
