<template>
  <div class="identity-container">
    <div class="identity-header">
      <h1><i class="fas fa-user-circle"></i> 請選擇您的身份</h1>
    </div>

    <div class="identity-actions">
      <router-link to="/guest/access" class="identity-card guest-card">
        <i class="fas fa-user"></i>
        <span>我是客人</span>
        <p>寄存行李</p>
      </router-link>

      <router-link to="/staff/login" class="identity-card staff-card">
        <i class="fas fa-user-tie"></i>
        <span>我是工作人員</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
// 不需要额外的逻辑，纯展示组件
</script>

<style scoped>
.identity-container {
  padding: 40px 20px;
  max-width: 800px;
  margin: 0 auto;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.identity-header {
  text-align: center;
  margin-bottom: 50px;
}

.identity-header h1 {
  color: #2c3e50;
  font-size: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.identity-header i {
  font-size: 2.5rem;
  color: #3498db;
}

.identity-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.identity-card {
  background: white;
  border-radius: 15px;
  padding: 40px 30px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  color: #2c3e50;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.identity-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.identity-card i {
  font-size: 3.5rem;
  margin-bottom: 20px;
  display: block;
}

.identity-card span {
  font-weight: 600;
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.identity-card p {
  color: #7f8c8d;
  font-size: 1rem;
  margin-top: 10px;
}

.guest-card i {
  color: #2ecc71;
}

.staff-card i {
  color: #3498db;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .identity-actions {
    grid-template-columns: 1fr;
  }
  
  .identity-card {
    padding: 30px 20px;
  }
}
</style>