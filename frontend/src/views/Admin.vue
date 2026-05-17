<template>
  <div class="admin">
    <h2>优惠券管理</h2>
    
    <el-card class="mb-20">
      <template #header>
        <span>创建优惠券模板</span>
      </template>
      <el-form :model="templateForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="名称">
              <el-input v-model="templateForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类型">
              <el-select v-model="templateForm.type" style="width: 100%">
                <el-option label="固定金额" value="FIXED_AMOUNT" />
                <el-option label="百分比折扣" value="PERCENTAGE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="面值">
              <el-input-number v-model="templateForm.value" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最低金额">
              <el-input-number v-model="templateForm.minAmount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="templateForm.stock" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户等级要求">
              <el-input-number v-model="templateForm.userLevelRequired" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="每人限领">
              <el-input-number v-model="templateForm.userMaxClaim" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="适用分类">
              <el-select v-model="templateForm.productCategories" multiple style="width: 100%">
                <el-option label="电子产品" value="ELECTRONICS" />
                <el-option label="服装" value="CLOTHING" />
                <el-option label="食品" value="FOOD" />
                <el-option label="家居" value="HOME" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="有效期至">
              <el-date-picker v-model="templateForm.validEndTime" type="datetime" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="createTemplate">创建模板</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <span>优惠券模板列表</span>
      </template>
      <el-table :data="templates" border>
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            {{ row.type === 'FIXED_AMOUNT' ? '固定金额' : '百分比' }}
          </template>
        </el-table-column>
        <el-table-column prop="value" label="面值" width="100" />
        <el-table-column prop="minAmount" label="最低金额" width="100" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="claimedCount" label="已领" width="80" />
        <el-table-column prop="userLevelRequired" label="等级要求" width="100" />
        <el-table-column prop="active" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'danger'">
              {{ row.active ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="未使用券数量" width="130">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ getUnusedCount(row.templateId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后更新" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editTemplate(row)">编辑规则</el-button>
            <el-button size="small" type="warning" @click="revalidateCoupons(row.templateId)">重新校验</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑优惠券规则" width="600px">
      <el-alert
        title="调整规则说明"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      >
        保存修改后，系统将自动重新校验所有未使用的优惠券状态。
      </el-alert>
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="活动名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="用户等级要求">
          <el-input-number v-model="editForm.userLevelRequired" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="每人限领数量">
          <el-input-number v-model="editForm.userMaxClaim" :min="1" />
        </el-form-item>
        <el-form-item label="最低使用金额">
          <el-input-number v-model="editForm.minAmount" :min="0" />
        </el-form-item>
        <el-form-item label="适用商品分类">
          <el-select v-model="editForm.productCategories" multiple style="width: 100%">
            <el-option label="电子产品" value="ELECTRONICS" />
            <el-option label="服装" value="CLOTHING" />
            <el-option label="食品" value="FOOD" />
            <el-option label="家居" value="HOME" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期至">
          <el-date-picker v-model="editForm.validEndTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-switch v-model="editForm.active" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateTemplate">保存并重新校验</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialogVisible" title="重新校验结果" width="500px">
      <el-result :icon="revalidationResult.totalAffected > 0 ? 'warning' : 'success'" :title="`${revalidationResult.templateName} 校验完成`">
        <template #subTitle>
          共扫描 {{ revalidationResult.totalScanned || 0 }} 张未使用的优惠券
        </template>
        <template #extra>
          <div style="text-align: left; padding: 20px;">
            <el-statistic title="因过期自动失效" :value="revalidationResult.expiredCount" value-color="#f56c6c" style="display: inline-block; margin-right: 30px;" />
            <el-statistic title="用户等级不匹配" :value="revalidationResult.levelMismatchCount" value-color="#e6a23c" style="display: inline-block; margin-right: 30px;" />
            <el-statistic title="超出领取限制" :value="revalidationResult.maxClaimExceedCount" value-color="#67c23a" style="display: inline-block;" />
            <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee;">
              <strong>影响总计：</strong>
              <span style="color: #f56c6c; font-size: 18px; font-weight: bold; margin-left: 10px;">
                {{ revalidationResult.totalAffected }} 张优惠券
              </span>
            </div>
            <div style="margin-top: 15px; color: #909399; font-size: 12px;">
              校验时间：{{ formatTime(revalidationResult.revalidationTime) }}
            </div>
          </div>
        </template>
      </el-result>
      <template #footer>
        <el-button type="primary" @click="resultDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const templates = ref([])
const userCoupons = ref([])
const editDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const currentTemplateId = ref('')

const revalidationResult = reactive({
  templateId: '',
  templateName: '',
  expiredCount: 0,
  levelMismatchCount: 0,
  maxClaimExceedCount: 0,
  totalAffected: 0,
  totalScanned: 0,
  revalidationTime: ''
})

const templateForm = ref({
  name: '满减优惠券',
  type: 'FIXED_AMOUNT',
  value: 20,
  minAmount: 100,
  stock: 100,
  userLevelRequired: 2,
  userMaxClaim: 2,
  productCategories: ['ELECTRONICS', 'CLOTHING'],
  validEndTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
})

const editForm = ref({
  name: '',
  userLevelRequired: 1,
  userMaxClaim: 1,
  minAmount: 0,
  productCategories: [],
  validEndTime: null,
  active: true
})

const createTemplate = async () => {
  try {
    await axios.post('/api/coupon/template', templateForm.value)
    ElMessage.success('创建成功')
    loadTemplates()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const resetForm = () => {
  templateForm.value = {
    name: '满减优惠券',
    type: 'FIXED_AMOUNT',
    value: 20,
    minAmount: 100,
    stock: 100,
    userLevelRequired: 2,
    userMaxClaim: 2,
    productCategories: ['ELECTRONICS', 'CLOTHING'],
    validEndTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
  }
}

const loadTemplates = async () => {
  try {
    const res = await axios.get('/api/coupon/templates')
    templates.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const loadUserCoupons = async () => {
  try {
    const res = await axios.get('/api/coupon/user/all')
    userCoupons.value = res.data.data || []
  } catch (error) {
    userCoupons.value = []
  }
}

const getUnusedCount = (templateId) => {
  return userCoupons.value.filter(uc => 
    uc.templateId === templateId && uc.status === 'CLAIMED'
  ).length
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const editTemplate = (row) => {
  currentTemplateId.value = row.templateId
  editForm.value = {
    name: row.name,
    userLevelRequired: row.userLevelRequired,
    userMaxClaim: row.userMaxClaim,
    minAmount: row.minAmount,
    productCategories: [...row.productCategories],
    validEndTime: row.validEndTime ? new Date(row.validEndTime) : null,
    active: row.active
  }
  editDialogVisible.value = true
}

const updateTemplate = async () => {
  try {
    const res = await axios.put(`/api/coupon/template/${currentTemplateId.value}`, editForm.value)
    ElMessage.success('规则更新成功，已完成重新校验')
    
    if (res.data.data) {
      Object.assign(revalidationResult, res.data.data)
      revalidationResult.totalScanned = getUnusedCount(currentTemplateId.value) + res.data.data.totalAffected
      resultDialogVisible.value = true
    }
    
    editDialogVisible.value = false
    loadTemplates()
    loadUserCoupons()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const revalidateCoupons = async (templateId) => {
  try {
    await ElMessageBox.confirm(
      '确认要重新校验该优惠券模板下所有未使用的优惠券吗？这将根据当前规则更新优惠券状态。',
      '重新校验确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await axios.post(`/api/coupon/template/${templateId}/revalidate`)
    ElMessage.success('重新校验完成')
    
    if (res.data.data) {
      Object.assign(revalidationResult, res.data.data)
      revalidationResult.totalScanned = getUnusedCount(templateId) + res.data.data.totalAffected
      resultDialogVisible.value = true
    }
    
    loadTemplates()
    loadUserCoupons()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重新校验失败')
    }
  }
}

onMounted(() => {
  loadTemplates()
  loadUserCoupons()
})
</script>

<style scoped>
.mb-20 {
  margin-bottom: 20px;
}
</style>
