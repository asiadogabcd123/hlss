// @/api/luggage.js
import api from './index'

console.log('API 實例是否有效:', api)  
console.log('BaseURL:', api.defaults.baseURL)  

/**
 * 行李管理相關API
 * 包含寄存、查詢、取件等所有請求方法
 */
export default {
  /**
   * 創建行李寄存記錄
   * @param {Object} payload - 寄存信息
   * @returns {Promise} - 請求Promise
   */
  create(payload) {
    return api.post('/luggage', payload) 
  },
  
  /**
   * 獲取行李統計數據
   * @returns {Promise} - 統計信息Promise
   */
  getStats() {
    return api.get('/luggage/stats')
  },
  
  /**
   * 根據驗證碼查詢行李
   * @param {string} code - 驗證碼
   * @returns {Promise} - 行李信息Promise
   */
  findByCode(code) {
    return api.get(`/luggage/${code}`)
  },


  // === 工作人員標籤管理API ===
  
  /**
   * 獲取待打印行李標籤列表 (qr_generated=0)
   * @param {Object} params - 查詢參數（date或startDate+endDate）
   * @returns {Promise} - 行李列表Promise
   */
  getPendingForQR(params = {}) {
    const validatedParams = {}
    if (params.date) {
      validatedParams.date = params.date
    } else if (params.startDate && params.endDate) {
      validatedParams.startDate = params.startDate
      validatedParams.endDate = params.endDate
    }
    return api.get('/luggage/pending-qr', { params: validatedParams })
  },
  
  /**
   * 獲取可重新打印行李標籤列表 (qr_generated=1)
   * @param {Object} params - 查詢參數（date或startDate+endDate）
   * @returns {Promise} - 行李列表Promise
   */
  getPrintedLuggage(params = {}) {
    const validatedParams = {}
    if (params.date) {
      validatedParams.date = params.date
    } else if (params.startDate && params.endDate) {
      validatedParams.startDate = params.startDate
      validatedParams.endDate = params.endDate
    }
    return api.get('/luggage/printed-qr', { params: validatedParams })
  },
  
  /**
   * 獲取全部當前寄存的行李（未取件狀態）
   * @returns {Promise} - 行李列表Promise
   */
  getAllCurrentLuggage() {
    return api.get('/luggage/current')
  },
  
  /**
    * 新增：查詢指定日期的「今日寄存」記錄（專用API）
    * @param {string} date - 查詢日期（格式YYYY-MM-DD）
    * @returns {Promise} - 今日寄存列表
  */
  getTodayStoredLuggage(date) {
    return api.get('/luggage/today-stored', {
      params: { date: date.trim() }  // 傳遞日期參數
    })
  },
  

  /**
   * 新增：查詢指定日期的「今日取件」記錄（專用API）
   * @param {string} date - 查詢日期（格式YYYY-MM-DD）
   * @returns {Promise} - 今日取件列表
   */
  getTodayRetrievedLuggage(date) {
    return api.get('/luggage/today-retrieved', {
      params: { date: date.trim() }
    })
  },

  /**
   * 根據ID獲取行李詳情
   * @param {number|string} id - 行李ID或訂單編號（如LUG-123）
   * @returns {Promise} - 行李詳情Promise
   */
  getLuggageById(id) {
    // 處理業務編號格式（如LUG-123）
    const processedId = typeof id === 'string' && id.startsWith('LUG-') 
      ? id.replace('LUG-', '') 
      : id;
    return api.get(`/luggage/detail/${processedId}`)
  },
  
  /**
   * 生成行李標籤
   * @param {number} id - 行李ID
   * @param {number} count - 標籤數量
   * @returns {Promise} - 生成結果Promise
   */
  generateLabels(id, count) {
    return api.post(`/luggage/${id}/generate-labels`, null, {
      params: { count }
    })
  },

  /**
   * 標記行李為已生成QR碼
   * @param {number} id - 行李ID
   * @returns {Promise} - 標記結果Promise
   */
  markAsQrGenerated(id) {
    return api.put(`/luggage/${id}/mark-printed`)
  },

  
  /**
   * 完成取件操作
   * @param {number} id - 行李ID
   * @param {object} payload - 取件數據（包含scannedLuggage數組）
   * @returns {Promise} - 取件結果Promise
   */
  completeRetrieval(id, payload) {
    return api.post(`/luggage/${id}/retrieve`, payload)
  },
  
  /**
  * 將過期行李標記為已刪除狀態（僅更新狀態）
  * @param {number} id - 行李ID
  * @returns {Promise} - 請求Promise
  */
  markAsDeleted(id) {
    return api.put(`/luggage/mark-delete/${id}`)
  },

  /**
   * 根據行李標籤獲取行李信息
   * @param {string} tag - 行李標籤 (格式: LUG-{id}-{index})
   * @returns {Promise} - 行李信息Promise
   */
  getLuggageByTag(tag) {
    return api.get(`/luggage/tag/${tag}`)
  }
}