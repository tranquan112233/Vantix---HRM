import api from './axios.js'

const leaveTypeService = {

    getAll() {
        return api.get("/leave-types");
    }

};

export default leaveTypeService;