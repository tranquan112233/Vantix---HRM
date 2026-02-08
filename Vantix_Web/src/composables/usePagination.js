import { ref, computed, watch } from 'vue'

export function usePagination(source, defaultPageSize = 10) {
    const currentPage = ref(1)
    const pageSize = ref(defaultPageSize)

    const totalPages = computed(() =>
        Math.max(1, Math.ceil(source.value.length / pageSize.value))
    )

    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * pageSize.value
        return source.value.slice(start, start + pageSize.value)
    })

    const changePage = (p) => {
        if (p >= 1 && p <= totalPages.value) {
            currentPage.value = p
        }
    }

    watch([source, pageSize], () => {
        currentPage.value = 1
    })

    return {
        currentPage,
        pageSize,
        totalPages,
        paginatedData,
        changePage
    }
}
