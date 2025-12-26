
import {request} from '@/utils/request';

export interface PasswordDTO{
    phone: string;
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface UserBasicInfoDTO{
    nickName: string;
    avatar: string;
    phone: string;
    email: string;
    birthday: Date | string | null;
    gender: number;
}

export const updateUserInfo = (data:UserBasicInfoDTO) => request.put(`/users`,data);

export const updatePassword = (data:PasswordDTO) => request.put(`/users/password`,data);


export const getUserInfo = () => request.get(`/users`);
