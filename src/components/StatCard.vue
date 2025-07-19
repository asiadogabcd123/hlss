<template>
  <div class="stat-card" :style="{ borderLeft: `4px solid ${color}` }">
    <div class="icon">
      <i :class="`fas fa-${icon}`"></i>
    </div>
    <div class="content">
      <h3>{{ title }}</h3>
      <div v-if="loading" class="loading-placeholder"></div>
      <p v-else class="value">{{ value }}</p>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
defineProps({
  icon: String,
  title: String,
  value: [String, Number],
  color: {
    type: String,
    default: '#3498db'
  }
})
</script>

<style scoped>

.loading-placeholder {
  width: 60px;
  height: 24px;
  background: #eee;
  border-radius: 4px;
  animation: pulse 1.5s infinite ease-in-out;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.icon {
  font-size: 1.5rem;
  margin-right: 15px;
  color: inherit;
}

.content h3 {
  margin: 0;
  font-size: 0.9rem;
  color: #7f8c8d;
}

.content p {
  margin: 5px 0 0;
  font-size: 1.5rem;
  font-weight: bold;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 0.3; }
  100% { opacity: 0.6; }
}
</style>