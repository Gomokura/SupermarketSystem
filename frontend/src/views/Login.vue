<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="brand-title">超市管理系统</h1>
      <p class="brand-subtitle">Smart Supermarket Management</p>
      <div class="entry-hint">选择端口后会自动填入演示账号，登录后进入对应工作台</div>

      <div class="role-selector">
        <div
          v-for="role in roles"
          :key="role.id"
          class="role-card"
          :class="{ active: activeRole === role.id }"
          @click="selectRole(role.id)"
        >
          <span class="role-icon">{{ role.icon }}</span>
          <span class="role-name">{{ role.name }}</span>
          <span class="role-desc">{{ role.desc }}</span>
          <span class="role-entry">{{ role.entry }}</span>
        </div>
      </div>

      <!-- 当前角色账号提示 -->
      <div class="account-hint">
        <el-alert type="info" :closable="false" style="padding: 8px 12px">
          <div style="display:flex; justify-content:space-between; align-items:center">
            <span>
              <strong>{{ currentRoleInfo.name }}</strong>测试账号：
              <code>{{ currentRoleInfo.username }}</code> /
              <code>{{ currentRoleInfo.password }}</code>
              <em>进入 {{ currentRoleInfo.entry }}</em>
            </span>
            <el-button size="small" link type="primary" @click="fillTestAccount">一键填入</el-button>
          </div>
        </el-alert>
      </div>

      <el-form :model="loginForm" :rules="activeRules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            :placeholder="activeRole === 'customer' ? '请输入手机号' : '请输入账号'"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            @click="handleLogin"
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="all-accounts">
        <el-collapse>
          <el-collapse-item title="查看所有测试账号" name="1">
            <table class="account-table">
              <thead>
                <tr><th>角色</th><th>账号</th><th>密码</th><th>入口</th></tr>
              </thead>
              <tbody>
                <tr v-for="r in roles" :key="r.id" :class="{ highlight: activeRole === r.id }">
                  <td>{{ r.name }}</td>
                  <td><code>{{ r.username }}</code></td>
                  <td><code>{{ r.password }}</code></td>
                  <td>{{ r.entry }}</td>
                </tr>
              </tbody>
            </table>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authAPI } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const roles = [
  { id: 'customer',  name: '顾客',  icon: '🛒', desc: '购物消费',  entry: '用户端', username: '13800138001', password: '123456' },
  { id: 'admin',     name: '管理员', icon: '👑', desc: '系统管理',  entry: '管理后台', username: 'admin', password: '123456' },
  { id: 'courier',   name: '配送员', icon: '🚴', desc: '订单配送',  entry: '配送工作台', username: '13900000001', password: '123456' },
  { id: 'cashier',   name: '收银员', icon: '💳', desc: '收银结账',  entry: '收银台', username: 'cashier01', password: '123456' },
  { id: 'warehouse', name: '仓储',   icon: '📦', desc: '库存管理',  entry: '仓储后台', username: 'admin', password: '123456' },
  { id: 'dashboard', name: '看板',   icon: '📊', desc: '数据分析',  entry: '数据看板', username: 'admin', password: '123456' },
]

const roleIds = roles.map(role => role.id)
const initialRole = roleIds.includes(route.query.role) ? route.query.role : 'customer'
const activeRole = ref(initialRole)
const initialRoleInfo = roles.find(role => role.id === initialRole) || roles[0]
const loginForm = reactive({ username: initialRoleInfo.username, password: initialRoleInfo.password })

const currentRoleInfo = computed(() => roles.find(r => r.id === activeRole.value) || roles[0])

const selectRole = (roleId) => {
  activeRole.value = roleId
  fillTestAccount()
}

const fillTestAccount = () => {
  const role = currentRoleInfo.value
  loginForm.username = role.username
  loginForm.password = role.password
  formRef.value?.clearValidate()
}

// Auto-fill when role changes
watch(activeRole, () => fillTestAccount())
watch(() => route.query.role, (role) => {
  if (roleIds.includes(role)) activeRole.value = role
})

