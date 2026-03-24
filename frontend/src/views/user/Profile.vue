<template>
  <div class="page-container">
    <h2>个人中心</h2>
    <el-row :gutter="24">
      <!-- 左：基本信息 -->
      <el-col :span="14">
        <el-card header="基本信息">
          <el-form
            :model="userForm"
            :rules="rules"
            ref="formRef"
            label-width="90px"
          >
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="userForm.role === 'admin' ? 'danger' : 'primary'">
                {{ userForm.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="账号状态">
              <el-tag :type="userForm.status === '正常' ? 'success' : 'warning'">
                {{ userForm.status || '正常' }}
              </el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右：修改密码 -->
      <el-col :span="10">
        <el-card header="修改密码">
          <el-form
            :model="pwdForm"
            :rules="pwdRules"
            ref="pwdFormRef"
            label-width="90px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                show-password
                placeholder="至少6位"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                show-password
                placeholder="再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="changePassword" :loading="changingPwd">
                确认修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()
const formRef   = ref()
const pwdFormRef = ref()
const saving    = ref(false)
const changingPwd = ref(false)

// ── 基本信息表单 ──────────────────────────────────────────
const userForm = reactive({
  username: '',
  realName: '',
  phone: '',
  role: '',
  status: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

onMounted(async () => {
  // 从 store 读（store 里有则直接用，避免多余请求）
  const info = userStore.userInfo
  if (info && info.userId) {
    Object.assign(userForm, info)
  } else {
    // 否则重新拉一次
    await userStore.fetchUserInfo()
    Object.assign(userForm, userStore.userInfo)
  }
})

const saveProfile = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await request.put('/auth/userinfo', {
      realName: userForm.realName,
      phone: userForm.phone
    })
    // 同步更新 store
    userStore.setUserInfo({ ...userStore.userInfo, realName: userForm.realName, phone: userForm.phone })
    ElMessage.success('保存成功')
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

// ── 修改密码表单 ──────────────────────────────────────────
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword:     [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword:     [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const changePassword = async () => {
  await pwdFormRef.value.validate()
  changingPwd.value = true
  try {
    await request.put('/auth/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdFormRef.value.resetFields()
    // 修改密码后退出登录
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch (e) {
    console.error(e)
  } finally {
    changingPwd.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 0 4px;
}
h2 {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}
</style>
