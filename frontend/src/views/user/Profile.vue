<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="mb-20">
          <template #header>
            <div class="card-header">
              <span>鍩烘湰淇℃伅</span>
            </div>
          </template>
          <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
            <el-form-item label="鐢ㄦ埛鍚?>
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="澶村儚">
              <div class="avatar-wrapper">
                <el-avatar :size="80" :src="profileForm.avatarUrl || defaultAvatar" />
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :http-request="handleAvatarUpload"
                  accept="image/*"
                >
                  <el-button size="small" type="primary" class="mt-10">鏇存崲澶村儚</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="鐪熷疄濮撳悕" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="璇疯緭鍏ョ湡瀹炲鍚? />
            </el-form-item>
            <el-form-item label="鎬у埆" prop="gender">
              <el-radio-group v-model="profileForm.gender">
                <el-radio label="鐢?>鐢?/el-radio>
                <el-radio label="濂?>濂?/el-radio>
                <el-radio label="鏈煡">鏈煡</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="鐢熸棩" prop="birthday">
              <el-date-picker
                v-model="profileForm.birthday"
                type="date"
                placeholder="閫夋嫨鐢熸棩"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="閭" prop="email">
              <el-input v-model="profileForm.email" placeholder="璇疯緭鍏ラ偖绠? />
            </el-form-item>
            <el-form-item label="鎵嬫満鍙? prop="phone">
              <el-input v-model="profileForm.phone" placeholder="璇疯緭鍏ユ墜鏈哄彿" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="saveProfile">淇濆瓨淇敼</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="mb-20">
          <template #header>
            <div class="card-header">
              <span>浼氬憳淇℃伅</span>
            </div>
          </template>
          <div class="member-info">
            <div class="member-avatar">
              <el-avatar :size="64" :src="profileForm.avatarUrl || defaultAvatar" />
            </div>
            <div class="member-details">
              <div class="member-name">{{ profileForm.username }}</div>
              <el-tag :type="memberTagType" class="mt-5">{{ memberLevelText }}</el-tag>
            </div>
          </div>
          <el-divider />
          <div class="stat-item">
            <div class="stat-label">褰撳墠绉垎</div>
            <div class="stat-value">{{ pointsBalance }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">鎴戠殑鏀惰棌</div>
            <div class="stat-value">{{ favoriteCount }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">鎴戠殑浼樻儬鍒?/div>
            <div class="stat-value">{{ couponCount }}</div>
          </div>
        </el-card>

        <el-card>
          <template #header>
            <div class="card-header">
              <span>淇敼瀵嗙爜</span>
            </div>
          </template>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px">
            <el-form-item label="鏃у瘑鐮? prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="璇疯緭鍏ユ棫瀵嗙爜" />
            </el-form-item>
            <el-form-item label="鏂板瘑鐮? prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="璇疯緭鍏ユ柊瀵嗙爜" />
            </el-form-item>
            <el-form-item label="纭瀵嗙爜" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="璇峰啀娆¤緭鍏ユ柊瀵嗙爜" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="pwdLoading" @click="changePassword">淇敼瀵嗙爜</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { authAPI, pointsAPI, favoriteAPI, couponAPI } from '@/api'
import { useUserStore } from '@/stores/user'

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
  username: '',
  realName: '',
  phone: '',
  avatarUrl: '',
  gender: '鏈煡',
  birthday: '',
  email: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('涓ゆ杈撳叆鐨勫瘑鐮佷笉涓€鑷?))
  } else {
    callback()
  }
}

const profileRules = {
  email: [
    { type: 'email', message: '璇疯緭鍏ユ纭殑閭鏍煎紡', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '璇疯緭鍏ユ纭殑鎵嬫満鍙?, trigger: 'blur' }
  ]
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '璇疯緭鍏ユ棫瀵嗙爜', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '璇疯緭鍏ユ柊瀵嗙爜', trigger: 'blur' },
    { min: 6, message: '瀵嗙爜闀垮害涓嶈兘灏戜簬6浣?, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '璇峰啀娆¤緭鍏ユ柊瀵嗙爜', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const memberLevel = computed(() => {
  const p = pointsBalance.value
  if (p >= 10000) return '閽荤煶'
  if (p >= 5000) return '閲戝崱'
  if (p >= 1000) return '閾跺崱'
  return '鏅€?
})

const memberLevelText = computed(() => `${memberLevel.value}浼氬憳`)

const memberTagType = computed(() => {
  const map = { '鏅€?: 'info', '閾跺崱': 'success', '閲戝崱': 'warning', '閽荤煶': 'danger' }
  return map[memberLevel.value] || 'info'
})

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
      gender: data.gender || '鏈煡',
      birthday: data.birthday || '',
      email: data.email || ''
    })
    userStore.setUserInfo(data)
  } catch (error) {
    console.error(error)
  }
>>>>>>> Stashed changes
}

const loadPoints = async () => {
  try {
    const res = await pointsAPI.getMyPoints()
    pointsBalance.value = res.data?.balance ?? 0
  } catch (error) {
    console.error(error)
  }
}

const loadFavorites = async () => {
  try {
    const res = await favoriteAPI.getMyFavorites()
    favoriteCount.value = (res.data?.length || 0)
  } catch (error) {
    console.error(error)
  }
}

const loadCoupons = async () => {
  try {
    const res = await couponAPI.getMyCoupons()
    couponCount.value = (res.data?.length || 0)
  } catch (error) {
    console.error(error)
  }
}

const saveProfile = async () => {
  await profileFormRef.value.validate()
  profileLoading.value = true
  try {
    await authAPI.updateUserInfo({
      realName: profileForm.realName,
      phone: profileForm.phone,
      avatarUrl: profileForm.avatarUrl,
      gender: profileForm.gender,
      birthday: profileForm.birthday,
      email: profileForm.email
    })
    userStore.setUserInfo({ ...userStore.userInfo, ...profileForm })
    ElMessage.success('淇濆瓨鎴愬姛')
  } catch (error) {
    console.error(error)
  } finally {
    profileLoading.value = false
  }
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
      ElMessage.success('澶村儚涓婁紶鎴愬姛锛岃鐐瑰嚮淇濆瓨')
    } else {
      ElMessage.error(res.message || '涓婁紶澶辫触')
    }
  } catch (error) {
    ElMessage.error('涓婁紶澶辫触')
    console.error(error)
  }
}

const changePassword = async () => {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await authAPI.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('瀵嗙爜淇敼鎴愬姛')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value.resetFields()
  } catch (error) {
    console.error(error)
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}
.mb-20 {
  margin-bottom: 20px;
}
.mt-5 {
  margin-top: 5px;
}
.mt-10 {
  margin-top: 10px;
}
.card-header {
  font-weight: bold;
  font-size: 16px;
}
.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
}
.avatar-uploader {
  display: inline-block;
}
.member-info {
  display: flex;
  align-items: center;
  gap: 15px;
}
.member-name {
  font-size: 16px;
  font-weight: bold;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.stat-item:last-child {
  border-bottom: none;
}
.stat-label {
  color: #909399;
  font-size: 14px;
}
.stat-value {
  font-weight: bold;
  color: #303133;
  font-size: 16px;
}
</style>
