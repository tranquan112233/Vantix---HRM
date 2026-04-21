export const DEFAULT_PAGE_SIZE = 10

export function pageParams(pagination) {
  return {
    page: Math.max(pagination.page - 1, 0),
    size: pagination.size,
  }
}

export function applyPage(data, rowsRef, pagination) {
  if (Array.isArray(data)) {
    rowsRef.value = data
    pagination.total = data.length
    return
  }

  rowsRef.value = data?.content || []
  pagination.page = (data?.page ?? 0) + 1
  pagination.size = data?.size || pagination.size
  pagination.total = data?.totalElements || 0
}
