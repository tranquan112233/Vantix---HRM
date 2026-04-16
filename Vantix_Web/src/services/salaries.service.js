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
                month: month, year: year
            }
        });
    },

    finalizePayrollBatch(month, year) {
        return api.post(`/payroll-batches/finalize/${month}/${year}`);
    },

    generateSalaries(month, year) {
        return api.post(`${ENDPOINT}/generate/${month}/${year}`);
    },

    exportSalariesExcel(month, year) {
        return api.get(`${ENDPOINT}/export`, {
            params: {month, year}, responseType: 'blob'
        });
    },

    exportSinglePayslipExcel(id) {
        return api.get(`${ENDPOINT}/export/${id}`, {
            responseType: 'blob'
        });
    },
}