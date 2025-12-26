import {request} from '@/utils/request';

interface RegisterForm {
  username: string
  phone: string
  password: string

}

export const register = (RegisterForm:RegisterForm) => request.post('/users/register',RegisterForm);