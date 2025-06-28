<!-- src/components/staff/QrGenerator.vue -->
<template>
  <button @click="generateQr">生成寄存二维码</button>
  <img v-if="qrStore.currentQrUrl" :src="qrStore.currentQrUrl" alt="寄存二维码" />
</template>

<script setup>
import { useQrStore } from '@/stores/qr';
import { useAuthStore } from '@/stores/auth';

const qrStore = useQrStore();
const authStore = useAuthStore();

const generateQr = () => {
  qrStore.generateQr(authStore.staffId, 'BRANCH_001'); // 从authStore或API获取真实数据
};
</script>