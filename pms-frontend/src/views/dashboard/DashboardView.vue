<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card">
          <div :style="{width:'48px',height:'48px',borderRadius:'50%',background:item.bg,display:'flex',alignItems:'center',justifyContent:'center'}">
            <el-icon :size="24" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <div style="font-size:24px;font-weight:bold;">{{ item.value }}</div>
            <div style="font-size:13px;color:#999;">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="14">
        <div class="page-card">
          <div style="font-weight:bold;margin-bottom:12px;">项目进度概览</div>
          <div ref="progressChart" style="height:300px;"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="page-card">
          <div style="font-weight:bold;margin-bottom:12px;">我的待办任务</div>
          <div v-for="t in myTasks" :key="t.id" style="padding:8px 0;border-bottom:1px solid #f0f0f0;display:flex;justify-content:space-between;">
            <div>
              <el-tag :type="priorityType(t.priority)" size="small">{{ t.priority }}</el-tag>
              <span style="margin-left:8px;">{{ t.title }}</span>
            </div>
            <span style="color:#999;font-size:12px;">{{ t.projectName }}</span>
          </div>
          <el-empty v-if="!myTasks.length" description="暂无待办任务" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const statCards = ref([
  { label: '进行中项目', value: 0, icon: 'FolderOpened', bg: '#e8f4ff', color: '#409EFF' },
  { label: '待处理任务', value: 0, icon: 'List', bg: '#fff7e6', color: '#E6A23C' },
  { label: '待修复缺陷', value: 0, icon: 'Warning', bg: '#fef0f0', color: '#F56C6C' },
  { label: '本周工时(h)', value: 0, icon: 'Timer', bg: '#e8f8e8', color: '#67C23A' },
])
const myTasks = ref<any[]>([])
const progressChart = ref()

function priorityType(p: string) { return p === 'URGENT' ? 'danger' : p === 'HIGH' ? 'warning' : p === 'LOW' ? 'info' : '' }

onMounted(async () => {
  try {
    const res: any = await request({ url: '/dashboard/stats', method: 'get' })
    if (res.data) {
      statCards.value[0].value = res.data.activeProjects || 0
      statCards.value[1].value = res.data.pendingTasks || 0
      statCards.value[2].value = res.data.openDefects || 0
      statCards.value[3].value = res.data.weekHours || 0
    }
  } catch {}
  try {
    const res: any = await request({ url: '/task/my?page=1&size=5', method: 'get' })
    myTasks.value = (res.data?.records) || []
  } catch {}
  if (progressChart.value) {
    const chart = echarts.init(progressChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ name: '项目进度', type: 'line', data: [], smooth: true }],
    })
  }
})
</script>
