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
      }

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
     * 但本地没有Token
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
     * 已登录状态下
     * 不允许再次进入登录页或注册页
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

      next('/dashboard')

      return
    }

    next()

  }
)


export default router