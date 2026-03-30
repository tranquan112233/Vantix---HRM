import { createApp }  from 'vue'
import { createPinia } from 'pinia'
import App             from './App.vue'
import router          from './router'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'

import './style.css'

createApp(App)
    .use(createPinia())
    .use(router)
    .use(Toast, {
    position: 'top-center',
    timeout: 3000,
    closeOnClick: true,
    pauseOnFocusLoss: false,
    pauseOnHover: true,
    draggable: true,
    draggablePercent: 0.6,
    showCloseButtonOnHover: false,
    hideProgressBar: false,
    closeButton: 'button',
    icon: true,
    rtl: false
    })
    .mount('#app')