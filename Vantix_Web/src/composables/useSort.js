import { ref, computed } from "vue"

export function useSort(source, defaultKey = "id") {
    const sortKey = ref(defaultKey)
    const sortAsc = ref(true)

    const sortedData = computed(() => {
        return [...source.value].sort((a, b) => {
            const valA = a[sortKey.value]
            const valB = b[sortKey.value]

            if (valA < valB) return sortAsc.value ? -1 : 1
            if (valA > valB) return sortAsc.value ? 1 : -1
            return 0
        })
    })

    function sortBy(key) {
        if (sortKey.value === key) {
            sortAsc.value = !sortAsc.value
        } else {
            sortKey.value = key
            sortAsc.value = true
        }
    }

    function getSortIcon(column) {
        if (sortKey.value !== column) {
            return "bi bi-arrow-down-up text-muted"
        }

        return sortAsc.value
            ? "bi bi-arrow-up text-secondary"
            : "bi bi-arrow-down text-secondary"
    }

    return {
        sortKey,
        sortAsc,
        sortedData,
        sortBy,
        getSortIcon
    }
}