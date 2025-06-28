// src/stores/qr.js
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useQrStore = defineStore('qr', () => {
  const qrUrl = ref('');
  const generateQr = (staffId) => {
    qrUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(
      `${window.location.origin}/retrieve?staff=${staffId}`
    )}`;
  };

  return { qrUrl, generateQr };
});