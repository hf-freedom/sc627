<template>
  <div class="home">
    <h2>欢迎使用优惠券投放风控系统</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">快速开始</div>
          </template>
          <div class="card-content">
            <p>1. 先创建用户</p>
            <p>2. 在管理员页面创建优惠券模板</p>
            <p>3. 在用户中心领取优惠券</p>
            <p>4. 在订单管理页面使用优惠券下单</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">功能特性</div>
          </template>
          <div class="card-content">
            <p>✓ 账号等级校验</p>
            <p>✓ 领取次数限制</p>
            <p>✓ 设备风控检测</p>
            <p>✓ 商品范围校验</p>
            <p>✓ 订单取消退回</p>
            <p>✓ 异常用户审核</p>
            <p>✓ 自动过期处理</p>
            <p>✓ 库存控制</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">快速操作</div>
          </template>
          <div class="card-content">
            <el-button type="primary" @click="createTestUser">创建测试用户</el-button>
            <p v-if="testUserId">用户ID: {{ testUserId }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const testUserId = ref('')

const createTestUser = async () => {
  try {
    const res = await axios.post('/api/user', {
      username: '测试用户',
      level: 3
    })
    testUserId.value = res.data.data.userId
    localStorage.setItem('currentUserId', res.data.data.userId)
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.home h2 {
  margin-bottom: 30px;
  color: #333;
}
.card-header {
  font-weight: bold;
  font-size: 16px;
}
.card-content p {
  margin: 10px 0;
  color: #666;
}
</style>