const activeRules = computed(() => {
  if (activeRole.value === 'customer') {
    return {
      username: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$|^\w{2,20}$/, message: '请输入有效的手机号或账号', trigger: 'blur' }
      ],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }
  }
  return {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
})

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    // 顾客登录：手机号放 phone 字段，其他角色放 username
    const isPhone = /^1[3-9]\d{9}$/.test(loginForm.username)
    const loginData = activeRole.value === 'customer'
      ? { phone: isPhone ? loginForm.username : undefined, username: isPhone ? undefined : loginForm.username, password: loginForm.password }
      : { username: loginForm.username, password: loginForm.password }
    let result

    if (activeRole.value === 'customer') {
      result = await authAPI.login(loginData)
      userStore.login('customer', {
        id: result.data.userId,
        name: result.data.nickname || result.data.username,
        role: 'customer',
        username: result.data.username,
        points: result.data.points,
        memberLevel: result.data.memberLevel,
        phone: result.data.phone
      })
      userStore.setToken(result.data.token)
    } else if (['admin', 'cashier', 'warehouse', 'dashboard'].includes(activeRole.value)) {
      result = await authAPI.adminLogin(loginData)
      const selectedRole = activeRole.value
      userStore.login(selectedRole, {
        id: result.data.adminId,
        name: result.data.realName || result.data.username,
        role: selectedRole,
        username: result.data.username
      })
      userStore.setAdminToken(result.data.token)
    } else if (activeRole.value === 'courier') {
      result = await authAPI.courierLogin(loginData)
      userStore.login('courier', {
        id: result.data.courierId,
        name: result.data.courierName,
        role: 'courier',
        username: result.data.courierName,
        phone: result.data.phone,
        status: result.data.status
      })
      userStore.setCourierToken(result.data.token)
    }

    ElMessage.success('登录成功')

    const redirectMap = {
      customer: '/', admin: '/admin', cashier: '/pos',
      courier: '/courier', warehouse: '/admin', dashboard: '/admin'
    }
    router.push(redirectMap[activeRole.value])
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 480px;
  padding: 36px 32px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.brand-title {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
  text-align: center;
}

.brand-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  text-align: center;
  margin-bottom: 8px;
}

.entry-hint {
  margin-bottom: 22px;
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.72);
  text-align: center;
  font-size: 12px;
}

.role-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.role-card {
  min-height: 96px;
  padding: 10px 6px;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid transparent;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.role-card:hover {
  background: rgba(255, 255, 255, 0.12);
}

.role-card.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.25);
}

.role-icon {
  display: block;
  font-size: 20px;
  margin-bottom: 2px;
}

.role-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 2px;
}

.role-desc {
  display: block;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
}

.role-entry {
  display: inline-block;
  margin-top: 6px;
  padding: 2px 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.09);
  color: rgba(255, 255, 255, 0.68);
  font-size: 10px;
}

.account-hint em {
  margin-left: 8px;
  color: #64748b;
  font-style: normal;
}

.account-hint {
  margin-bottom: 16px;
}

.login-form {
  margin-top: 4px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 10px;
  height: 44px;
}

.all-accounts {
  margin-top: 4px;
}

.all-accounts :deep(.el-collapse) {
  border: none;
  background: transparent;
}

.all-accounts :deep(.el-collapse-item__header) {
  background: transparent;
  color: rgba(255,255,255,0.6);
  font-size: 12px;
  border: none;
  padding: 0;
  height: 32px;
}

.all-accounts :deep(.el-collapse-item__wrap) {
  background: transparent;
  border: none;
}

.all-accounts :deep(.el-collapse-item__content) {
  padding-bottom: 0;
}

.account-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  color: rgba(255,255,255,0.8);
}

.account-table th {
  padding: 6px 8px;
  text-align: left;
  border-bottom: 1px solid rgba(255,255,255,0.15);
  color: rgba(255,255,255,0.5);
  font-weight: 500;
}

.account-table td {
  padding: 6px 8px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.account-table tr.highlight td {
  color: #79a8ff;
}

.account-table code {
  background: rgba(255,255,255,0.1);
  padding: 1px 6px;
  border-radius: 4px;
  font-family: monospace;
}

@media (max-width: 560px) {
  .login-card {
    padding: 28px 18px;
  }

  .role-selector {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
