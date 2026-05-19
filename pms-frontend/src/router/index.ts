import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' },
      },
      {
        path: 'project',
        name: 'ProjectList',
        component: () => import('@/views/project/ProjectListView.vue'),
        meta: { title: '项目管理', icon: 'FolderOpened' },
      },
      {
        path: 'project/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetailView.vue'),
        meta: { title: '项目详情', hidden: true },
      },
      {
        path: 'project/:id/gantt',
        name: 'ProjectGantt',
        component: () => import('@/views/project/GanttView.vue'),
        meta: { title: '甘特图', hidden: true },
      },
      {
        path: 'requirement',
        name: 'RequirementList',
        component: () => import('@/views/requirement/RequirementListView.vue'),
        meta: { title: '需求管理', icon: 'Document' },
      },
      {
        path: 'task',
        name: 'TaskKanban',
        component: () => import('@/views/task/KanbanView.vue'),
        meta: { title: '任务看板', icon: 'Grid' },
      },
      {
        path: 'task/list',
        name: 'TaskList',
        component: () => import('@/views/task/TaskListView.vue'),
        meta: { title: '任务列表', icon: 'List' },
      },
      {
        path: 'defect',
        name: 'DefectList',
        component: () => import('@/views/defect/DefectListView.vue'),
        meta: { title: '缺陷管理', icon: 'Warning' },
      },
      {
        path: 'stats',
        name: 'Statistics',
        component: () => import('@/views/stats/StatisticsView.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
