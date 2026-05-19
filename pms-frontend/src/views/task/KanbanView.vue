<template>
  <div>
    <div class="page-toolbar">
      <el-select v-model="currentProjectId" placeholder="选择项目" @change="loadKanban" style="width:240px">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
      <el-button type="primary" @click="showTaskDialog()">新建任务</el-button>
    </div>
    <div class="kanban-board" v-if="columns.length">
      <div class="kanban-column" v-for="col in columns" :key="col.id">
        <div class="kanban-column-header">
          <span>
            <el-tag :color="col.color" effect="dark" size="small" style="color:#fff">{{ col.columnName }}</el-tag>
            <span style="margin-left:6px;font-size:12px;color:#999;">{{ col.tasks?.length || 0 }}{{ col.wipLimit > 0 ? '/' + col.wipLimit : '' }}</span>
          </span>
        </div>
        <draggable
          :list="col.tasks || []"
          group="tasks"
          item-key="id"
          ghost-class="ghost"
          @change="(evt: any) => handleMove(col, evt)"
        >
          <template #item="{ element }">
            <div class="kanban-card" @click="openTaskDetail(element)">
              <div style="font-weight:500;font-size:13px;margin-bottom:4px;">{{ element.title }}</div>
              <div style="display:flex;align-items:center;gap:6px;font-size:12px;">
                <el-tag :type="priorityTag(element.priority)" size="small">{{ element.priority }}</el-tag>
                <span v-if="element.assigneeName" style="color:#999;">{{ element.assigneeName }}</span>
              </div>
              <div v-if="element.dueDate" style="font-size:11px;color:#999;margin-top:4px;">截止: {{ element.dueDate }}</div>
            </div>
          </template>
        </draggable>
      </div>
    </div>
    <el-empty v-else description="请先选择一个项目" />

    <!-- Task Detail / Create Dialog -->
    <el-dialog v-model="taskDialogVisible" :title="isNewTask ? '新建任务' : '任务详情'" width="650px">
      <el-descriptions v-if="!isNewTask && currentTask" :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="状态">{{ currentTask.status }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentTask.priority }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ currentTask.assigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="计划工时">{{ currentTask.plannedHours || '-' }}h</el-descriptions-item>
        <el-descriptions-item label="实际工时">{{ currentTask.actualHours || 0 }}h</el-descriptions-item>
        <el-descriptions-item label="进度">{{ currentTask.progress || 0 }}%</el-descriptions-item>
      </el-descriptions>
      <el-form ref="taskFormRef" :model="taskForm" label-width="80px">
        <el-form-item label="任务标题" prop="title" :rules="[{required:true,message:'请输入标题'}]">
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="taskForm.type"><el-option v-for="t in ['TASK','BUG','FEATURE','IMPROVEMENT']" :key="t" :label="t" :value="t" /></el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority"><el-option v-for="p in ['LOW','MEDIUM','HIGH','URGENT']" :key="p" :label="p" :value="p" /></el-select>
        </el-form-item>
        <el-form-item label="执行人">
          <el-input-number v-model="taskForm.assigneeId" :min="1" placeholder="用户ID" />
        </el-form-item>
        <el-form-item label="计划工时">
          <el-input-number v-model="taskForm.plannedHours" :min="0" :precision="1" /> h
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="taskForm.dueDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <template v-if="!isNewTask">
          <el-divider>进度更新</el-divider>
          <el-form-item label="完成进度">
            <el-slider v-model="progressForm.progress" :min="0" :max="100" show-input style="width:300px" />
          </el-form-item>
          <el-form-item label="剩余工时">
            <el-input-number v-model="progressForm.hours" :min="0" :precision="1" /> h
          </el-form-item>
          <el-button type="success" size="small" @click="handleUpdateProgress">更新进度</el-button>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSaveTask" v-if="isNewTask">创建</el-button>
        <el-button type="danger" @click="handleDeleteTask" v-if="!isNewTask">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import draggable from 'vuedraggable'
import { taskApi } from '@/api/task'
import { projectApi } from '@/api/project'

const projects = ref<any[]>([])
const currentProjectId = ref<number | null>(null)
const columns = ref<any[]>([])
const taskDialogVisible = ref(false)
const isNewTask = ref(true)
const currentTask = ref<any>(null)
const taskFormRef = ref()
const taskForm = reactive<any>({ title: '', description: '', type: 'TASK', priority: 'MEDIUM', assigneeId: null, plannedHours: 0, dueDate: null })
const progressForm = reactive({ progress: 0, hours: 0 })

function priorityTag(p: string) { const m: any = { URGENT: 'danger', HIGH: 'warning', LOW: 'info' }; return m[p] || '' }

onMounted(async () => {
  try {
    const res: any = await projectApi.list({ page: 1, size: 100 })
    projects.value = res.data?.records || []
  } catch {}
})

async function loadKanban() {
  if (!currentProjectId.value) return
  const res: any = await taskApi.kanban(currentProjectId.value)
  columns.value = res.data?.columns || []
}

function showTaskDialog(task?: any) {
  isNewTask.value = !task
  currentTask.value = task || null
  if (task) {
    Object.assign(taskForm, { title: task.title, description: task.description, type: task.type, priority: task.priority, assigneeId: task.assigneeId, plannedHours: task.plannedHours, dueDate: task.dueDate })
    progressForm.progress = task.progress || 0
    progressForm.hours = 0
  } else {
    Object.assign(taskForm, { title: '', description: '', type: 'TASK', priority: 'MEDIUM', assigneeId: null, plannedHours: 0, dueDate: null })
  }
  taskDialogVisible.value = true
}

function openTaskDetail(task: any) {
  showTaskDialog(task)
}

async function handleSaveTask() {
  const valid = await taskFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await taskApi.create({ ...taskForm, projectId: currentProjectId.value })
    ElMessage.success('创建成功')
    taskDialogVisible.value = false
    loadKanban()
  } catch {}
}

async function handleDeleteTask() {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await taskApi.delete(currentTask.value.id)
  ElMessage.success('已删除')
  taskDialogVisible.value = false
  loadKanban()
}

async function handleMove(col: any, evt: any) {
  if (evt.added) {
    const task = evt.added.element
    const newOrder = evt.added.newIndex
    await taskApi.move(task.id, col.id, newOrder)
  }
}

async function handleUpdateProgress() {
  if (!currentTask.value) return
  await taskApi.updateProgress(currentTask.value.id, progressForm.progress, progressForm.hours)
  ElMessage.success('进度已更新')
  loadKanban()
}
</script>
