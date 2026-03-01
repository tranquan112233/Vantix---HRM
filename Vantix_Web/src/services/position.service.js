import api from './axios.js'

export default {
  getAll() {
    return api.get('/positions')
  }
}