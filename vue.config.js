const { defineConfig } = require('@vue/cli-service')
const webpack = require('webpack')

module.exports = defineConfig({
  devServer: {
    port: 8081, 
    proxy: {
      '/api': {
        target: 'http://localhost:8001', 
        changeOrigin: true,
        pathRewrite: {
          '^/api': '' 
        }
      }
    }
  },
  transpileDependencies: true,
  configureWebpack: {
    plugins: [
      new webpack.DefinePlugin({
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: JSON.stringify(false),
        'process.env': {
          VUE_APP_VERSION: JSON.stringify(process.env.VUE_APP_VERSION),
          VUE_APP_API_BASE: JSON.stringify(process.env.VUE_APP_API_BASE)
        }
      })
    ]
  }
})