<template>
  <div>
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="keyword" placeholder="搜索项目名称" clearable style="width:220px" @keyup.enter="loadData" />
        <el-select v-model="statusFilter" placeholder="项目状态" clearable style="width:140px">
          <el-option label="初始化" value="INIT" /><el-option label="计划中" value="PLANNING" />
          <el-option label="执行中" value="EXECUTING" /><el-option label="监控中" value="MONITORING" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="showDialog()">新建项目</el-button>
      </div>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column prop="projectName" label="项目名称" min-width="200">
          <template #default="{row}"><el-link type="primary" @click="$router.push(`/project/${row.id}`)">{{ row.projectName }}</el-link></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}"><el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="budget" label="预算(万)" width="100" />
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" @click="$router.push(`/project/${row.id}`)">详情</el-button>
            <el-button size="small" type="warning" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" style="margin-top:12px" @change="loadData" />
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="项目目标" prop="objectives">
          <el-input v-model="form.objectives" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="预算(万元)" prop="budget">
          <el-input-number v-model="form.budget" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="form.status"><el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/project'

const loading = ref(false)
const list = ref<any[]>([])
const keyword = ref('')
const statusFilter = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const editingId = ref<number | null>(null)
const formRef = ref()
const form = reactive({
  projectName: '', description: '', objectives: '', startDate: '', endDate: '',
  budget: 0 as number | undefined, status: 'INIT',
})
const formRules = { projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }] }
const statusOptions = [
  { label: '初始化', value: 'INIT' }, { label: '计划中', value: 'PLANNING' },
  { label: '执行中', value: 'EXECUTING' }, { label: '监控中', value: 'MONITORING' },
  { label: '已关闭', value: 'CLOSED' },
]

function statusTag(s: string) { const m: any = { INIT: 'info', PLANNING: '', EXECUTING: 'success', MONITORING: 'warning', CLOSED: 'danger' }; return m[s] || '' }
function statusLabel(s: string) { const m: any = statusOptions.reduce((acc: any, o) => { acc[o.value] = o.label; return acc }, {}); return m[s] || s }

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res: any = await projectApi.list({ keyword: keyword.value, status: statusFilter.value, page: page.value, size: size.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function showDialog(row?: any) {
  dialogTitle.value = row ? '编辑项目' : '新建项目'
  editingId.value = row?.id || null
  if (row) Object.assign(form, { projectName: row.projectName, description: row.description, objectives: row.objectives, startDate: row.startDate, endDate: row.endDate, budget: row.budget, status: row.status })
  else Object.assign(form, { projectName: '', description: '', objectives: '', startDate: '', endDate: '', budget: undefined, status: 'INIT' })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) await projectApi.update(editingId.value, form)
    else await projectApi.create(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch {}
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该项目？', '提示', { type: 'warning' })
  await projectApi.delete(id)
  ElMessage.success('已删除')
  loadData()
}
</script>
