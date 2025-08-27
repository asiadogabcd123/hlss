<!-- src/components/Notification.vue -->
<template>
  <div 
    class="notification" 
    :class="[type, { show: show }]"
    v-if="show"
  >
    <div class="notification-icon">
      <i class="fas" :class="iconClass"></i>
    </div>
    <div class="notification-content">
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
    </div>
    <button class="close-btn" @click="$emit('close')">
      <i class="fas fa-times"></i>
    </button>
  </div>
</template>

<script>
import { computed } from 'vue';

export default {
  props: {
    show: Boolean,
    type: {
      type: String,
      default: 'success'
    },
    title: String,
    message: String
  },
  setup(props) {
    const iconClass = computed(() => {
      return {
        'success': 'fa-check-circle',
        'error': 'fa-exclamation-circle'
      }[props.type] || 'fa-info-circle';
    });
    
    return {
      iconClass
    };
  }
};
</script>

<style scoped>
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  background: white;
  border-radius: 10px;
  padding: 15px 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  gap: 15px;
  z-index: 1000;
  transform: translateX(150%);
  transition: transform 0.5s ease;
  min-width: 300px;
}

.notification.show {
  transform: translateX(0);
}

.notification.success {
  border-left: 5px solid #4caf50;
}

.notification.error {
  border-left: 5px solid #f44336;
}

.notification-icon {
  font-size: 1.8rem;
}

.notification.success .notification-icon {
  color: #4caf50;
}

.notification.error .notification-icon {
  color: #f44336;
}

.notification-content {
  flex: 1;
}

.notification-content h3 {
  margin-bottom: 5px;
  color: #333;
}

.notification-content p {
  color: #666;
  font-size: 0.9rem;
}

.close-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 1rem;
  padding: 5px;
}

.close-btn:hover {
  color: #666;
}
</style>