// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const staffToken = ref(localStorage.getItem('staffToken'))
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isLoading = ref(false)

  const staffLogin = async (username, password) => {
    try {
      isLoading.value = true;
      
      // 加強請求日誌
      console.log('發送登入請求至 /api/auth/login', { 
        username: username.trim() 
      });
  
      const response = await api.post('/auth/login', { 
        username: username.trim(),
        password: password.trim()
      });
  
      // 完整日誌記錄
      console.log('完整API回應:', {
        status: response.status,
        headers: response.headers,
        data: response.data
      });
  
      // 安全解構（添加預設值）
      const { 
        token = null, 
        expiresAt = null,
        staffName = '未知使用者',
        role = 'STAFF' 
      } = response.data || {};
  
      if (!token) {
        throw new Error('後端回傳資料缺少 token 欄位');
      }
  
      // 轉換時間格式
      const expiryTimestamp = expiresAt 
        ? new Date(expiresAt).getTime() 
        : Date.now() + 7 * 24 * 60 * 60 * 1000; // 預設7天有效期
  
      // 儲存到本地
      localStorage.setItem('staffToken', token);
      localStorage.setItem('userInfo', JSON.stringify({ staffName, role }));
      localStorage.setItem('tokenExpiry', expiryTimestamp.toString());
  
      // 更新Pinia狀態
      staffToken.value = token;
      userInfo.value = { staffName, role };
  
      console.log('登入狀態更新完成', {
        token: staffToken.value,
        userInfo: userInfo.value,
        expiryTime: new Date(expiryTimestamp).toLocaleString()
      });
  
      return { token, staffName, role };
    } catch (error) {
      console.error('登入流程錯誤:', {
        errorName: error.name,
        message: error.message,
        stack: error.stack
      });
      
      logout();
      throw new Error(
        error.response?.data?.message || 
        '登入失敗，請檢查帳號密碼或網路連接'
      );
    } finally {
      isLoading.value = false;
    }
  }

  const logout = () => {
    localStorage.removeItem('staffToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('tokenExpiry')
    staffToken.value = null
    userInfo.value = null
    console.log('已登出，状态已清空')
  }

  const checkAuth = () => {
    const expiry = localStorage.getItem('tokenExpiry')
    return staffToken.value && expiry && Date.now() < parseInt(expiry)
  }

  return { 
    staffToken, 
    userInfo,
    isLoading,
    staffLogin, 
    logout,
    checkAuth
  }
})