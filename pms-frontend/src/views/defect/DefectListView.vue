<template>
  <div>
    <div class="page-card">
      <div class="page-toolbar">
        <el-select v-model="projectId" placeholder="选择项目" @change="loadData" style="width:220px">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索缺陷" clearable style="width:180px" @keyup.enter="loadData" />
        <el-select v-model="severityFilter" placeholder="严重程度" clearable style="width:120px">
          <el-option v-for="s in ['BLOCKER','CRITICAL','MAJOR','MINOR','TRIVIAL']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="danger" @click="showDialog()">报告缺陷</el-button>
      </div>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="defectCode" label="编号" width="130" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{row}"><el-tag :type="severityTag(row.severity)" size="small">{{ row.severity }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}"><el-tag size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="assigneeId" label="修复人" width="80" />
        <el-table-column label="操作" width="240">
          <template #default="{row}">
            <el-button size="small" @click="showDialog(row)">详情</el-button>
            <template v-if="row.status==='OPEN'">
              <el-button size="small" type="primary" @click="handleAssign(row)">指派</el-button>
            </template>
            <el-select v-model="row.status" size="small" style="width:100px" @change="(v: string) => handleStatus(row, v)">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" style="margin-top:12px" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '缺陷详情' : '报告缺陷'" width="650px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="标题" prop="title" :rules="[{required:true}]"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="重现步骤"><el-input v-model="form.stepsToReproduce" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="期望结果"><el-input v-model="form.expectedResult" /></el-form-item>
        <el-form-item label="实际结果"><el-input v-model="form.actualResult" /></el-form-item>
        <el-form-item label="严重程度"><el-select v-model="form.severity">
          <el-option v-for="s in ['BLOCKER','CRITICAL','MAJOR','MINOR','TRIVIAL']" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
        <el-form-item label="优先级"><el-select v-model="form.priority">
          <el-option v-for="p in ['LOW','MEDIUM','HIGH','URGENT']" :key="p" :label="p" :value="p" /></el-select>
        </el-form-item>
        <el-form-item label="所属模块"><el-input v-model="form.module" /></el-form-item>
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
import { defectApi } from '@/api/defect'
import { projectApi } from '@/api/project'

const loading = ref(false)
const projects = ref<any[]>([])
const list = ref<any[]>([])
const projectId = ref<number | null>(null)
const keyword = ref('')
const severityFilter = ref('')
const page = ref(1), size = ref(10), total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const form = reactive({ title: '', description: '', stepsToReproduce: '', expectedResult: '', actualResult: '', severity: 'MINOR', priority: 'MEDIUM', module: '' })
const statusOptions = ['OPEN','ASSIGNED','FIXING','FIXED','VERIFYING','VERIFIED','CLOSED','REOPENED']

function severityTag(s: string) { const m: any = { BLOCKER: 'danger', CRITICAL: 'danger', MAJOR: 'warning', MINOR: 'info', TRIVIAL: '' }; return m[s] || '' }

onMounted(async () => {
  const res: any = await projectApi.list({ page: 1, size: 100 })
  projects.value = res.data?.records || []
})

async function loadData() {
  if (!projectId.value) return
  loading.value = true
  try {
    const res: any = await defectApi.list({ projectId: projectId.value, keyword: keyword.value, severity: severityFilter.value, page: page.value, size: size.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function showDialog(row?: any) {
  editingId.value = row?.id || null
  if (row) Object.assign(form, { title: row.title, description: row.description, stepsToReproduce: row.stepsToReproduce, expectedResult: row.expectedResult, actualResult: row.actualResult, severity: row.severity, priority: row.priority, module: row.module })
  else Object.assign(form, { title: '', description: '', stepsToReproduce: '', expectedResult: '', actualResult: '', severity: 'MINOR', priority: 'MEDIUM', module: '' })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) await defectApi.update(editingId.value, form)
    else await defectApi.create({ ...form, projectId: projectId.value })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch {}
}

async function handleAssign(row: any) {
  const { value } = await ElMessageBox.prompt('指派给用户ID', '指派缺陷')
  if (!value) return
  await defectApi.assign(row.id, Number(value))
  ElMessage.success('已指派')
  loadData()
}

async function handleStatus(row: any, status: string) {
  await defectApi.updateStatus(row.id, status)
  ElMessage.success('状态已更新')
  loadData()
}
</script>
