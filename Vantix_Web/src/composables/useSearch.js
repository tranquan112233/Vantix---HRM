import { ref, computed } from 'vue'

/**
 * Composable search nhiều field (hỗ trợ field lồng sâu)
 *
 * @param {Ref<Array>} sourceData - data gốc (vd: users)
 * @param {Array<string>} fields - ['username', 'email', 'role.roleName']
 */
export function useSearch(sourceData, fields = []) {
    const searchText = ref('')

    /**
     * Lấy giá trị theo path lồng sâu
     * vd: role.roleName
     */
    const getValueByPath = (obj, path) => {
        if (!obj || !path) return null

        return path
            .split('.')
            .reduce((acc, key) => acc?.[key], obj)
    }

    const filteredData = computed(() => {
        const keyword = searchText.value.trim().toLowerCase()
        if (!keyword) return sourceData.value

        return sourceData.value.filter(item =>
            fields.some(field => {
                const value = getValueByPath(item, field)
                if (value === null || value === undefined) return false

                return value
                    .toString()
                    .toLowerCase()
                    .includes(keyword)
            })
        )
    })

    return {
        searchText,
        filteredData
    }
}
