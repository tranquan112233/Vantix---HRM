import api from "@/services/axios";

const ENDPOINT = '/salaries';

export default {
    getDepartmentNames() {
        return api.get(`${ENDPOINT}/departments`);
    },

    getSalaries(month, year) {
        return api.get(`${ENDPOINT}`, {
            params: {
                month: month, year: year
            }
        });
    },

    submitAllToPending(month, year) {
        return api.put(`${ENDPOINT}/submit-all-pending`, null, {
            params: {
                month: month,
                year: year
            }
        });
    },

    finalizePayrollBatch(month, year) {
        return api.post(`/payroll-batches/finalize`, null, {
            params: { month, year }
        });
    }
}