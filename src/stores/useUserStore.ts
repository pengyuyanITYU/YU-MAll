import {defineStore} from 'pinia'
import { reactive } from 'vue'


export const useUserStore = defineStore('userInfo',()=>{
    interface UserInfo {
        username: string
        nickName: string
        avatar: string
        balance:string
        userId:number
        isLoggedIn:boolean
        levelName:string
        currentPoints:number
        email: string
        phone: string
        birthday: Date | string | null
        gender: number

    }
    const userInfo = reactive({
        username:'',
        nickName:'',
        avatar:'',
        balance:'',
        userId:0,
        isLoggedIn:false,
        levelName:'',
        currentPoints:0,
        email:'',
        phone:'',
        birthday: null,
        gender: null,

    })
    const updateUserInfo = (data:Partial<UserInfo>):void =>{
        Object.assign(userInfo,data)
        userInfo.isLoggedIn = true
    }

    const logout = ():void =>{
        userInfo.username = ''
        userInfo.nickName = ''
        userInfo.avatar = ''
        userInfo.balance = ''
        userInfo.userId = 0
        userInfo.isLoggedIn = false
        userInfo.levelName = ''
        userInfo.currentPoints = 0
        userInfo.email = ''
        userInfo.phone = ''
        userInfo.birthday = null
        userInfo.gender = null
        localStorage.removeItem('Authorization')
    }

    return {userInfo, updateUserInfo, logout}

},{persist:{storage:sessionStorage}}) 
