import api from "@/services/axios";

const ENDPOINT = '/salaries';

export default {
    getDepartmentNames() {
        return api.get(`${ENDPOINT}/departments`);
    }
}