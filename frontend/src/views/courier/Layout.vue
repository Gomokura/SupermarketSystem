<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-left">
        <span class="title">配送员</span>
        <el-tag :type="isOnline ? 'success' : 'info'" size="small" class="status-tag">
          {{ isOnline ? '在线' : '离线' }}
        </el-tag>
      </div>
      <div class="header-right">
        <span class="admin-name">{{ courierName }} · 今日{{ todayCount }}单</span>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button text type="primary" class="menu-btn">
            <el-icon><User /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="toggle">
                {{ isOnline ? '设为离线' : '设为在线' }}
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main"><router-view /></el-main>

    <!-- 个人信息弹窗 -->
    <el-dialog v-model="profileVisible" title="个人信息" width="320px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="姓名">{{ profile?.courierName }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ profile?.phone }}</el-descriptions-item>
        <el-descriptions-item label="今日已送">{{ profile?.todayCount }} 单</el-descriptions-item>
        <el-descriptions-item label="累计已送">{{ profile?.totalCount }} 单</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="profileVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="320px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPwd">保存</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { courierAPI } from '@/api'

const router = useRouter()
const courierName = ref('配送员')
const todayCount = ref(0)
const isOnline = ref(false)
const profile = ref(null)
const profileVisible = ref(false)
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref()

const pwdForm = ref({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' }
  ]
}

onMounted(() => loadProfile())

const loadProfile = async () => {
  try {
    const res = await courierAPI.getProfile()
    const d = res.data || {}
    courierName.value = d.courierName || '配送员'
    todayCount.value = d.todayCount || 0
    isOnline.value = d.status === 'online'
    profile.value = d
  } catch (e) {
    // ignore
  }
}

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    localStorage.removeItem('courierToken')
    router.push('/login')
  } else if (cmd === 'profile') {
    profileVisible.value = true
  } else if (cmd === 'toggle') {
    const newStatus = isOnline.value ? 'offline' : 'online'
    try {
      await courierAPI.updateStatus(newStatus)
      isOnline.value = !isOnline.value
      ElMessage.success(isOnline.value ? '已上线' : '已离线')
    } catch (e) {
      // already shown by interceptor
    }
  }
}

const submitPwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdLoading.value = true
    try {
      await courierAPI.changePassword(pwdForm.value)
      ElMessage.success('密码修改成功')
      pwdVisible.value = false
      pwdForm.value = { oldPassword: '', newPassword: '' }
    } catch (e) {
      // interceptor handles error
    } finally {
      pwdLoading.value = false
    }
  })
}
</script>

<style scoped>
.layout { height: 100vh; display: flex; flex-direction: column; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #67c23a;
  color: #fff;
  padding: 0 16px;
  flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.header-right { display: flex; align-items: center; gap: 8px; }
.title { font-size: 16px; font-weight: 600; }
.status-tag { cursor: default; }
.admin-name { font-size: 13px; opacity: 0.9; }
.menu-btn { color: #fff; }
.main { padding: 0; flex: 1; overflow-y: auto; background: #f5f5f5; }
</style>
