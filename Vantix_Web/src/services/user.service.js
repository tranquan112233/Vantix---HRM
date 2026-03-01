import api from "@/services/axios";

class userService {

    // ================= GET ALL =================
    getAll() {
        return api.get("/users")
    }

    // ================= GET BY ID =================
    getById(id) {
        return api.get(`/users/${id}`)
    }

    // ================= CREATE =================
    create(data) {
        return api.post("/users", data)
    }

    // ================= UPDATE =================
    update(id, data) {
        return api.put(`/users/${id}`, data)
    }

    // ================= LOCK =================
    lock(id) {
        return api.put(`/users/${id}/lock`)
    }

    // ================= UNLOCK =================
    unlock(id) {
        return api.put(`/users/${id}/unlock`)
    }

}

export default new userService();