import { computed } from "vue"

export function useSearch(source, searchRef) {
    const filteredData = computed(() => {
        if (!searchRef.value) return source.value

        return source.value.filter(item =>
            Object.values(item)
                .join(" ")
                .toLowerCase()
                .includes(searchRef.value.toLowerCase())
        )
    })

    return {
        filteredData
    }
}