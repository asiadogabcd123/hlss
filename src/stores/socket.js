import { defineStore } from 'pinia'
import io from 'socket.io-client'

export const useSocketStore = defineStore('socket', {
  state: () => ({
    socket: null
  }),

  actions: {
    connect() {
      this.socket = io(process.env.VUE_APP_WS_URL, {
        auth: {
          token: localStorage.getItem('staffToken')
        }
      })
    },

    sendNotification(event, data) {
      this.socket.emit(event, data)
    }
  }
})