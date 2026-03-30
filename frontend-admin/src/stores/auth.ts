import { defineStore } from 'pinia';

interface AuthState {
  token: string;
  userId: number | null;
  nickName: string;
  avatar: string;
}

const TOKEN_KEY = 'ADMIN_AUTH_TOKEN';
const USER_KEY = 'ADMIN_AUTH_USER';

function readPersistedState(): AuthState {
  const token = localStorage.getItem(TOKEN_KEY) || '';
  const userRaw = localStorage.getItem(USER_KEY);
  if (!userRaw) {
    return { token, userId: null, nickName: '', avatar: '' };
  }
  try {
    const parsed = JSON.parse(userRaw);
    return {
      token,
      userId: parsed.userId ?? null,
      nickName: parsed.nickName ?? '',
      avatar: parsed.avatar ?? ''
    };
  } catch {
    return { token, userId: null, nickName: '', avatar: '' };
  }
}

export const useAuthStore = defineStore('admin-auth', {
  state: (): AuthState => readPersistedState(),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setLogin(payload: { token: string; userId?: number; nickName?: string; avatar?: string }) {
      this.token = payload.token;
      this.userId = payload.userId ?? null;
      this.nickName = payload.nickName ?? '';
      this.avatar = payload.avatar ?? '';
      localStorage.setItem(TOKEN_KEY, this.token);
      localStorage.setItem(
        USER_KEY,
        JSON.stringify({
          userId: this.userId,
          nickName: this.nickName,
          avatar: this.avatar
        })
      );
    },
    logout() {
      this.token = '';
      this.userId = null;
      this.nickName = '';
      this.avatar = '';
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }
});