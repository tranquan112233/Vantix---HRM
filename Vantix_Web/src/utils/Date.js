/**
 * Định dạng ngày giờ theo dd/MM/yyyy HH:mm
 * @param {string|Date|null} value
 * @returns {string}
 */
export function formatDate(value) {
    if (!value) return 'Not active'
    const d = new Date(value)
    if (isNaN(d)) return 'Invalid date'
    const pad = (n) => n.toString().padStart(2, '0')
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * Định dạng ngày theo dd/MM/yyyy
 */
export function formatDateOnly(value) {
    if (!value) return '—'
    const d = new Date(value)
    if (isNaN(d)) return 'Invalid date'
    const pad = (n) => n.toString().padStart(2, '0')
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`
}