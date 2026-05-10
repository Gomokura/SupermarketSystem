<template>
  <div class="profile-wrap">
    <!-- 用户卡片 -->
    <div class="user-card">
      <div class="avatar-section">
        <el-upload :show-file-list="false" :http-request="handleAvatarUpload" accept="image/*">
          <div class="avatar-box">
            <el-avatar :size="72" :src="profileForm.avatarUrl || defaultAvatar" />
            <div class="avatar-badge">换</div>
          </div>
        </el-upload>
        <div class="user-meta">
          <div class="user-name">{{ profileForm.username || '未设置昵称' }}</div>
          <el-tag :type="memberTagType" size="small" effect="plain">{{ memberLevelText }}</el-tag>
        </div>
      </div>
      <div class="stat-row">
        <div class="stat-item" @click="$router.push('/points-logs')">
          <div class="stat-val">{{ pointsBalance }}</div>
          <div class="stat-lbl">积分</div>
        </div>
        <div class="stat-item" @click="$router.push('/favorites')">
          <div class="stat-val">{{ favoriteCount }}</div>
          <div class="stat-lbl">收藏</div>
        </div>
        <div class="stat-item" @click="$router.push('/coupons')">
          <div class="stat-val">{{ couponCount }}</div>
          <div class="stat-lbl">优惠券</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="menu-section">
      <div class="menu-item" v-for="entry in menuEntries" :key="entry.label" @click="$router.push(entry.path)">
        <span class="menu-icon">{{ entry.icon }}</span>
        <span class="menu-label">{{ entry.label }}</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <!-- 基本信息 -->
    <div class="section-card">
      <div class="section-title">基本信息</div>
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-position="left" label-width="72px">
        <el-form-item label="昵称" prop="username">
          <el-input v-model="profileForm.username" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="profileForm.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
            <el-radio value="未">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker v-model="profileForm.birthday" type="date" placeholder="选择生日"
            format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-button type="primary" :loading="profileLoading" @click="saveProfile" style="width:100%;margin-top:4px">
          保存修改
        </el-button>
      </el-form>
    </div>

    <!-- 修改密码 -->
    <div class="section-card">
      <div class="section-title">修改密码</div>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="left" label-width="72px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
        <el-button type="warning" :loading="pwdLoading" @click="changePassword" style="width:100%;margin-top:4px">
          修改密码
        </el-button>
      </el-form>
    </div>

    <!-- 退出登录 -->
    <div class="logout-btn" @click="handleLogout">退出登录</div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authAPI, pointsAPI, favoriteAPI, couponAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const profileFormRef = ref()
const pwdFormRef = ref()
const profileLoading = ref(false)
const pwdLoading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const pointsBalance = ref(0)
const favoriteCount = ref(0)
const couponCount = ref(0)

const profileForm = reactive({
  username: '', realName: '', phone: '', avatarUrl: '', gender: '未', birthday: '', email: ''
})

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const menuEntries = [
  { icon: '📦', label: '我的订单', path: '/orders' },
  { icon: '❤️', label: '我的收藏', path: '/favorites' },
  { icon: '🎫', label: '我的优惠券', path: '/coupons' },
  { icon: '💎', label: '积分明细', path: '/points-logs' },
  { icon: '📍', label: '收货地址', path: '/address' },
  { icon: '🔄', label: '我的售后', path: '/after-sale' },
  { icon: '⭐', label: '我的评价', path: '/orders' },
]

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}

const profileRules = {
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirmPassword, trigger: 'blur' }]
}

const memberLevel = computed(() => {
  const p = pointsBalance.value
  if (p >= 10000) return '钻石'
  if (p >= 5000) return '金卡'
  if (p >= 1000) return '银卡'
  return '普通'
})

const memberLevelText = computed(() => `${memberLevel.value}会员`)
const memberTagType = computed(() => ({ '普通': 'info', '银卡': 'success', '金卡': 'warning', '钻石': 'danger' })[memberLevel.value] || 'info')

