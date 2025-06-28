<template>
  <div class="bag-list">
    <div v-for="bag in bags" :key="bag.id" class="bag-item">
      <div class="bag-id">{{ bag.id }}</div>
      <div class="bag-info">
        <h4>{{ bag.guestName }}</h4>
        <p>{{ bag.time.toLocaleString('zh-TW') }}</p>
      </div>
      <div class="bag-status" :class="bag.status.toLowerCase()">
        {{ bag.status }}
      </div>
    </div>
  </div>
</template>

<script setup>

import { defineProps } from 'vue'
defineProps({
  bags: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.bag-list {
  margin-top: 15px;
}

.bag-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.bag-id {
  font-weight: bold;
  margin-right: 15px;
  color: #e74c3c;
}

.bag-info {
  flex: 1;
}

.bag-info h4 {
  margin: 0;
  font-size: 1rem;
}

.bag-info p {
  margin: 5px 0 0;
  font-size: 0.8rem;
  color: #7f8c8d;
}

.bag-status {
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 0.8rem;
}

.bag-status.已寄存 {
  background: #e3f2fd;
  color: #1976d2;
}

.bag-status.已取件 {
  background: #e8f5e9;
  color: #388e3c;
}
</style>