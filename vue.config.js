const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,

  devServer: {
    host: '0.0.0.0', // 允许局域网访问
    port: 8081,      // 固定端口
    hot: true,
    allowedHosts: 'all' // 允许所有主机访问
  }
  
})
