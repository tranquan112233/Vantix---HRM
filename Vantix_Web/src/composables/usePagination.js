// composables/usePagination.js
import { ref, computed, watch } from "vue"

export function usePagination(source, pageSizeRef) {
    const currentPage = ref(1)

    const totalPages = computed(() =>
        Math.ceil(source.value.length / pageSizeRef.value)
    )

    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * pageSizeRef.value
        return source.value.slice(start, start + pageSizeRef.value)
    })

    const visiblePages = computed(() => {
        const maxVisible = 5
        let start = Math.max(1, currentPage.value - 2)
        let end = Math.min(totalPages.value, start + maxVisible - 1)

        if (end - start < maxVisible - 1) {
            start = Math.max(1, end - maxVisible + 1)
        }

        const pages = []
        for (let i = start; i <= end; i++) {
            pages.push(i)
        }
        return pages
    })

    const startItem = computed(() =>
        source.value.length === 0
            ? 0
            : (currentPage.value - 1) * pageSizeRef.value + 1
    )

    const endItem = computed(() =>
        Math.min(currentPage.value * pageSizeRef.value, source.value.length)
    )

    watch(pageSizeRef, () => {
        currentPage.value = 1
    })

    function nextPage() {
        if (currentPage.value < totalPages.value) currentPage.value++
    }

    function prevPage() {
        if (currentPage.value > 1) currentPage.value--
    }

    return {
        currentPage,
        totalPages,
        paginatedData,
        visiblePages,
        startItem,
        endItem,
        nextPage,
        prevPage
    }
}