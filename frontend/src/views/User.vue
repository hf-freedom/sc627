<template>
  <div class="user">
    <h2>用户中心</h2>

    <el-card class="mb-20">
      <template #header>
        <span>当前用户</span>
      </template>
      <el-input v-model="userId" placeholder="请输入用户ID" style="width: 300px; margin-right: 10px" />
      <el-button type="primary" @click="loadUserInfo">加载用户信息</el-button>
      <div v-if="userInfo" style="margin-top: 15px">
        <p>用户名: {{ userInfo.username }}</p>
        <p>等级: {{ userInfo.level }}</p>
        <p>风险等级: {{ userInfo.riskLevel }}</p>
        <el-tag :type="userInfo.needAudit ? 'warning' : 'success'">
          {{ userInfo.needAudit ? '需要审核' : '正常状态' }}
        </el-tag>
      </div>
    </el-card>

    <el-card class="mb-20">
      <template #header>
        <span>可领取优惠券</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="template in templates" :key="template.templateId">
          <el-card class="coupon-card" :class="{ 'coupon-card-disabled': !canClaim(template) }">
            <div class="coupon-header">
              <div class="coupon-value">¥{{ template.value }}</div>
              <div class="coupon-name">{{ template.name }}</div>
            </div>
            <div class="coupon-body">
              <p>满{{ template.minAmount }}可用</p>
              <p>等级要求: {{ template.userLevelRequired }}级</p>
              <p>库存: {{ template.stock - template.claimedCount }}/{{ template.stock }}</p>
              <div class="claim-check" v-if="userInfo">
                <el-tag :type="getCheckResult(template).type" size="small">
                  {{ getCheckResult(template).message }}
                </el-tag>
              </div>
            </div>
            <div class="coupon-footer">
              <el-button type="primary" size="small" @click="claimCoupon(template.templateId)" 
                :disabled="!canClaim(template)">
                立即领取
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card>
      <template #header>
        <span>我的优惠券</span>
      </template>
      <el-table :data="userCoupons" border>
        <el-table-column prop="userCouponId" label="优惠券ID" width="250" />
        <el-table-column prop="templateId" label="模板ID" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceId" label="领取设备" width="150" />
        <el-table-column prop="claimTime" label="领取时间" width="180" />
        <el-table-column prop="expireTime" label="过期时间" width="180" />
        <el-table-column prop="useTime" label="使用时间" width="180">
          <template #default="{ row }">
            {{ row.useTime || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const userId = ref('')
const userInfo = ref(null)
const templates = ref([])
const userCoupons = ref([])

const loadUserInfo = async () => {
  try {
    const res = await axios.get(`/api/user/${userId.value}`)
    userInfo.value = res.data.data
    loadUserCoupons()
  } catch (error) {
    ElMessage.error('用户不存在')
  }
}

const loadTemplates = async () => {
  try {
    const res = await axios.get('/api/coupon/templates')
    templates.value = res.data.data.filter(t => t.active)
  } catch (error) {
    console.error(error)
  }
}

const loadUserCoupons = async () => {
  if (!userId.value) return
  try {
    const res = await axios.get(`/api/coupon/user/${userId.value}`)
    userCoupons.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const canClaim = (template) => {
  if (!userInfo.value) return false
  if (!template.active) return false
  if (template.stock - template.claimedCount <= 0) return false
  if (userInfo.value.level < template.userLevelRequired) return false
  if (userInfo.value.needAudit) return false
  const claimCount = userCoupons.value.filter(uc => uc.templateId === template.templateId).length
  if (claimCount >= template.userMaxClaim) return false
  return true
}

const getCheckResult = (template) => {
  if (!userInfo.value) return { type: 'info', message: '请先加载用户信息' }
  if (!template.active) return { type: 'danger', message: '活动已结束' }
  if (template.stock - template.claimedCount <= 0) return { type: 'danger', message: '库存不足' }
  if (userInfo.value.level < template.userLevelRequired) {
    return { type: 'warning', message: `用户等级不足，需要${template.userLevelRequired}级` }
  }
  if (userInfo.value.needAudit) return { type: 'warning', message: '用户需要审核' }
  const claimCount = userCoupons.value.filter(uc => uc.templateId === template.templateId).length
  if (claimCount >= template.userMaxClaim) {
    return { type: 'warning', message: `已达领取上限${template.userMaxClaim}张` }
  }
  return { type: 'success', message: '可以领取' }
}

const claimCoupon = async (templateId) => {
  if (!userId.value) {
    ElMessage.warning('请先输入用户ID')
    return
  }
  try {
    const deviceId = 'device_' + Date.now()
    const res = await axios.post('/api/coupon/claim', {
      templateId,
      userId: userId.value,
      deviceId
    })
    const coupon = res.data.data
    ElMessage.success(`领取成功！优惠券ID: ${coupon.userCouponId.substring(0, 8)}...`)
    loadTemplates()
    loadUserCoupons()
  } catch (error) {
    const errMsg = error.response?.data?.message || '领取失败'
    ElMessage.error(errMsg)
  }
}

const getStatusType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'CLAIMED': 'primary',
    'USED': 'info',
    'EXPIRED': 'danger',
    'RETURNED': 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'AVAILABLE': '可领取',
    'CLAIMED': '已领取',
    'USED': '已使用',
    'EXPIRED': '已过期',
    'RETURNED': '已退回'
  }
  return map[status] || status
}

onMounted(() => {
  const savedUserId = localStorage.getItem('currentUserId')
  if (savedUserId) {
    userId.value = savedUserId
    loadUserInfo()
  }
  loadTemplates()
})
</script>

<style scoped>
.mb-20 {
  margin-bottom: 20px;
}
.coupon-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}
.coupon-card-disabled {
  opacity: 0.6;
  filter: grayscale(50%);
}
.coupon-header {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
  padding: 15px;
  border-radius: 4px 4px 0 0;
  margin: -20px -20px 15px -20px;
}
.coupon-card-disabled .coupon-header {
  background: linear-gradient(135deg, #909399, #c0c4cc);
}
.coupon-value {
  font-size: 32px;
  font-weight: bold;
}
.coupon-name {
  font-size: 14px;
}
.coupon-body p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}
.claim-check {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}
.coupon-footer {
  text-align: right;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
</style>
