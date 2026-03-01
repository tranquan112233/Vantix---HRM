import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import authStore from '@/stores/authStore'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import * as bootstrap from 'bootstrap'
window.bootstrap = bootstrap

createApp(App)
    .use(router)
    .mount('#app')
if (localStorage.getItem('token')) {
    authStore.loadUser()
}
