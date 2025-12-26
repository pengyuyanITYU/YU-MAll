import {request} from '@/utils/request';
import { FormInstance } from 'element-plus';
interface LoginForm {
  username: string
  password: string
  remember: boolean
}
export const login = (loginForm:LoginForm) => request.post('/users/login',loginForm);
