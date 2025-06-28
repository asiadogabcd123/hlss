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
            v-model="username"
            type="text"
            placeholder="請輸入工號"
            required
          >
        </div>
        
        <div class="form-group">
          <label for="password">密碼</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="請輸入密碼"
            required
          >
        </div>
        
        <button type="submit" class="login-btn">
          <i class="fas fa-sign-in-alt"></i> 登入系統
        </button>
        
        <div v-if="errorMessage" class="error-message">
          <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
        </div>
      </form>
    </div>
    
    <footer class="login-footer">
      <p>© 2025 置地酒店管理系統 </p>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const errorMessage = ref('')

const handleLogin = async () => {
  try {
    errorMessage.value = ''
    await auth.staffLogin(username.value, password.value)
    router.push('/staff/dashboard')
  } catch (error) {
    errorMessage.value = '登入失敗：帳號或密碼不正確'
    console.error('登入錯誤:', error)
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
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 10px;
  padding: 2rem;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}

.logo {
  text-align: center;
  margin-bottom: 2rem;
  color: #2c3e50;
}

.logo i {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
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
}

.login-btn:hover {
  background: #2980b9;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  margin-top: 1rem;
}

.login-footer {
  margin-top: 2rem;
  color: white;
  text-align: center;
  opacity: 0.8;
}
</style>