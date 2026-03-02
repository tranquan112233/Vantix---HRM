import api from "@/services/axios";

const ENDPOINT = '/contracts';

export default {
    getAllContracts() {
        return api.get(`${ENDPOINT}/getAllContracts`);
    },

    createContract(data) {
        return api.post(`${ENDPOINT}/postContract`, data);
    },

    deleteContract(id) {
        return api.delete(`${ENDPOINT}/deleteContract/${id}`);
    },

    updateContractStatus(id) {
        return api.put(`${ENDPOINT}/updateContractStatus/${id}`);
    }
}