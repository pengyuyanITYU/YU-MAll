import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
// 路由懒加载引入
const Home = () => import('../views/items/Home.vue')
// const ItemDetail = () => import('../views/ItemDetail.vue')
const Login = () => import('../views/login/Login.vue')
const Register = () => import('../views/register/Register.vue')

const ItemDetail = () => import('../views/items/ItemDetail.vue')

const Search = () => import('../views/search/Search.vue')

const User = () => import('../views/user/User.vue')

const Cart = () => import('../views/cart/Cart.vue')

const Checkout = () => import('../views/order/Order.vue')

const OrderDetail = () => import('../views/order/OrderDetail.vue')

const Pay = () => import('../views/pay/index.vue')

// 1. 使用 RouteRecordRaw 类型定义路由数组
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '商城首页 - YU-MAll' }
  },  
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '用户登录', hideLayout: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '用户注册', hideLayout: true }
  },
  {
    path: '/item/:id',
    name: 'ItemDetail',
    component: ItemDetail,
    meta: { title: '商品详情' }
  },
  {
    path: '/search',
    name:'Search',
    component: Search,
    meta: { title: '商品搜索' }
  },
  {
    path: '/user',
    name: 'User',
    component: User,
    meta: { title: '用户中心', requiresAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { title: '购物车' , requiresAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
    meta: { title: '确认订单', requiresAuth: true }
  },{
    // 订单详情页路由配置
    // :id 是动态参数，对应订单号
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetail, // 下面提供的详情页组件
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
  path: '/pay',
  name: 'Pay',
  component: Pay,
  meta: { title: '订单支付' }
},
 {
    path: '/comment/publish',
    name: 'PublishComment',
    // 建议使用懒加载 (Lazy Loading)，优化首屏加载速度
    component: () => import('@/views/comment/PublishComment.vue'), 
    meta: {
      title: '发表评价',
      requiresAuth: true // 如果你有登录拦截逻辑，记得加上这个
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // TS 会自动推断 scrollBehavior 参数的类型，不需要手动写
  scrollBehavior(to, from, savedPosition) {
    // 切换页面时自动滚动到顶部
    return { top: 0 }
  }
})

// 全局后置钩子
router.afterEach((to) => {
  // 2. 这里需要断言一下类型，或者确保 title 存在
  const title = to.meta.title as string | undefined
  document.title = title || 'YU-Mall'
})

router.beforeEach((to,from,next)=>{
  if(to.matched.some(record => record.meta.requiresAuth)){
    const loginUser = localStorage.getItem('Authorization')
    if(loginUser){
      next()
    }
    else{
      ElMessage.error('登录过期，请重新登录')
      next('/login')
    }
  }
  else{
    next()
  }
})

// 路由错误监听
router.onError((error) => {
  const pattern = /Loading chunk (\d)+ failed/g;
  const isChunkLoadFailed = error.message.match(pattern);
  
  // 也可以检查动态导入失败
  const isImportFailed = error.message.includes('Failed to fetch dynamically imported module');

  if (isChunkLoadFailed || isImportFailed) {
    // 防止无限刷新：检查是否刚刚刷新过
    const targetPath = router.currentRoute.value.fullPath;
    
    console.log('资源加载失败，尝试自动刷新...');
    
    // 使用 replace 重新加载当前页面，强制从服务器拉取最新资源
    window.location.reload(); 
  }
});

export default router