<template>
  <div class="layout-container">
    <div class="layout-aside">
      <div style="height:50px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:18px;font-weight:bold;border-bottom:1px solid rgba(255,255,255,.1);">
        PMS
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/project">
          <el-icon><FolderOpened /></el-icon><span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/requirement">
          <el-icon><Document /></el-icon><span>需求管理</span>
        </el-menu-item>
        <el-sub-menu index="task-group">
          <template #title>
            <el-icon><Grid /></el-icon><span>任务管理</span>
          </template>
          <el-menu-item index="/task" style="padding-left:60px!important">看板视图</el-menu-item>
          <el-menu-item index="/task/list" style="padding-left:60px!important">任务列表</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/defect">
          <el-icon><Warning /></el-icon><span>缺陷管理</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><DataAnalysis /></el-icon><span>统计分析</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="layout-main">
      <div class="layout-header">
        <div style="font-size:15px;font-weight:500;">{{ route.meta?.title || '' }}</div>
        <div style="display:flex;align-items:center;gap:16px;">
          <span style="color:#666;font-size:13px;">{{ userStore.user?.realName || userStore.user?.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/project')) return '/project'
  if (route.path.startsWith('/task/list')) return '/task/list'
  return route.path
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
