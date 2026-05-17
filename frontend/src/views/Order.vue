<template>
  <div class="order">
    <h2>订单管理</h2>

    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>待审核订单</span>
          <el-tag type="warning" v-if="pendingOrders.length > 0">
            待审核: {{ pendingOrders.length }} 条
          </el-tag>
        </div>
      </template>
      <el-table :data="pendingOrders" border :row-class-name="getPendingRowClassName">
        <el-table-column prop="orderId" label="订单ID" width="250" />
        <el-table-column prop="userId" label="用户ID" width="150" />
        <el-table-column prop="totalAmount" label="订单金额" width="100" />
        <el-table-column prop="discountAmount" label="优惠金额" width="100" />
        <el-table-column prop="payAmount" label="实付金额" width="100" />
        <el-table-column prop="auditRemark" label="审核备注" min-width="150">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.auditRemark }}</el-tag>
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

    <el-card class="mb-20">
      <template #header>
        <span>创建订单</span>
      </template>
      <el-form :model="orderForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="用户ID">
              <el-input v-model="orderForm.userId" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优惠券ID">
              <el-select v-model="orderForm.userCouponId" clearable placeholder="选择优惠券" style="width: 100%">
                <el-option 
                  v-for="coupon in availableCoupons" 
                  :key="coupon.userCouponId" 
                  :label="coupon.userCouponId" 
                  :value="coupon.userCouponId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="订单金额">
              <el-input-number v-model="orderForm.totalAmount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品分类">
          <el-select v-model="orderForm.productCategories" multiple style="width: 50%">
            <el-option label="电子产品" value="ELECTRONICS" />
            <el-option label="服装" value="CLOTHING" />
            <el-option label="食品" value="FOOD" />
            <el-option label="家居" value="HOME" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="createOrder">创建订单</el-button>
          <el-button @click="loadAvailableCoupons">加载可用优惠券</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <span>订单列表</span>
      </template>
      <el-table :data="orders" border>
        <el-table-column prop="orderId" label="订单ID" width="250" />
        <el-table-column prop="userId" label="用户ID" width="150" />
        <el-table-column prop="totalAmount" label="订单金额" width="100" />
        <el-table-column prop="discountAmount" label="优惠金额" width="100" />
        <el-table-column prop="payAmount" label="实付金额" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CREATED' ? 'success' : 'danger'">
              {{ row.status === 'CREATED' ? '有效' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 'PENDING'" type="warning" effect="dark">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.auditStatus === 'REJECTED'" type="danger">已拒绝</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="danger" 
              @click="cancelOrder(row.orderId)"
              :disabled="row.status === 'CANCELLED'">
              取消订单
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const pendingOrders = ref([])
const availableCoupons = ref([])

const orderForm = ref({
  userId: '',
  userCouponId: '',
  totalAmount: 200,
  productCategories: ['ELECTRONICS']
})

const getPendingRowClassName = ({ row }) => {
  return 'pending-row'
}

const loadPendingOrders = async () => {
  try {
    const res = await axios.get('/api/order/audit/pending')
    pendingOrders.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const approveOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认审核通过该订单？', '提示', {
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
    loadOrders()
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
    ElMessage.success('审核拒绝，优惠券已退回')
    loadPendingOrders()
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const createOrder = async () => {
  try {
    const res = await axios.post('/api/order', orderForm.value)
    if (res.data.data.needAudit) {
      ElMessage.warning('订单创建成功，但用户为异常用户，需人工审核优惠券使用')
    } else {
      ElMessage.success('订单创建成功')
    }
    loadOrders()
    loadPendingOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建失败')
  }
}

const loadOrders = async () => {
  try {
    const res = await axios.get('/api/order')
    orders.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const cancelOrder = async (orderId) => {
  try {
    await axios.post(`/api/order/cancel/${orderId}`)
    ElMessage.success('订单已取消，优惠券已退回')
    loadOrders()
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

const loadAvailableCoupons = async () => {
  if (!orderForm.value.userId) {
    ElMessage.warning('请先输入用户ID')
    return
  }
  try {
    const res = await axios.get(`/api/coupon/user/${orderForm.value.userId}`)
    availableCoupons.value = res.data.data.filter(c => c.status === 'CLAIMED')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  const savedUserId = localStorage.getItem('currentUserId')
  if (savedUserId) {
    orderForm.value.userId = savedUserId
  }
  loadOrders()
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
:deep(.pending-row) {
  background-color: #fdf6ec !important;
}
:deep(.pending-row:hover) {
  background-color: #faecd8 !important;
}
</style>
