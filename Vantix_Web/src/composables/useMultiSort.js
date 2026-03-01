import { ref, computed } from 'vue'

export function useMultiSort(sourceData) {
    const sortKey = ref(null)   // vd: 'username', 'role.roleName'
    const sortDir = ref('asc')  // 'asc' | 'desc'

    /** Lấy value theo path lồng sâu */
    const getValueByPath = (obj, path) => {
        if (!obj || !path) return null
        return path.split('.').reduce((acc, key) => acc?.[key], obj)
    }

    /** Click sort */
    const sort = (key) => {
        if (sortKey.value === key) {
            sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
        } else {
            sortKey.value = key
            sortDir.value = 'asc'
        }
    }

    /** Icon sort (Bootstrap icon) */
    const sortIcon = (key) => {
        if (sortKey.value !== key) return 'bi-arrow-down-up'
        return sortDir.value === 'asc'
            ? 'bi-sort-up'
            : 'bi-sort-down'
    }

    const sortedData = computed(() => {
        if (!sortKey.value) return sourceData.value

        const dir = sortDir.value === 'asc' ? 1 : -1

        return [...sourceData.value].sort((a, b) => {
            const valA = getValueByPath(a, sortKey.value)
            const valB = getValueByPath(b, sortKey.value)

            // null / undefined đẩy xuống cuối
            if (valA == null && valB == null) return 0
            if (valA == null) return 1
            if (valB == null) return -1

            // number
            if (typeof valA === 'number' && typeof valB === 'number') {
                return (valA - valB) * dir
            }

            // date
            if (!isNaN(Date.parse(valA)) && !isNaN(Date.parse(valB))) {
                return (new Date(valA) - new Date(valB)) * dir
            }

            // string
            return valA
                .toString()
                .localeCompare(valB.toString(), 'vi', {
                    sensitivity: 'base'
                }) * dir
        })
    })

    return {
        sortKey,
        sortDir,
        sort,
        sortIcon,
        sortedData
    }
}
