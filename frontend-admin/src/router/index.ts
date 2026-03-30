import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '管理端登录', public: true }
    },
    {
      path: '/',
      component: () => import('@/layout/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: '/dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '控制台' }
        },
        {
          path: '/users',
          name: 'users',
          component: () => import('@/views/users/UserManageView.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: '/items',
          name: 'items',
          component: () => import('@/views/items/ItemManageView.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: '/categories',
          name: 'categories',
          component: () => import('@/views/categories/CategoryManageView.vue'),
          meta: { title: '分类管理' }
        },
        {
          path: '/orders',
          name: 'orders',
          component: () => import('@/views/orders/OrderManageView.vue'),
          meta: { title: '订单管理' }
        }
      ]
    }
  ]
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('ADMIN_AUTH_TOKEN');
  if (!to.meta.public && !token) {
    next('/login');
    return;
  }
  if (to.path === '/login' && token) {
    next('/dashboard');
    return;
  }
  next();
});

router.afterEach((to) => {
  document.title = `${to.meta.title || '管理端'} - YU-Mall`;
});

export default router;