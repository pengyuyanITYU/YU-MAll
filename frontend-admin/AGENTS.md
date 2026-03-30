# Frontend-Admin

管理端 Vue 3 前端，后台运营操作界面。

## STRUCTURE

```
src/
├── api/           # Admin API（/admin/* 端点）
├── layout/        # AdminLayout 侧边栏+顶栏
├── router/        # 路由守卫校验 ADMIN_AUTH_TOKEN
├── stores/        # auth.ts 登录态
├── types/         # 类型定义
└── views/         # dashboard, users, items, orders, categories
```

## VUE PATTERNS

```typescript
// 分页查询
const query = reactive({ pageNo: 1, pageSize: 10, name: '' });
const rows = ref<ItemModel[]>([]);
async function load() {
  const res = await listItems(query);
  rows.value = res.rows || [];
}

// 表单 CRUD
const formVisible = ref(false);
const form = reactive({ id: undefined, name: '' });
async function submit() {
  form.id ? await updateItem(form) : await createItem(form);
}

// 删除确认
await ElMessageBox.confirm(`确认删除「${row.name}」?`, '提示', { type: 'warning' });
```

## ADMIN FEATURES

- **API 端点**：全部 `/admin/*` 前缀
- **认证**：`localStorage.ADMIN_AUTH_TOKEN`
- **布局**：`AdminLayout.vue` 包裹业务页
- **状态管理**：`useAuthStore()` 存储登录信息
- **表格分页**：`el-table` + `el-pagination`
## GOTCHAS

- Java Long 已转字符串，前端 ID 作为 string 处理
- 价格：后端存分，前端 `money(val)` 转元显示

## DEV

```bash
cd frontend-admin && npm install && npm run dev
```