onMounted(async () => {
  await loadUserInfo()
  await Promise.all([loadPoints(), loadFavorites(), loadCoupons()])
})

const loadUserInfo = async () => {
  try {
    const res = await authAPI.getUserInfo()
    const data = res.data || res
    Object.assign(profileForm, {
      username: data.username || data.nickname || '',
      realName: data.realName || '',
      phone: data.phone || '',
      avatarUrl: data.avatarUrl || '',
      gender: (data.gender === '未知' || data.gender === '保密') ? '未' : (data.gender || '未'),
      birthday: data.birthday || '',
      email: data.email || ''
    })
    userStore.setUserInfo(data)
  } catch (e) { console.error(e) }
}

const loadPoints = async () => {
  try {
    const res = await pointsAPI.getMyPoints()
    pointsBalance.value = res.data?.balance ?? 0
  } catch (e) { console.error(e) }
}

const loadFavorites = async () => {
  try {
    const res = await favoriteAPI.getMyFavorites()
    favoriteCount.value = res.data?.length || 0
  } catch (e) { console.error(e) }
}

const loadCoupons = async () => {
  try {
    const res = await couponAPI.getMyCoupons()
    couponCount.value = res.data?.length || 0
  } catch (e) { console.error(e) }
}

const saveProfile = async () => {
  await profileFormRef.value.validate()
  profileLoading.value = true
  try {
    await authAPI.updateUserInfo({ ...profileForm })
    userStore.setUserInfo({ ...userStore.userInfo, ...profileForm })
    ElMessage.success('保存成功')
  } catch (e) { console.error(e) }
  finally { profileLoading.value = false }
}

const handleAvatarUpload = async ({ file }) => {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await fetch('/api/upload', {
      method: 'POST',
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      body: formData
    }).then(r => r.json())
    if (res.code === 200) {
      profileForm.avatarUrl = res.data
      ElMessage.success('头像上传成功，请保存')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) { ElMessage.error('上传失败') }
}

const changePassword = async () => {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await authAPI.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    pwdFormRef.value.resetFields()
  } catch (e) { console.error(e) }
  finally { pwdLoading.value = false }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  } catch {}
}
</script>

<style scoped>
.profile-wrap { padding: 0 0 80px; background: #f5f5f5; min-height: 100%; }

.user-card {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: #fff; padding: 20px 16px 0;
}

.avatar-section { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }

.avatar-box { position: relative; cursor: pointer; }
.avatar-badge {
  position: absolute; bottom: 0; right: 0;
  background: rgba(0,0,0,0.5); color: #fff;
  font-size: 10px; border-radius: 6px; padding: 1px 4px;
}

.user-name { font-size: 17px; font-weight: bold; margin-bottom: 4px; }

.stat-row {
  display: flex; background: rgba(255,255,255,0.15);
  border-radius: 10px 10px 0 0; margin: 0 -16px; padding: 12px 0;
}
.stat-item { flex: 1; text-align: center; cursor: pointer; }
.stat-val { font-size: 18px; font-weight: bold; }
.stat-lbl { font-size: 11px; opacity: 0.85; margin-top: 2px; }

.menu-section {
  background: #fff; margin-bottom: 10px;
}
.menu-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; border-bottom: 1px solid #f5f5f5; cursor: pointer;
}
.menu-item:last-child { border-bottom: none; }
.menu-icon { font-size: 18px; width: 24px; text-align: center; }
.menu-label { flex: 1; font-size: 14px; color: #333; }
.menu-arrow { color: #ccc; font-size: 18px; }

.section-card {
  background: #fff; margin-bottom: 10px; padding: 16px;
}
.section-title { font-size: 15px; font-weight: bold; color: #222; margin-bottom: 14px; }

.logout-btn {
  margin: 16px; padding: 13px;
  background: #fff; border-radius: 8px;
  text-align: center; color: #ff4d4f;
  font-size: 15px; cursor: pointer;
  border: 1px solid #ffd6d6;
}
.logout-btn:active { opacity: 0.8; }
</style>
