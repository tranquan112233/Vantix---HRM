import {createRouter, createWebHistory} from 'vue-router'

import Auth from "../views/Auth.vue";
import Home from "../views/Home.vue"
import Attendance from "../views/Attendance.vue";
import Contract from "../views/Contract.vue";
import ContractAnnex from "../views/ContractAnnex.vue";

const routes = [{
    path: '/', redirect: '/home'
}, {
    path: '/auth', name: 'Auth', component: Auth,
}, {
    path: '/home', name: 'Home', component: Home, meta: {requiresAuth: true}
}, {
    path: '/attendance', name: 'Attendance', component: Attendance, meta: {requiresAuth: true}
}, {
    path: '/contract', name: 'Contract', component: Contract, meta: {requiresAuth: true}
}, {
    path: '/contract/:id/annex', name: 'ContractAnnex', component: ContractAnnex, meta: {requiresAuth: true}
}]

const router = createRouter({
    history: createWebHistory(), routes
})

export default router