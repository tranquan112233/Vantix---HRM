function escapeHtml(value) {
  if (value == null) return ''
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function money(value) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0))
}

function display(value) {
  return value || '-'
}

function download(blob, filename) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

export function downloadBlob(response, fallbackName) {
  const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' })
  download(blob, fileNameFromDisposition(response.headers['content-disposition']) || fallbackName)
}

export function fileNameFromDisposition(disposition) {
  if (!disposition) return ''
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) return decodeURIComponent(utf8Match[1])
  const match = disposition.match(/filename="?([^"]+)"?/i)
  return match?.[1] || ''
}

export function contractFilename(contract, extension = 'doc') {
  const code = String(contract?.contractCode || 'contract').replace(/[\\/:*?"<>|]+/g, '-')
  const employee = String(contract?.employeeName || '').replace(/[\\/:*?"<>|]+/g, '-')
  return [code, employee].filter(Boolean).join('-') + `.${extension}`
}

export function buildContractHtml(contract, helpers = {}) {
  const t = helpers.t || ((key) => key)
  const typeLabel = helpers.typeLabel || ((value) => value || '-')
  const positionName = contract.positionName || helpers.positionName?.(contract.positionId) || '-'
  const employeeName = contract.employeeName || helpers.employeeName?.(contract.employeeId) || '-'
  const gross = contract.totalGrossSalary || Number(contract.baseSalary || 0) + Number(contract.totalAllowance || 0)

  return `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>${escapeHtml(contract.contractCode || t('contract.details'))}</title>
<style>
  @page { margin: 18mm; }
  body { font-family: "Times New Roman", serif; color: #111827; line-height: 1.45; }
  h1 { text-align: center; font-size: 20px; margin: 10px 0 4px; text-transform: uppercase; }
  h2 { font-size: 15px; margin: 20px 0 8px; text-transform: uppercase; }
  .center { text-align: center; }
  .muted { color: #4b5563; }
  table { width: 100%; border-collapse: collapse; margin: 8px 0 12px; }
  th, td { border: 1px solid #9ca3af; padding: 7px 9px; vertical-align: top; }
  th { width: 34%; text-align: left; background: #f3f4f6; }
  .clause { margin: 8px 0; }
  .signature-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 48px; margin-top: 42px; text-align: center; }
  .signature-name { margin-top: 86px; font-weight: 700; }
  @media print { .no-print { display: none; } }
</style>
</head>
<body>
  <div class="center">
    <strong>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</strong><br>
    <span>Độc lập - Tự do - Hạnh phúc</span>
  </div>

  <h1>Hợp đồng lao động</h1>
  <p class="center muted">Số: ${escapeHtml(display(contract.contractCode))}</p>

  <h2>1. Thông tin hợp đồng</h2>
  <table>
    <tr><th>${escapeHtml(t('contract.employee'))}</th><td>${escapeHtml(employeeName)}</td></tr>
    <tr><th>${escapeHtml(t('contract.position'))}</th><td>${escapeHtml(positionName)}</td></tr>
    <tr><th>${escapeHtml(t('contract.contractType'))}</th><td>${escapeHtml(typeLabel(contract.contractType))}</td></tr>
    <tr><th>${escapeHtml(t('contract.signedDate'))}</th><td>${escapeHtml(display(contract.signedDate))}</td></tr>
    <tr><th>${escapeHtml(t('contract.startDate'))}</th><td>${escapeHtml(display(contract.startDate))}</td></tr>
    <tr><th>${escapeHtml(t('contract.endDate'))}</th><td>${escapeHtml(display(contract.endDate))}</td></tr>
  </table>

  <h2>2. Công việc và thời gian làm việc</h2>
  <p class="clause">Người lao động đảm nhận vị trí ${escapeHtml(positionName)} theo phân công của công ty và các quy định nội bộ hiện hành.</p>
  <table>
    <tr><th>${escapeHtml(t('contract.standardWorkDays'))}</th><td>${escapeHtml(display(contract.standardWorkDays))}</td></tr>
    <tr><th>${escapeHtml(t('contract.hoursPerDay'))}</th><td>${escapeHtml(display(contract.hoursPerDay))}</td></tr>
    <tr><th>${escapeHtml(t('contract.probationMonths'))}</th><td>${escapeHtml(display(contract.probationMonths))}</td></tr>
    <tr><th>${escapeHtml(t('contract.noticePeriodDays'))}</th><td>${escapeHtml(display(contract.noticePeriodDays))}</td></tr>
  </table>

  <h2>3. Lương và phụ cấp</h2>
  <table>
    <tr><th>${escapeHtml(t('contract.baseSalary'))}</th><td>${money(contract.baseSalary)} VND</td></tr>
    <tr><th>${escapeHtml(t('contract.insuranceSalary'))}</th><td>${money(contract.insuranceSalary)} VND</td></tr>
    <tr><th>${escapeHtml(t('contract.totalAllowance'))}</th><td>${money(contract.totalAllowance)} VND</td></tr>
    <tr><th>${escapeHtml(t('contract.totalGrossSalary'))}</th><td>${money(gross)} VND</td></tr>
  </table>

  <h2>4. Điều khoản chung</h2>
  <p class="clause">Hai bên cam kết thực hiện đúng các nội dung trong hợp đồng, quy định của công ty và pháp luật lao động hiện hành.</p>
  <p class="clause">${escapeHtml(contract.note || '')}</p>

  <div class="signature-grid">
    <div>
      <strong>ĐẠI DIỆN CÔNG TY</strong>
      <div class="muted">(Ký, ghi rõ họ tên)</div>
      <div class="signature-name">&nbsp;</div>
    </div>
    <div>
      <strong>NGƯỜI LAO ĐỘNG</strong>
      <div class="muted">(Ký, ghi rõ họ tên)</div>
      <div class="signature-name">${escapeHtml(employeeName)}</div>
    </div>
  </div>
</body>
</html>`
}

export function exportContractDoc(contract, helpers) {
  const html = buildContractHtml(contract, helpers)
  const blob = new Blob(['\uFEFF' + html], { type: 'application/msword;charset=utf-8' })
  download(blob, contractFilename(contract, 'doc'))
}

export function printContract(contract, helpers) {
  const html = buildContractHtml(contract, helpers)
  const w = window.open('', '_blank')
  if (!w) return
  w.document.open()
  w.document.write(html)
  w.document.close()
  w.document.title = contractFilename(contract, 'pdf')
  w.addEventListener('load', () => { w.focus(); w.print() })
  setTimeout(() => { try { w.focus(); w.print() } catch {} }, 400)
}

