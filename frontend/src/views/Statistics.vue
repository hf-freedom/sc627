<template>
  <div class="statistics">
    <h2>数据统计</h2>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">总发放优惠券</div>
          <div class="stat-value">{{ totalStats.totalIssued }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">已领取数量</div>
          <div class="stat-value">{{ totalStats.totalClaimed }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">已使用数量</div>
          <div class="stat-value">{{ totalStats.totalUsed }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">风险拦截次数</div>
          <div class="stat-value">{{ totalStats.riskBlockedCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-label">领取率</div>
          <div class="stat-value rate">
            {{ totalStats.totalIssued > 0 
              ? ((totalStats.totalClaimed / totalStats.totalIssued) * 100).toFixed(2) 
              : 0 }}%
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-label">核销率</div>
          <div class="stat-value rate">
            {{ totalStats.totalClaimed > 0 
              ? ((totalStats.totalUsed / totalStats.totalClaimed) * 100).toFixed(2) 
              : 0 }}%
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-label">优惠总成本</div>
          <div class="stat-value rate">¥{{ totalStats.totalCost }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <span>各优惠券详细统计</span>
      </template>
      <el-table :data="statistics" border>
        <el-table-column prop="templateName" label="优惠券名称" width="150" />
        <el-table-column prop="totalIssued" label="总发放" width="100" />
        <el-table-column prop="totalClaimed" label="已领取" width="100" />
        <el-table-column prop="totalUsed" label="已使用" width="100" />
        <el-table-column prop="totalExpired" label="已过期" width="100" />
        <el-table-column prop="totalReturned" label="已退回" width="100" />
        <el-table-column prop="riskBlockedCount" label="风险拦截" width="100" />
        <el-table-column prop="totalCost" label="总成本" width="100" />
        <el-table-column prop="claimRate" label="领取率" width="100">
          <template #default="{ row }">
            {{ row.claimRate ? row.claimRate.toFixed(2) : 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="usageRate" label="核销率" width="100">
          <template #default="{ row }">
            {{ row.usageRate ? row.usageRate.toFixed(2) : 0 }}%
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const statistics = ref([])

const totalStats = computed(() => {
  let totalIssued = 0
  let totalClaimed = 0
  let totalUsed = 0
  let riskBlockedCount = 0
  let totalCost = 0
  
  for (const stat of statistics.value) {
    totalIssued += stat.totalIssued || 0
    totalClaimed += stat.totalClaimed || 0
    totalUsed += stat.totalUsed || 0
    riskBlockedCount += stat.riskBlockedCount || 0
    totalCost += stat.totalCost || 0
  }
  
  return {
    totalIssued,
    totalClaimed,
    totalUsed,
    riskBlockedCount,
    totalCost
  }
})

const loadStatistics = async () => {
  try {
    const res = await axios.get('/api/statistics/coupons')
    statistics.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.mb-20 {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.stat-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}
.stat-value.rate {
  color: #67c23a;
}
</style>
