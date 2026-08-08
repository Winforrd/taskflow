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
   * 工作台
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

      {
        path: '',

        redirect: '/dashboard'
      },


      {
        path: 'dashboard',

        name: 'Dashboard',

        component: () =>
          import(
            '@/views/dashboard/DashboardView.vue'
          ),

        meta: {
          requiresAuth: true
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
 * 路由守卫
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
     * 未登录
     */
    if (
      to.meta.requiresAuth
      && !token
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
     * 已经登录
     * 又访问登录注册页
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