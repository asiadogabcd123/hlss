<template>
  <div class="staff-login-container">
    <div class="login-box">
      <div class="logo">
        <i class="fas fa-hotel"></i>
        <h1>酒店行李系統</h1>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">工作人員帳號</label>
          <input
            id="username"
            v-model.trim="username"
            type="text"
            placeholder="請輸入工號"
            required
            autocomplete="username"
            :disabled="isLoading"
          >
        </div>
        
        <div class="form-group password-group">
          <label for="password">密碼</label>
          <div class="password-input-wrapper">
            <input
              id="password"
              v-model.trim="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="請輸入密碼"
              required
              autocomplete="current-password"
              :disabled="isLoading"
              @keyup.enter="handleLogin"
            >
            <button 
              type="button" 
              class="toggle-password"
              @click="showPassword = !showPassword"
              tabindex="-1"
            >
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>
        
        <button 
          type="submit" 
          class="login-btn"
          :class="{ loading: isLoading }"
          :disabled="isLoading"
        >
          <template v-if="!isLoading">
            <i class="fas fa-sign-in-alt"></i> 登入系統
          </template>
          <template v-else>
            <i class="fas fa-spinner fa-spin"></i> 登入中...
          </template>
        </button>
        
        <div v-if="errorMessage" class="error-message">
          <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
        </div>
      </form>
    </div>
    
    <footer class="login-footer">
      <p>© {{ currentYear }} 置地酒店管理系統</p>
      <p class="version">v{{ appVersion }}</p>
    </footer>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export default {
  setup() {
    const router = useRouter()
    const authStore = useAuthStore() // 统一使用 authStore 命名

    const username = ref('')
    const password = ref('')
    const errorMessage = ref('')
    const isLoading = ref(false)
    const showPassword = ref(false)

    const currentYear = computed(() => new Date().getFullYear())
    const appVersion = process.env.VUE_APP_VERSION || '1.0.0'

    const handleLogin = async () => {
      if (!username.value || !password.value) {
        errorMessage.value = '請輸入帳號和密碼'
        return
      }

      try {
        isLoading.value = true
        errorMessage.value = ''
        
        // 使用 authStore 而不是 auth
        const result = await authStore.staffLogin(username.value, password.value)
        
        if (result?.token) {
          console.log('跳转到仪表盘...')
          const redirectPath = router.currentRoute.value.query.redirect || '/staff/dashboard'
          router.replace(redirectPath)
        }
      } catch (error) {
        console.error('登录流程错误:', error)
        errorMessage.value = error.message
        password.value = ''
      } finally {
        isLoading.value = false
      }
    }

    onMounted(() => {
      document.getElementById('username')?.focus()
    })

    return {
      username,
      password,
      errorMessage,
      isLoading,
      showPassword,
      currentYear,
      appVersion,
      handleLogin
    }
  }
}
</script>

<style scoped>
.staff-login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a2a6c, #b21f1f);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 10px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.logo {
  text-align: center;
  margin-bottom: 2rem;
  color: #2c3e50;
}

.logo i {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: #3498db;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #34495e;
}

.form-group input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  border-color: #3498db;
  outline: none;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.password-group {
  position: relative;
}

.password-input-wrapper {
  position: relative;
}

.toggle-password {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  padding: 5px;
}

.login-btn {
  padding: 12px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover:not(:disabled) {
  background: #2980b9;
}

.login-btn:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.login-btn.loading {
  cursor: wait;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
  animation: shake 0.5s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-5px); }
  40%, 80% { transform: translateX(5px); }
}

.login-footer {
  margin-top: 2rem;
  color: white;
  text-align: center;
  opacity: 0.8;
  font-size: 0.9rem;
}

.login-footer .version {
  font-size: 0.8rem;
  margin-top: 0.5rem;
  opacity: 0.6;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .login-box {
    padding: 1.5rem;
  }
  
  .logo i {
    font-size: 2.5rem;
  }
}
</style>