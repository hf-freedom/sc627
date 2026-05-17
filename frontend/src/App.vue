<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>优惠券投放风控系统</h1>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu router>
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/admin">优惠券管理</el-menu-item>
            <el-menu-item index="/user">用户中心</el-menu-item>
            <el-menu-item index="/order">订单管理</el-menu-item>
            <el-menu-item index="/audit">审核管理</el-menu-item>
            <el-menu-item index="/statistics">数据统计</el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style>
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}
.el-header {
  background-color: #409eff;
  color: white;
  line-height: 60px;
  padding: 0 20px;
}
.el-header h1 {
  margin: 0;
  font-size: 24px;
}
.el-aside {
  background-color: #f5f7fa;
}
.el-main {
  background-color: #fff;
  padding: 20px;
}
</style>
