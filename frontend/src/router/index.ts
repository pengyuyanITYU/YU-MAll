import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderFlowHistoryRedirect, shouldBlockOrderFlowBack } from '@/utils/orderFlowHistory'

const Home = () => import('../views/Home.vue')
const Login = () => import('../views/login/Login.vue')
const Register = () => import('../views/register/Register.vue')
const AuthPreviewLab = () => import('../views/auth-preview/AuthPreviewLab.vue')
const ItemDetail = () => import('../views/items/ItemDetail.vue')
const Shop = () => import('../views/shop/Shop.vue')
const Search = () => import('../views/search/Search.vue')
const User = () => import('../views/user/User.vue')
const Cart = () => import('../views/cart/Cart.vue')
const Checkout = () => import('../views/order/Order.vue')
const OrderDetail = () => import('../views/order/OrderDetail.vue')
const Pay = () => import('../views/pay/index.vue')
const AiAssistant = () => import('../views/ai/AiAssistant.vue')
const PublishComment = () => import('@/views/comment/PublishComment.vue')

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: 'YU-MALL Home', hideNavBar: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: 'User Login', hideLayout: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: 'User Register', hideLayout: true }
  },
  {
    path: '/auth-preview',
    name: 'AuthPreviewLab',
    component: AuthPreviewLab,
    meta: { title: 'Auth Preview Lab', hideLayout: true }
  },
  {
    path: '/item/:id',
    name: 'ItemDetail',
    component: ItemDetail,
    meta: { title: 'Item Detail' }
  },
  {
    path: '/shop/:shopId',
    name: 'Shop',
    component: Shop,
    meta: { title: 'Shop Items' }
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
    meta: { title: 'Search' }
  },
  {
    path: '/user',
    name: 'User',
    component: User,
    meta: { title: 'User Center', requiresAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { title: 'Cart', requiresAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
    meta: { title: 'Checkout', requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { title: 'Order Detail', requiresAuth: true }
  },
  {
    path: '/pay',
    name: 'Pay',
    component: Pay,
    meta: { title: 'Payment' }
  },
  {
    path: '/comment/publish',
    name: 'PublishComment',
    component: PublishComment,
    meta: { title: 'Publish Comment', requiresAuth: true }
  },
  {
    path: '/ai-assistant',
    name: 'AiAssistant',
    component: AiAssistant,
    meta: { title: 'AI Assistant' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

let isPopStateNavigation = false

if (typeof window !== 'undefined') {
  window.addEventListener('popstate', () => {
    isPopStateNavigation = true
  })
}

router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title || 'YU-Mall'
  isPopStateNavigation = false
})

router.beforeEach((to, from, next) => {
  if (isPopStateNavigation && shouldBlockOrderFlowBack(to.path)) {
    const redirectTarget = router.resolve(getOrderFlowHistoryRedirect())
    next({
      path: redirectTarget.path,
      query: redirectTarget.query,
      hash: redirectTarget.hash,
      replace: true
    })
    return
  }

  if (to.matched.some((record) => record.meta.requiresAuth)) {
    const loginUser = localStorage.getItem('Authorization')
    if (loginUser) {
      next()
      return
    }
    ElMessage.error('登录过期，请重新登录')
    next('/login')
    return
  }

  next()
})

router.onError((error) => {
  const pattern = /Loading chunk (\d)+ failed/g
  const isChunkLoadFailed = pattern.test(error.message)
  const isImportFailed = error.message.includes('Failed to fetch dynamically imported module')

  if (isChunkLoadFailed || isImportFailed) {
    window.location.reload()
  }
})

export default router
