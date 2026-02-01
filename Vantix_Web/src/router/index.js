import { createRouter, createWebHistory } from 'vue-router'

// ===== Layout =====
import UserLayout from '../layouts/UserLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'

// ===== Views User =====
import Home from '../views/user/Home.vue'
import Profile from '../views/user/Profile.vue'

// ===== Views Admin =====
import Users from '../views/admin/Users.vue'
import Departments from "../views/admin/Departments.vue";

// ===== Khai báo danh sách route =====
const routes = [

    // Redirect mặc định: truy cập "/" sẽ chuyển sang "/home"
    {
        path: '/',
        redirect: '/home'
    },

    // Giao diện người dùng (sau khi đăng nhập)
    {
        path: '/home',
        component: UserLayout,
        meta: { requiresAuth: true }, // Chỉ cho phép truy cập khi đã đăng nhập

        // Các route con của UserLayout
        children: [
            // Trang chủ người dùng [/home]
            {
                path: '',
                component: Home,
                meta: { title: 'Trang chủ | Vantix' } // Tiêu đề tab trình duyệt
            },

            // Trang thông tin cá nhân [/home/profile]
            {
                path: 'profile',
                component: Profile,
                meta: { title: 'Thông tin cá nhân | Vantix' }
            }
        ]
    },

    // Giao diện quản trị (sau khi đăng nhập và có role ADMIN)
    {
        path: '/admin',
        component: AdminLayout,
        meta: { requiresAuth: true, role: 'ADMIN' },
        children: [
            // {
            //     path: '',
            //     component: Dashboard,
            //     meta: { title: 'Dashboard | Vantix' }
            // },
            {
                path: 'users',
                component: Users,
                meta: { title: 'Quản lý người dùng | Vantix' }
            },
            {
                path: 'departments',
                component: Departments,
                meta: { title: 'Quản lý phòng ban | Vantix' }
            },
            // {
            //     path: 'settings',
            //     component: Settings,
            //     meta: { title: 'Cài đặt hệ thống | Vantix' }
            // }
        ]
    }

]

// ===== Khởi tạo router =====
const router = createRouter({
    history: createWebHistory(),
    routes
})

// ===== Tự động cập nhật tiêu đề trang theo route =====
router.beforeEach((to) => {
    // Lấy route gần nhất có meta.title (ưu tiên route con)
    const nearestWithTitle = to.matched
        .slice()
        .reverse()
        .find(r => r.meta?.title)

    // Gán title cho tab trình duyệt
    document.title = nearestWithTitle
        ? nearestWithTitle.meta.title
        : 'Vantix'
})

export default router
