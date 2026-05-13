// Zero-dep export helpers: Copy, CSV, Excel (.xls via HTML), PDF (print), Print
// columns: [{ prop, label, format?(row) }]

function escapeCsv(value) {
  if (value == null) return ''
  const str = String(value)
  if (/[",\n\r]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`
  }
  return str
}

function escapeHtml(value) {
  if (value == null) return ''
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function cellValue(row, col) {
  if (typeof col.format === 'function') return col.format(row)
  return row?.[col.prop] ?? ''
}

function rowsToMatrix(rows, columns) {
  return rows.map(row => columns.map(col => cellValue(row, col)))
}

function download(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

function buildPrintableHtml({ title, columns, rows }) {
  const th = columns.map(c => `<th>${escapeHtml(c.label)}</th>`).join('')
  const trs = rowsToMatrix(rows, columns)
    .map(r => `<tr>${r.map(v => `<td>${escapeHtml(v)}</td>`).join('')}</tr>`)
    .join('')
  return `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>${escapeHtml(title || 'Report')}</title>
<style>
  @page { margin: 16mm; }
  body { font-family: Arial, sans-serif; color: #111; padding: 16px; }
  h1 { font-size: 18px; margin: 0 0 12px; }
  .meta { font-size: 11px; color: #666; margin-bottom: 12px; }
  table { width: 100%; border-collapse: collapse; font-size: 12px; }
  thead th { background: #F1F5F9; text-align: left; font-weight: 600; }
  th, td { border: 1px solid #CBD5E1; padding: 6px 8px; vertical-align: top; }
  tbody tr:nth-child(even) { background: #F8FAFC; }
  @media print { .no-print { display: none } }
</style>
</head>
<body>
  ${title ? `<h1>${escapeHtml(title)}</h1>` : ''}
  <div class="meta">${new Date().toLocaleString()}</div>
  <table>
    <thead><tr>${th}</tr></thead>
    <tbody>${trs}</tbody>
  </table>
</body>
</html>`
}

export function exportCsv({ filename, columns, rows }) {
  const header = columns.map(c => escapeCsv(c.label)).join(',')
  const body = rowsToMatrix(rows, columns)
    .map(r => r.map(escapeCsv).join(','))
    .join('\n')
  const bom = '\uFEFF'
  const blob = new Blob([bom + header + '\n' + body], { type: 'text/csv;charset=utf-8' })
  download(blob, `${filename}.csv`)
}

export function exportXls({ filename, title, columns, rows }) {
  const th = columns.map(c => `<th>${escapeHtml(c.label)}</th>`).join('')
  const trs = rowsToMatrix(rows, columns)
    .map(r => `<tr>${r.map(v => `<td>${escapeHtml(v)}</td>`).join('')}</tr>`)
    .join('')
  const html = `<!DOCTYPE html><html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><meta charset="utf-8"><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>${escapeHtml(title || 'Sheet1')}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head><body><table border="1"><thead><tr>${th}</tr></thead><tbody>${trs}</tbody></table></body></html>`
  const blob = new Blob(['\uFEFF' + html], { type: 'application/vnd.ms-excel' })
  download(blob, `${filename}.xls`)
}

export async function copyToClipboard({ columns, rows }) {
  const header = columns.map(c => c.label).join('\t')
  const body = rowsToMatrix(rows, columns)
    .map(r => r.map(v => String(v ?? '').replace(/[\t\r\n]/g, ' ')).join('\t'))
    .join('\n')
  const text = header + '\n' + body
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text)
    return
  }
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.select()
  document.execCommand('copy')
  ta.remove()
}

export function printTable({ title, columns, rows }) {
  openPrintWindow({ title, columns, rows, autoPrint: true, filename: title })
}

export function exportPdf({ filename, title, columns, rows }) {
  openPrintWindow({ title: title || filename, columns, rows, autoPrint: true, filename })
}

function openPrintWindow({ title, columns, rows, autoPrint, filename }) {
  const html = buildPrintableHtml({ title, columns, rows })
  const w = window.open('', '_blank')
  if (!w) return
  w.document.open()
  w.document.write(html)
  w.document.close()
  if (filename) w.document.title = filename
  if (autoPrint) {
    w.addEventListener('load', () => { w.focus(); w.print() })
    setTimeout(() => { try { w.focus(); w.print() } catch {} }, 400)
  }
}
