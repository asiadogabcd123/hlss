// src/stores/qr.js
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useQrStore = defineStore('qr', () => {
  const qrData = ref('');

  const generateQr = () => {
    qrData.value = `${window.location.origin}/retrieve`; // 纯路径，无参数
  };

  return { qrData, generateQr };
});