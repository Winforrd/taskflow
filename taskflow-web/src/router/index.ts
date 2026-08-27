import {
  createRouter,
  createWebHistory
} from 'vue-router'

import type {
  RouteRecordRaw
} from 'vue-router'

import {
  getToken
} from '@/utils/auth'


const routes: RouteRecordRaw[] = [

  /**
   * 登录
   */
  {
    path: '/login',

    name: 'Login',

    component: () =>
      import(
        '@/views/auth/LoginView.vue'
      ),

    meta: {
      requiresAuth: false
    }
  },


  /**
   * 注册
   */
  {
    path: '/register',

    name: 'Register',

    component: () =>
      import(
        '@/views/auth/RegisterView.vue'
      ),

    meta: {
      requiresAuth: false
    }
  },


  /**
   * 主布局
   */
  {
    path: '/',

    component: () =>
      import(
        '@/layouts/BasicLayout.vue'
      ),

    meta: {
      requiresAuth: true
    },

    children: [

      /**
       * 默认跳转工作台
       */
      {
        path: '',

        redirect: '/dashboard'
      },


      /**
       * 工作台
       */
      {
        path: 'dashboard',

        name: 'Dashboard',

        component: () =>
          import(
            '@/views/dashboard/DashboardView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '工作台'
        }
      },


      /**
       * 我的团队
       */
      {
        path: 'teams',

        name: 'Teams',

        component: () =>
          import(
            '@/views/team/TeamListView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '我的团队'
        }
      },


      /**
       * 团队详情
       *
       * 例如：
       * /teams/1
       */
      {
        path: 'teams/:teamId',

        name: 'TeamDetail',

        component: () =>
          import(
            '@/views/team/TeamDetailView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '团队详情'
        }
      },


      /**
       * 团队成员
       *
       * 例如：
       * /teams/1/members
       */
      {
        path: 'teams/:teamId/members',

        name: 'TeamMembers',

        component: () =>
          import(
            '@/views/team/TeamMembersView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '团队成员'
        }
      },


      /**
       * 个人中心
       */
      {
        path: 'profile',

        name: 'Profile',

        component: () =>
          import(
            '@/views/user/ProfileView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '个人中心'
        }
      },

      /**
       * 团队项目列表
       */
      {
        path: 'teams/:teamId/projects',

        name: 'ProjectList',

        component: () =>
          import(
            '@/views/project/ProjectListView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '项目列表'
        }
      },


      /**
       * 项目详情
       */
      {
        path: 'projects/:projectId',

        name: 'ProjectDetail',

        component: () =>
          import(
            '@/views/project/ProjectDetailView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '项目详情'
        }
      },


      /**
       * 项目成员
       */
      {
        path: 'projects/:projectId/members',

        name: 'ProjectMembers',

        component: () =>
          import(
            '@/views/project/ProjectMembersView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '项目成员'
        }
      },

      /**
       * 项目任务看板
       */
      {
        path: 'projects/:projectId/tasks',

        name: 'TaskBoard',

        component: () =>
          import(
            '@/views/task/TaskBoardView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '任务看板'
        }
      },


      /**
       * 任务详情
       */
      {
        path: 'tasks/:taskId',

        name: 'TaskDetail',

        component: () =>
          import(
            '@/views/task/TaskDetailView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '任务详情'
        }
      },

      /**
       * 我的任务
       */
      {
        path: 'my-tasks',

        name: 'MyTasks',

        component: () =>
          import(
            '@/views/task/MyTaskView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '我的任务'
        }
      },

      {
        path: 'notifications',

        name: 'Notifications',

        component: () =>
          import(
            '@/views/notification/NotificationView.vue'
          ),

        meta: {
          requiresAuth: true,
          title: '通知中心'
        }
      },

      {
        path:'projects/:projectId/statistics',

        name:'ProjectStatistics',

        component:
          () =>
            import(
              '@/views/project/ProjectStatisticsView.vue'
            ),

        meta: {
          requiresAuth:
            true,

          title:
            '项目统计'
        }
      },

    ]
  },


  /**
   * 未匹配地址
   */
  {
    path: '/:pathMatch(.*)*',

    redirect: '/dashboard'
  }

]


const router =
  createRouter({

    history:
      createWebHistory(),

    routes

  })


/**
 * 全局前置路由守卫
 */
router.beforeEach(
  (
    to,
    from,
    next
  ) => {

    const token =
      getToken()


    /**
     * 需要登录
     * 但没有 Token
     */
    if (
      to.meta.requiresAuth
      &&
      !token
    ) {

      next({

        path: '/login',

        query: {
          redirect:
            to.fullPath
        }

      })

      return
    }


    /**
     * 已登录
     * 又访问登录页或注册页
     */
    if (
      token
      &&
      (
        to.path === '/login'
        ||
        to.path === '/register'
      )
    ) {

      next(
        '/dashboard'
      )

      return
    }


    next()

  }
)


export default router