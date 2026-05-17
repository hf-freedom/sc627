<template>
  <div class="audit">
    <h2>审核管理</h2>

    <el-card class="mb-20">
      <template #header>
        <span>待审核用户</span>
      </template>
      <el-table :data="usersNeedAudit" border>
        <el-table-column prop="userId" label="用户ID" width="250" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="level" label="等级" width="100" />
        <el-table-column prop="riskLevel" label="风险等级" width="150">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" effect="dark">
              <span v-if="row.riskLevel === 'SUSPECTED'">⚠️ </span>
              <span v-else-if="row.riskLevel === 'HIGH_RISK'">🔴 </span>
              {{ getRiskLevelText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approveUser(row.userId)">
              通过审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="usersNeedAudit.length === 0" description="暂无待审核用户" />
    </el-card>

    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>待审核订单 - 异常用户优惠券使用</span>
          <el-tag type="danger" size="small" v-if="pendingOrders.length > 0">
            待审核: {{ pendingOrders.length }} 条
          </el-tag>
        </div>
      </template>
      <el-table :data="pendingOrders" border :row-class-name="getPendingRowClassName">
        <el-table-column prop="orderId" label="订单ID" width="250" />
        <el-table-column prop="userId" label="用户ID" width="150" />
        <el-table-column prop="totalAmount" label="订单金额" width="100" />
        <el-table-column prop="discountAmount" label="优惠金额" width="100" />
        <el-table-column prop="auditRemark" label="审核备注" min-width="150">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.auditRemark }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approveOrder(row.orderId)">
              审核通过
            </el-button>
            <el-button size="small" type="danger" @click="rejectOrder(row.orderId)">
              审核拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="pendingOrders.length === 0" description="暂无待审核订单" />
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>风控记录</span>
          <el-tag type="warning" size="small" v-if="suspectedCount > 0">
            疑似风险: {{ suspectedCount }} 条
          </el-tag>
        </div>
      </template>
      <el-table :data="riskRecords" border :row-class-name="getTableRowClassName">
        <el-table-column prop="recordId" label="记录ID" width="100" />
        <el-table-column prop="deviceId" label="设备ID" width="180">
          <template #default="{ row }">
            <span class="device-id">{{ row.deviceId }}</span>
            <el-tag 
              v-if="isHighFrequencyDevice(row.deviceId)" 
              type="danger" 
              size="mini" 
              style="margin-left: 5px">
              高频设备
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="250" />
        <el-table-column prop="riskLevel" label="风险等级" width="150">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" effect="dark">
              <span v-if="row.riskLevel === 'SUSPECTED'">⚠️ 疑似风险</span>
              <span v-else-if="row.riskLevel === 'HIGH_RISK'">🔴 高风险</span>
              <span v-else>🟢 正常</span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="claimCountInWindow" label="窗口内领取次数" width="150">
          <template #default="{ row }">
            <el-progress 
              :percentage="(row.claimCountInWindow / 5) * 100" 
              :color="getProgressColor(row.claimCountInWindow)"
              :stroke-width="10"
              :show-text="false"
              style="width: 80px; display: inline-block; margin-right: 10px" />
            <span :class="getCountClass(row.claimCountInWindow)">
              {{ row.claimCountInWindow }} 次
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="windowStartTime" label="窗口开始时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="150">
          <template #default="{ row }">
            <el-tag type="warning" size="small" v-if="row.remark && row.remark.includes('频繁')">
              {{ row.remark }}
            </el-tag>
            <span v-else>{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="触发时间" width="180" />
      </el-table>
      <el-empty v-if="riskRecords.length === 0" description="暂无风控记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const usersNeedAudit = ref([])
const riskRecords = ref([])
const pendingOrders = ref([])

const suspectedCount = computed(() => {
  return riskRecords.value.filter(r => r.riskLevel === 'SUSPECTED').length
})

const getRiskTagType = (riskLevel) => {
  const map = {
    'NORMAL': 'success',
    'SUSPECTED': 'warning',
    'HIGH_RISK': 'danger'
  }
  return map[riskLevel] || 'info'
}

const getRiskLevelText = (riskLevel) => {
  const map = {
    'NORMAL': '正常',
    'SUSPECTED': '疑似风险',
    'HIGH_RISK': '高风险'
  }
  return map[riskLevel] || riskLevel
}

const getTableRowClassName = ({ row }) => {
  if (row.riskLevel === 'SUSPECTED') return 'suspected-row'
  if (row.riskLevel === 'HIGH_RISK') return 'high-risk-row'
  return ''
}

const getPendingRowClassName = ({ row }) => {
  return 'pending-order-row'
}

const isHighFrequencyDevice = (deviceId) => {
  const count = riskRecords.value.filter(r => r.deviceId === deviceId).length
  return count >= 2
}

const getProgressColor = (count) => {
  if (count >= 5) return '#f56c6c'
  if (count >= 3) return '#e6a23c'
  return '#67c23a'
}

const getCountClass = (count) => {
  if (count >= 5) return 'count-danger'
  if (count >= 3) return 'count-warning'
  return 'count-normal'
}

const loadUsersNeedAudit = async () => {
  try {
    const res = await axios.get('/api/user/audit')
    usersNeedAudit.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const loadRiskRecords = async () => {
  try {
    const res = await axios.get('/api/risk/records')
    riskRecords.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const loadPendingOrders = async () => {
  try {
    const res = await axios.get('/api/order/audit/pending')
    pendingOrders.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const approveUser = async (userId) => {
  try {
    await axios.post(`/api/user/approve/${userId}`)
    ElMessage.success('审核通过')
    loadUsersNeedAudit()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const approveOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认审核通过该订单？优惠券使用将生效', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.post(`/api/order/audit/${orderId}`, {
      auditResult: 'APPROVED',
      auditRemark: '人工审核通过'
    })
    ElMessage.success('审核通过')
    loadPendingOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const rejectOrder = async (orderId) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因'
    })
    await axios.post(`/api/order/audit/${orderId}`, {
      auditResult: 'REJECTED',
      auditRemark: value || '人工审核拒绝，优惠券已退回'
    })
    ElMessage.success('审核拒绝，优惠券已退回用户')
    loadPendingOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadUsersNeedAudit()
  loadRiskRecords()
  loadPendingOrders()
})
</script>

<style scoped>
.mb-20 {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
:deep(.suspected-row) {
  background-color: #fdf6ec !important;
}
:deep(.suspected-row:hover) {
  background-color: #faecd8 !important;
}
:deep(.high-risk-row) {
  background-color: #fef0f0 !important;
}
:deep(.high-risk-row:hover) {
  background-color: #fde2e2 !important;
}
:deep(.pending-order-row) {
  background-color: #fef0f0 !important;
}
:deep(.pending-order-row:hover) {
  background-color: #fde2e2 !important;
}
.device-id {
  font-family: monospace;
  font-size: 12px;
}
.count-normal {
  color: #67c23a;
  font-weight: bold;
}
.count-warning {
  color: #e6a23c;
  font-weight: bold;
}
.count-danger {
  color: #f56c6c;
  font-weight: bold;
}
</style>
