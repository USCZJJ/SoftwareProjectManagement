<template>
  <div>
    <div class="page-card">
      <div class="page-toolbar">
        <el-select v-model="projectId" placeholder="选择项目" @change="loadData" style="width:220px">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索需求" clearable style="width:180px" @keyup.enter="loadData" />
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width:120px">
          <el-option label="草稿" value="DRAFT" /><el-option label="已提交" value="SUBMITTED" />
          <el-option label="已评审" value="REVIEWED" /><el-option label="已批准" value="APPROVED" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="showDialog()">新建需求</el-button>
      </div>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="reqCode" label="编号" width="130" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{row}"><el-tag :type="row.priority==='MUST'?'danger':row.priority==='SHOULD'?'warning':''" size="small">{{ row.priority }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}"><el-tag size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <template v-if="row.status==='DRAFT'">
              <el-button size="small" type="warning" @click="handleSubmit(row)">提交评审</el-button>
            </template>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" style="margin-top:12px" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑需求' : '新建需求'" width="600px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="标题" prop="title" :rules="[{required:true,message:'请输入标题'}]">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type">
          <el-option v-for="t in ['FUNCTIONAL','NON_FUNCTIONAL','UI']" :key="t" :label="t" :value="t" /></el-select>
        </el-form-item>
        <el-form-item label="优先级"><el-select v-model="form.priority">
          <el-option v-for="p in ['MUST','SHOULD','COULD','WONT']" :key="p" :label="p" :value="p" /></el-select>
        </el-form-item>
        <el-form-item label="重要度"><el-rate v-model="form.severity" :max="5" /></el-form-item>
        <el-form-item label="需求来源"><el-input v-model="form.source" /></el-form-item>
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
import { reqApi } from '@/api/requirement'
import { projectApi } from '@/api/project'

const loading = ref(false)
const projects = ref<any[]>([])
const list = ref<any[]>([])
const projectId = ref<number | null>(null)
const keyword = ref('')
const statusFilter = ref('')
const page = ref(1), size = ref(10), total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const form = reactive({ title: '', description: '', type: 'FUNCTIONAL', priority: 'SHOULD', severity: 3, source: '' })

onMounted(async () => {
  const res: any = await projectApi.list({ page: 1, size: 100 })
  projects.value = res.data?.records || []
})

async function loadData() {
  if (!projectId.value) return
  loading.value = true
  try {
    const res: any = await reqApi.list({ projectId: projectId.value, keyword: keyword.value, status: statusFilter.value, page: page.value, size: size.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function showDialog(row?: any) {
  editingId.value = row?.id || null
  if (row) Object.assign(form, { title: row.title, description: row.description, type: row.type, priority: row.priority, severity: row.severity, source: row.source })
  else Object.assign(form, { title: '', description: '', type: 'FUNCTIONAL', priority: 'SHOULD', severity: 3, source: '' })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) await reqApi.update(editingId.value, form)
    else await reqApi.create({ ...form, projectId: projectId.value })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch {}
}

async function handleSubmit(row: any) {
  const { value } = await ElMessageBox.prompt('请输入评审人ID', '提交评审')
  if (!value) return
  await reqApi.submit(row.id, Number(value))
  ElMessage.success('已提交评审')
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await reqApi.delete(id)
  ElMessage.success('已删除')
  loadData()
}
</script>
