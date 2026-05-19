<template>
  <div class="page-card">
    <el-page-header @back="$router.push(`/project/${projectId}`)" content="甘特图" style="margin-bottom:16px" />
    <div ref="ganttContainer" style="width:100%;height:500px;"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { projectApi } from '@/api/project'

const route = useRoute()
const projectId = Number(route.params.id)
const ganttContainer = ref()

onMounted(async () => {
  const res: any = await projectApi.getWbs(projectId)
  const wbsData = res.data || []
  const tasks = wbsData
    .filter((w: any) => w.plannedStart && w.plannedEnd)
    .map((w: any, i: number) => ({
      name: w.nodeName,
      start: w.plannedStart,
      end: w.plannedEnd,
      progress: (w.progress || 0) / 100,
      itemStyle: w.isMilestone ? { color: '#E6A23C' } : undefined,
    }))

  if (ganttContainer.value && tasks.length) {
    const chart = echarts.init(ganttContainer.value)
    const categories = tasks.map((t: any) => t.name)
    chart.setOption({
      tooltip: { trigger: 'item', formatter: (p: any) => p.name + '<br/>' + p.value[0] + ' ~ ' + p.value[1] },
      grid: { left: '15%', right: '5%', top: 20, bottom: 20 },
      xAxis: { type: 'time', axisLabel: { formatter: '{yyyy}-{MM}-{dd}' } },
      yAxis: { type: 'category', data: categories, axisLabel: { width: 100, overflow: 'truncate' } },
      series: [{
        type: 'custom', renderItem: (params: any, api: any) => {
          const catIndex = api.value(0)
          const start = api.coord([api.value(1), catIndex])
          const end = api.coord([api.value(2), catIndex])
          const height = api.size ? api.size([0, 1])[1] * 0.6 : 20
          return {
            type: 'group',
            children: [
              { type: 'rect', shape: { x: start[0], y: start[1] - height / 2, width: Math.max(end[0] - start[0], 2), height }, style: { fill: '#409EFF', rx: 4 } },
            ],
          }
        },
        encode: { x: [1, 2], y: 0 },
        data: tasks.map((t: any, i: number) => [i, t.start, t.end, t.progress]),
      }],
    })
  }
})
</script>
