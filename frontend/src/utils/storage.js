const STORAGE_KEY_PREFIX = 'supermarket_'
const MAX_STORAGE_SIZE = 4.5 * 1024 * 1024

export function useStorage() {
  const getStorageSize = () => {
    try {
      const total = Object.keys(localStorage).reduce((sum, key) => {
        const item = localStorage.getItem(key)
        return sum + (item ? item.length : 0)
      }, 0)
      return total
    } catch (e) {
      return MAX_STORAGE_SIZE
    }
  }

  const cleanupOldData = () => {
    try {
      const items = []
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i)
        if (key.startsWith(STORAGE_KEY_PREFIX)) {
          const item = localStorage.getItem(key)
          items.push({ key, size: item ? item.length : 0 })
        }
      }
      items.sort((a, b) => a.size - b.size)
      let currentSize = getStorageSize()
      for (const item of items) {
        if (currentSize < MAX_STORAGE_SIZE * 0.9) break
        localStorage.removeItem(item.key)
        currentSize -= item.size
      }
    } catch (e) {
      console.error('Storage cleanup failed:', e)
    }
  }

  const get = (key, defaultValue = null) => {
    try {
      const fullKey = STORAGE_KEY_PREFIX + key
      const value = localStorage.getItem(fullKey)
      if (value === null) return defaultValue
      try {
        return JSON.parse(value)
      } catch {
        return value
      }
    } catch (e) {
      console.error('Storage get failed:', e)
      return defaultValue
    }
  }

  const set = (key, value) => {
    try {
      const fullKey = STORAGE_KEY_PREFIX + key
      const serialized = typeof value === 'string' ? value : JSON.stringify(value)
      
      if (getStorageSize() + serialized.length > MAX_STORAGE_SIZE) {
        cleanupOldData()
      }
      
      localStorage.setItem(fullKey, serialized)
      return true
    } catch (e) {
      console.error('Storage set failed:', e)
      cleanupOldData()
      try {
        const fullKey = STORAGE_KEY_PREFIX + key
        const serialized = typeof value === 'string' ? value : JSON.stringify(value)
        localStorage.setItem(fullKey, serialized)
        return true
      } catch (e2) {
        console.error('Storage set failed after cleanup:', e2)
        return false
      }
    }
  }

  const remove = (key) => {
    try {
      const fullKey = STORAGE_KEY_PREFIX + key
      localStorage.removeItem(fullKey)
    } catch (e) {
      console.error('Storage remove failed:', e)
    }
  }

  const clear = () => {
    try {
      for (let i = localStorage.length - 1; i >= 0; i--) {
        const key = localStorage.key(i)
        if (key.startsWith(STORAGE_KEY_PREFIX)) {
          localStorage.removeItem(key)
        }
      }
    } catch (e) {
      console.error('Storage clear failed:', e)
    }
  }

  return {
    get,
    set,
    remove,
    clear,
    getStorageSize
  }
}