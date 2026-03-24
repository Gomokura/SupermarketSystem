<template>
  <div class="page-container">
    <h2>个人中心</h2>
    <el-card>
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="userForm.role" disabled />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-input v-model="userForm.status" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userForm = reactive({
  username: '',
  realName: '',
  phone: '',
  role: '',
  status: ''
})

onMounted(() => {
  Object.assign(userForm, userStore.userInfo)
})

const saveProfile = () => {
  ElMessage.success('保存成功')
}
</script>
