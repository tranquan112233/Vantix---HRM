import api from "./api.js";

const ENDPOINT = '/contracts';

export default {
    getAllContracts() {
        return api.get(`${ENDPOINT}/getAllContracts`)
    }
}