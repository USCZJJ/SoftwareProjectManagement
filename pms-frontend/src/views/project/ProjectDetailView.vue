<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/project')" :content="project?.projectName || '项目详情'" style="margin-bottom:16px" />
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="info">
        <div class="page-card" v-if="project">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="项目编号">{{ project.projectCode }}</el-descriptions-item>
            <el-descriptions-item label="项目名称">{{ project.projectName }}</el-descriptions-item>
            <el-descriptions-item label="项目状态"><el-tag>{{ statusLabel(project.status) }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="预算">{{ project.budget }}万元</el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ project.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ project.endDate }}</el-descriptions-item>
            <el-descriptions-item label="项目目标" :span="2">{{ project.objectives }}</el-descriptions-item>
            <el-descriptions-item label="项目描述" :span="2">{{ project.description }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:16px">
            <el-button type="primary" @click="$router.push(`/project/${project.id}/gantt`)">查看甘特图</el-button>
            <el-button @click="$router.push(`/task?projectId=${project.id}`)">任务看板</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="WBS分解" name="wbs">
        <div class="page-card">
          <div class="page-toolbar">
            <el-button type="primary" size="small" @click="showWbsDialog()">添加WBS节点</el-button>
            <el-button size="small" @click="loadCriticalPath">关键路径分析</el-button>
          </div>
          <el-table :data="wbsList" row-key="id" default-expand-all stripe size="small">
            <el-table-column prop="wbsCode" label="WBS编码" width="120" />
            <el-table-column prop="nodeName" label="节点名称" min-width="180" />
            <el-table-column prop="progress" label="进度" width="100">
              <template #default="{row}"><el-progress :percentage="row.progress || 0" /></template>
            </el-table-column>
            <el-table-column prop="plannedStart" label="计划开始" width="110" />
            <el-table-column prop="plannedEnd" label="计划结束" width="110" />
            <el-table-column prop="assigneeId" label="负责人ID" width="90" />
            <el-table-column label="操作" width="160">
              <template #default="{row}">
                <el-button size="small" @click="showWbsDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteWbs(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="里程碑" name="milestone">
        <div class="page-card">
          <div class="page-toolbar">
            <el-button type="primary" size="small" @click="showMsDialog()">添加里程碑</el-button>
          </div>
          <el-table :data="milestones" stripe size="small">
            <el-table-column prop="milestoneName" label="里程碑名称" min-width="180" />
            <el-table-column prop="plannedDate" label="计划日期" width="110" />
            <el-table-column prop="actualDate" label="实际日期" width="110" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{row}"><el-tag :type="row.status==='ACHIEVED'?'success':row.status==='DELAYED'?'danger':''">{{ row.status }}</el-tag></template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="计划变更" name="changes">
        <div class="page-card">
          <div class="page-toolbar">
            <el-button type="primary" size="small" @click="showChangeDialog()">提交变更申请</el-button>
          </div>
          <el-table :data="changes" stripe size="small">
            <el-table-column prop="changeType" label="变更类型" width="100" />
            <el-table-column prop="changeDesc" label="变更描述" min-width="180" />
            <el-table-column prop="status" label="审批状态" width="100">
              <template #default="{row}"><el-tag :type="row.status==='APPROVED'?'success':row.status==='REJECTED'?'danger':'warning'">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{row}">
                <template v-if="row.status === 'PENDING'">
                  <el-button size="small" type="success" @click="approveChange(row.id, true)">通过</el-button>
                  <el-button size="small" type="danger" @click="approveChange(row.id, false)">拒绝</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="项目成员" name="members">
        <div class="page-card">
          <div class="page-toolbar">
            <el-input-number v-model="newMember.userId" placeholder="用户ID" :min="1" size="small" />
            <el-select v-model="newMember.role" placeholder="角色" size="small" style="width:120px">
              <el-option label="PM" value="PM" /><el-option label="DEV" value="DEV" />
              <el-option label="QA" value="QA" /><el-option label="BA" value="BA" />
            </el-select>
            <el-button type="primary" size="small" @click="handleAddMember">添加成员</el-button>
          </div>
          <el-table :data="members" stripe size="small">
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="role" label="项目角色" width="100" />
            <el-table-column prop="joinTime" label="加入时间" width="160" />
            <el-table-column label="操作">
              <template #default="{row}"><el-button size="small" type="danger" @click="handleRemoveMember(row.userId)">移除</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/project'

const route = useRoute()
const projectId = Number(route.params.id)
const loading = ref(false)
const activeTab = ref('info')
const project = ref<any>(null)
const wbsList = ref<any[]>([])
const milestones = ref<any[]>([])
const changes = ref<any[]>([])
const members = ref<any[]>([])
const newMember = reactive({ userId: undefined, role: 'DEV' })

function statusLabel(s: string) { const m: any = { INIT: '初始化', PLANNING: '计划中', EXECUTING: '执行中', MONITORING: '监控中', CLOSED: '已关闭' }; return m[s] || s }

onMounted(async () => {
  loading.value = true
  try {
    const [projRes, wbsRes, msRes, chRes, memRes]: any[] = await Promise.all([
      projectApi.detail(projectId),
      projectApi.getWbs(projectId),
      projectApi.getMilestones(projectId),
      projectApi.getChanges(projectId),
      projectApi.getMembers(projectId),
    ])
    project.value = projRes.data
    wbsList.value = wbsRes.data || []
    milestones.value = msRes.data || []
    changes.value = chRes.data || []
    members.value = memRes.data || []
  } finally { loading.value = false }
})

function showWbsDialog(row?: any) {
  ElMessageBox.prompt('节点名称', row ? '编辑WBS' : '添加WBS', { inputValue: row?.nodeName || '' }).then(({ value }) => {
    if (row) projectApi.saveWbs(projectId, { ...row, nodeName: value })
    else projectApi.saveWbs(projectId, { nodeName: value, parentId: row?.parentId || 0 })
    ElMessage.success('保存成功')
    projectApi.getWbs(projectId).then((res: any) => wbsList.value = res.data || [])
  }).catch(() => {})
}

async function handleDeleteWbs(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await projectApi.deleteWbs(projectId, id)
  ElMessage.success('已删除')
  const res: any = await projectApi.getWbs(projectId)
  wbsList.value = res.data || []
}

async function loadCriticalPath() {
  try {
    const res: any = await projectApi.criticalPath(projectId)
    const ids = res.data || []
    ElMessage.success('关键路径节点ID: ' + ids.join(', '))
  } catch {}
}

function showMsDialog() {
  ElMessageBox.prompt('里程碑名称', '添加里程碑').then(async ({ value }) => {
    await projectApi.saveMilestone(projectId, { milestoneName: value, plannedDate: new Date().toISOString().slice(0, 10) })
    ElMessage.success('添加成功')
    const res: any = await projectApi.getMilestones(projectId)
    milestones.value = res.data || []
  }).catch(() => {})
}

function showChangeDialog() {
  ElMessageBox.prompt('变更描述', '提交变更申请', { inputType: 'textarea' }).then(async ({ value }) => {
    await projectApi.submitChange(projectId, { changeType: 'WBS', changeDesc: value, reason: value })
    ElMessage.success('已提交')
    const res: any = await projectApi.getChanges(projectId)
    changes.value = res.data || []
  }).catch(() => {})
}

async function approveChange(id: number, approved: boolean) {
  await projectApi.approveChange(projectId, id, approved, approved ? '同意' : '拒绝')
  ElMessage.success('已处理')
  const res: any = await projectApi.getChanges(projectId)
  changes.value = res.data || []
}

async function handleAddMember() {
  if (!newMember.userId) return ElMessage.warning('请输入用户ID')
  await projectApi.addMember(projectId, newMember.userId, newMember.role)
  ElMessage.success('添加成功')
  const res: any = await projectApi.getMembers(projectId)
  members.value = res.data || []
}

async function handleRemoveMember(userId: number) {
  await projectApi.removeMember(projectId, userId)
  ElMessage.success('已移除')
  const res: any = await projectApi.getMembers(projectId)
  members.value = res.data || []
}
</script>
