<template>
  <div>
    <div class="page-toolbar">
      <el-select v-model="projectId" placeholder="选择项目" @change="loadEVM" style="width:260px">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
    </div>

    <el-row :gutter="16">
      <el-col :span="12">
        <div class="page-card">
          <div style="font-weight:bold;margin-bottom:12px;">EVM 挣值分析</div>
          <el-descriptions :column="2" border size="small" v-if="evmData">
            <el-descriptions-item label="PV(计划值)">{{ evmData.pv }}h</el-descriptions-item>
            <el-descriptions-item label="EV(挣值)">{{ evmData.ev }}h</el-descriptions-item>
            <el-descriptions-item label="AC(实际成本)">{{ evmData.ac }}h</el-descriptions-item>
            <el-descriptions-item label="SPI(进度绩效)">{{ evmData.spi }}</el-descriptions-item>
            <el-descriptions-item label="CPI(成本绩效)">{{ evmData.cpi }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="evmData.status==='ON_TRACK'?'success':evmData.status==='BEHIND_SCHEDULE'?'warning':'danger'">
                {{ evmData.status === 'ON_TRACK' ? '正常' : evmData.status === 'BEHIND_SCHEDULE' ? '进度滞后' : '成本超支' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请选择项目" />
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <div style="font-weight:bold;margin-bottom:12px;">缺陷分析</div>
          <div ref="defectChart" style="height:240px;" v-if="projectId"></div>
          <el-empty v-else description="请选择项目" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="24">
        <div class="page-card">
          <div style="font-weight:bold;margin-bottom:12px;">工时统计 (近30天)</div>
          <div ref="worklogChart" style="height:300px;" v-if="projectId"></div>
          <el-empty v-else description="请选择项目" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { projectApi } from '@/api/project'

const projects = ref<any[]>([])
const projectId = ref<number | null>(null)
const evmData = ref<any>(null)
const defectChart = ref()
const worklogChart = ref()

onMounted(async () => {
  const res: any = await projectApi.list({ page: 1, size: 100 })
  projects.value = res.data?.records || []
})

async function loadEVM() {
  if (!projectId.value) return
  const [evmRes, defectRes, worklogRes]: any[] = await Promise.all([
    request({ url: `/dashboard/evm/${projectId.value}` }),
    request({ url: `/dashboard/defect-stats/${projectId.value}` }),
    request({ url: `/dashboard/worklog-stats?projectId=${projectId.value}&startDate=${new Date(Date.now()-30*86400000).toISOString().slice(0,10)}&endDate=${new Date().toISOString().slice(0,10)}` }),
  ])
  evmData.value = evmRes.data

  if (defectChart.value && defectRes.data) {
    const chart = echarts.init(defectChart.value)
    const severities = defectRes.data.bySeverity || {}
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%','70%'],
        data: Object.entries(severities).map(([k, v]) => ({ name: k, value: v })),
      }],
    })
  }

  if (worklogChart.value && worklogRes.data) {
    const chart = echarts.init(worklogChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: worklogRes.data.map((d: any) => d.date), axisLabel: { rotate: 45, fontSize: 10 } },
      yAxis: { type: 'value', name: '工时(h)' },
      series: [{ name: '工时', type: 'bar', data: worklogRes.data.map((d: any) => d.hours), itemStyle: { color: '#409EFF' } }],
    })
  }
}
</script>
