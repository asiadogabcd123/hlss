<template>
  <div class="access-container">
    <div class="access-card">
      <div class="logo">
        <i class="fas fa-suitcase"></i>
        <h1>行李寄存服務</h1>
      </div>

      <form @submit.prevent="generateQR" class="access-form">
        <div class="form-group">
          <label for="phone">手機號碼</label>
          <input
            id="phone"
            v-model="phone"
            type="tel"
            placeholder="請輸入手機號碼"
            required
            pattern="[0-9]{10}"
            title="請輸入10位手機號碼"
          >
          <small>用於取件時驗證身份</small>
        </div>

        <button type="submit" class="btn btn-primary">
          <i class="fas fa-qrcode"></i> 生成取件二維碼
        </button>
      </form>

      <div class="instructions">
        <h3><i class="fas fa-info-circle"></i> 使用說明</h3>
        <ol>
          <li>輸入手機號碼並生成二維碼</li>
          <li>向工作人員出示此二維碼</li>
          <li>掃描後即可取回行李</li>
        </ol>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GuestAccess',
  data() {
    return {
      phone: ''
    }
  },
  methods: {
    generateQR() {
      if (!this.phone.match(/^[0-9]{10}$/)) {
        alert('請輸入有效的手機號碼（10位數字）')
        return
      }
      
      const bagId = 'BAG-' + Math.floor(1000 + Math.random() * 9000)
      this.$router.push(`/guest/qr/${bagId}?phone=${this.phone}`)
    }
  }
}
</script>

<style scoped>
.access-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #1a2a6c, #b21f1f);
}

.access-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.logo {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
}

.logo i {
  font-size: 3rem;
  margin-bottom: 10px;
}

.access-form {
  margin-bottom: 30px;
}

.instructions {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 15px;
  margin-top: 20px;
}

.instructions ol {
  padding-left: 20px;
  margin-top: 10px;
}

.instructions li {
  margin-bottom: 8px;
}
</style>