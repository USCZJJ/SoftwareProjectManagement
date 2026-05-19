<template>
  <div>
    <div class="page-card">
      <div class="page-toolbar">
        <el-select v-model="projectId" placeholder="选择项目" @change="loadData" style="width:220px">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索任务" clearable style="width:180px" @keyup.enter="loadData" />
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width:130px">
          <el-option v-for="s in ['TODO','IN_PROGRESS','IN_REVIEW','DONE','CLOSED']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="$router.push('/task')">看板视图</el-button>
      </div>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="title" label="任务标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{row}"><el-tag size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{row}"><el-progress :percentage="row.progress || 0" /></template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="110" />
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <el-button size="small" @click="showProgressDialog(row)">更新进度</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" style="margin-top:12px" @change="loadData" />
    </div>

    <el-dialog v-model="progressVisible" title="更新进度" width="400px">
      <el-form label-width="80px">
        <el-form-item label="进度"><el-slider v-model="progressVal" :min="0" :max="100" show-input /></el-form-item>
        <el-form-item label="剩余工时"><el-input-number v-model="remainHours" :min="0" :precision="1" /> h</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProgress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskApi } from '@/api/task'
import { projectApi } from '@/api/project'

const loading = ref(false)
const projects = ref<any[]>([])
const list = ref<any[]>([])
const projectId = ref<number | null>(null)
const keyword = ref('')
const statusFilter = ref('')
const page = ref(1), size = ref(10), total = ref(0)

const progressVisible = ref(false)
const progressVal = ref(0)
const remainHours = ref(0)
const currentTaskId = ref<number | null>(null)

onMounted(async () => {
  const res: any = await projectApi.list({ page: 1, size: 100 })
  projects.value = res.data?.records || []
})

async function loadData() {
  if (!projectId.value) return
  loading.value = true
  try {
    const res: any = await taskApi.list({ projectId: projectId.value, keyword: keyword.value, status: statusFilter.value, page: page.value, size: size.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function showProgressDialog(row: any) {
  currentTaskId.value = row.id
  progressVal.value = row.progress || 0
  remainHours.value = 0
  progressVisible.value = true
}

async function handleUpdateProgress() {
  if (!currentTaskId.value) return
  await taskApi.updateProgress(currentTaskId.value, progressVal.value, remainHours.value)
  ElMessage.success('进度已更新')
  progressVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await taskApi.delete(id)
  ElMessage.success('已删除')
  loadData()
}
</script>
