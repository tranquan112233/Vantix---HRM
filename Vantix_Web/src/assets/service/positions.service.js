import api from "./api.js";

const ENDPOINT = '/positions';

export default {
    getAllPositions() {
        return api.get(`${ENDPOINT}/getAllPositions`)
    }
}