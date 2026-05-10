<template>
  <div class="courier-shell">
    <header class="topbar">
      <div class="brand">
        <el-button class="back-btn" circle @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="brand-mark">配</div>
        <div>
          <div class="title">配送工作台</div>
          <div class="subtitle">鲜惠超市同城履约中心</div>
        </div>
      </div>

      <div class="operator">
        <div class="operator-meta">
          <span class="name">{{ courierName }}</span>
          <span class="today">今日完成 {{ todayCount }} 单</span>
        </div>
        <el-tag :type="isOnline ? 'success' : 'info'" effect="dark" round>
          {{ isOnline ? '在线接单' : '离线' }}
        </el-tag>
        <el-button class="status-btn" :type="isOnline ? 'warning' : 'success'" @click="toggleStatus">
          {{ isOnline ? '暂停接单' : '开始接单' }}
        </el-button>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button class="icon-btn" circle>
            <el-icon><User /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="workspace">
      <router-view />
    </main>

    <el-dialog v-model="profileVisible" title="配送员信息" width="360px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="姓名">{{ profile?.courierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ profile?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ isOnline ? '在线接单' : '离线' }}</el-descriptions-item>
        <el-descriptions-item label="今日完成">{{ profile?.todayCount || 0 }} 单</el-descriptions-item>
        <el-descriptions-item label="累计完成">{{ profile?.totalCount || 0 }} 单</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="profileVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdVisible" title="修改密码" width="360px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px">
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
  </div>
</template>

<script setup>
import { ref, provide, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User } from '@element-plus/icons-vue'
import { courierAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
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
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  try {
    const res = await courierAPI.getProfile()
    const d = res.data || {}
    courierName.value = d.courierName || '配送员'
    todayCount.value = d.todayCount || 0
    isOnline.value = ['online', 'active'].includes(d.status)
    profile.value = d
  } catch (e) {
    // request interceptor shows the message
  }
}

const toggleStatus = async () => {
  const newStatus = isOnline.value ? 'offline' : 'online'
  try {
    await courierAPI.updateStatus(newStatus)
    isOnline.value = newStatus === 'online'
    if (profile.value) profile.value.status = newStatus
    ElMessage.success(isOnline.value ? '已开始接单' : '已暂停接单')
  } catch (e) {
    // request interceptor shows the message
  }
}

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/login?role=courier')
}

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
    return
  }
  if (cmd === 'profile') profileVisible.value = true
  if (cmd === 'password') pwdVisible.value = true
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
      // request interceptor shows the message
    } finally {
      pwdLoading.value = false
    }
  })
}

provide('reloadCourierProfile', loadProfile)
onMounted(loadProfile)
</script>

<style scoped>
.courier-shell {
  min-height: 100vh;
  background: #eef2f7;
  color: #172033;
}

.topbar {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: #111827;
  color: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, .18);
}

.brand,
.operator {
  display: flex;
  align-items: center;
}

.brand {
  gap: 12px;
}

.back-btn {
  background: #273244;
  border-color: #38465d;
  color: #fff;
}

.back-btn:hover {
  background: #344258;
  border-color: #4a5a73;
  color: #fff;
}

.brand-mark {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #f59e0b;
  color: #111827;
  font-weight: 800;
  font-size: 20px;
}

.title {
  font-size: 20px;
  font-weight: 800;
}

.subtitle {
  margin-top: 3px;
  font-size: 12px;
  color: #aeb7c7;
}

.operator {
  gap: 12px;
}

.operator-meta {
  text-align: right;
  line-height: 1.4;
}

.name {
  display: block;
  font-weight: 700;
}

.today {
  display: block;
  font-size: 12px;
  color: #aeb7c7;
}

.status-btn {
  min-width: 88px;
}

.icon-btn {
  background: #273244;
  border-color: #38465d;
  color: #fff;
}

.workspace {
  min-height: calc(100vh - 72px);
  padding: 22px 24px 28px;
}

@media (max-width: 720px) {
  .topbar {
    height: auto;
    min-height: 72px;
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
    padding: 14px 16px;
  }

  .operator {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .operator-meta {
    text-align: left;
  }

  .workspace {
    padding: 14px;
  }
}
</style>
