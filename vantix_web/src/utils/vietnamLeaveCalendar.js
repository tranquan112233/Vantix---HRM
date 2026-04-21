const DAY_MS = 24 * 60 * 60 * 1000
const VIETNAM_TIMEZONE = 7

export const WORK_CONDITIONS = [
  { value: 'NORMAL', baseDays: 12 },
  { value: 'HEAVY_OR_MINOR_OR_DISABLED', baseDays: 14 },
  { value: 'EXTREMELY_HEAVY', baseDays: 16 },
]

export const TET_PATTERNS = [
  { value: 'ONE_BEFORE_FOUR_AFTER', before: 1, after: 4 },
  { value: 'TWO_BEFORE_THREE_AFTER', before: 2, after: 3 },
  { value: 'ZERO_BEFORE_FIVE_AFTER', before: 0, after: 5 },
]

export const LEAVE_TYPES = [
  { value: 'ANNUAL', deductsAnnualLeave: true },
  { value: 'PAID_PERSONAL', deductsAnnualLeave: false },
  { value: 'UNPAID', deductsAnnualLeave: false },
  { value: 'SICK_OR_MATERNITY', deductsAnnualLeave: false },
]

export const LEAVE_STATUSES = [
  { value: 'APPROVED', type: 'success' },
  { value: 'PENDING', type: 'warning' },
  { value: 'REJECTED', type: 'danger' },
  { value: 'CANCELLED', type: 'info' },
]

function int(value) {
  return Math.floor(value)
}

function pad2(value) {
  return String(value).padStart(2, '0')
}

export function toISO(date) {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`
}

export function parseISO(dateText) {
  if (!dateText) {
    return null
  }

  const [year, month, day] = dateText.split('-').map(Number)
  if (!year || !month || !day) {
    return null
  }

  return new Date(year, month - 1, day)
}

export function formatISODate(dateText) {
  const date = parseISO(dateText)
  if (!date) {
    return '-'
  }

  return `${pad2(date.getDate())}/${pad2(date.getMonth() + 1)}/${date.getFullYear()}`
}

export function addDays(dateText, days) {
  const date = parseISO(dateText)
  date.setDate(date.getDate() + days)
  return toISO(date)
}

export function compareISO(a, b) {
  return a.localeCompare(b)
}

export function isDateInRange(dateText, startDate, endDate) {
  const start = startDate || endDate
  const end = endDate || startDate

  if (!dateText || !start || !end) {
    return false
  }

  const min = compareISO(start, end) <= 0 ? start : end
  const max = compareISO(start, end) <= 0 ? end : start
  return compareISO(dateText, min) >= 0 && compareISO(dateText, max) <= 0
}

function jdFromDate(day, month, year) {
  const a = int((14 - month) / 12)
  const y = year + 4800 - a
  const m = month + 12 * a - 3
  let jd = day + int((153 * m + 2) / 5) + 365 * y + int(y / 4) - int(y / 100) + int(y / 400) - 32045

  if (jd < 2299161) {
    jd = day + int((153 * m + 2) / 5) + 365 * y + int(y / 4) - 32083
  }

  return jd
}

function jdToDate(jd) {
  let a
  let b
  let c

  if (jd > 2299160) {
    a = jd + 32044
    b = int((4 * a + 3) / 146097)
    c = a - int((b * 146097) / 4)
  } else {
    b = 0
    c = jd + 32082
  }

  const d = int((4 * c + 3) / 1461)
  const e = c - int((1461 * d) / 4)
  const m = int((5 * e + 2) / 153)
  const day = e - int((153 * m + 2) / 5) + 1
  const month = m + 3 - 12 * int(m / 10)
  const year = b * 100 + d - 4800 + int(m / 10)

  return [day, month, year]
}

function newMoon(k) {
  const t = k / 1236.85
  const t2 = t * t
  const t3 = t2 * t
  const dr = Math.PI / 180
  let jd = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
  jd += 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)

  const m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
  const mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
  const f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
  let c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr) + 0.0021 * Math.sin(2 * dr * m)
  c1 -= 0.4068 * Math.sin(mpr * dr) + 0.0161 * Math.sin(2 * dr * mpr)
  c1 -= 0.0004 * Math.sin(3 * dr * mpr)
  c1 += 0.0104 * Math.sin(2 * dr * f) - 0.0051 * Math.sin((m + mpr) * dr)
  c1 -= 0.0074 * Math.sin((m - mpr) * dr) + 0.0004 * Math.sin((2 * f + m) * dr)
  c1 -= 0.0004 * Math.sin((2 * f - m) * dr) - 0.0006 * Math.sin((2 * f + mpr) * dr)
  c1 += 0.001 * Math.sin((2 * f - mpr) * dr) + 0.0005 * Math.sin((2 * mpr + m) * dr)

  let deltaT
  if (t < -11) {
    deltaT = 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
  } else {
    deltaT = -0.000278 + 0.000265 * t + 0.000262 * t2
  }

  return jd + c1 - deltaT
}

function sunLongitude(jdn) {
  const t = (jdn - 2451545.0) / 36525
  const t2 = t * t
  const dr = Math.PI / 180
  const m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
  const l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
  let dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m)
  dl += (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m) + 0.000290 * Math.sin(dr * 3 * m)
  let l = l0 + dl
  l *= dr
  l -= Math.PI * 2 * int(l / (Math.PI * 2))

  return l
}

function getNewMoonDay(k, timeZone) {
  return int(newMoon(k) + 0.5 + timeZone / 24)
}

function getSunLongitude(dayNumber, timeZone) {
  return int(sunLongitude(dayNumber - 0.5 - timeZone / 24) / Math.PI * 6)
}

function getLunarMonth11(year, timeZone) {
  const off = jdFromDate(31, 12, year) - 2415021
  const k = int(off / 29.530588853)
  let nm = getNewMoonDay(k, timeZone)
  const sunLong = getSunLongitude(nm, timeZone)

  if (sunLong >= 9) {
    nm = getNewMoonDay(k - 1, timeZone)
  }

  return nm
}

function getLeapMonthOffset(a11, timeZone) {
  const k = int((a11 - 2415021.076998695) / 29.530588853 + 0.5)
  let last = 0
  let i = 1
  let arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)

  do {
    last = arc
    i += 1
    arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
  } while (arc !== last && i < 14)

  return i - 1
}

export function convertLunarToSolar(lunarDay, lunarMonth, lunarYear, lunarLeap = false, timeZone = VIETNAM_TIMEZONE) {
  let a11 = getLunarMonth11(lunarYear - 1, timeZone)
  let b11 = getLunarMonth11(lunarYear, timeZone)
  let off = lunarMonth - 11

  if (off < 0) {
    off += 12
  }

  if (b11 - a11 > 365) {
    const leapOff = getLeapMonthOffset(a11, timeZone)
    const leapMonth = leapOff - 2 < 0 ? leapOff + 10 : leapOff - 2

    if (lunarLeap && lunarMonth !== leapMonth) {
      return null
    }

    if (lunarLeap || off >= leapOff) {
      off += 1
    }
  }

  const k = int(0.5 + (a11 - 2415021.076998695) / 29.530588853)
  const monthStart = getNewMoonDay(k + off, timeZone)
  return jdToDate(monthStart + lunarDay - 1)
}

function lunarDateISO(day, month, year) {
  const converted = convertLunarToSolar(day, month, year)
  if (!converted) {
    return ''
  }

  return `${converted[2]}-${pad2(converted[1])}-${pad2(converted[0])}`
}

function getTetOffsets(patternValue) {
  const pattern = TET_PATTERNS.find(item => item.value === patternValue) || TET_PATTERNS[0]
  const start = -pattern.before
  return Array.from({ length: pattern.before + pattern.after }, (_, index) => start + index)
}

function tetDayName(offset) {
  if (offset < 0) {
    return 'Tết Âm lịch'
  }

  return `Tết Nguyên đán mùng ${offset + 1}`
}

function makeHoliday(date, name, groupCode, groupName, type = 'PUBLIC') {
  return {
    date,
    name,
    groupCode,
    groupName,
    type,
  }
}

export function getVietnamHolidayGroups(year, options = {}) {
  const tetPattern = options.tetPattern || TET_PATTERNS[0].value
  const nationalDayAdjacent = options.nationalDayAdjacent || 'BEFORE'
  const tetDate = lunarDateISO(1, 1, year)
  const hungKingsDate = lunarDateISO(10, 3, year)
  const nationalAdjacentDate = nationalDayAdjacent === 'AFTER'
    ? `${year}-09-03`
    : `${year}-09-01`

  const groups = [
    {
      code: 'NEW_YEAR',
      name: 'Tết Dương lịch',
      holidays: [makeHoliday(`${year}-01-01`, 'Tết Dương lịch', 'NEW_YEAR', 'Tết Dương lịch')],
    },
    {
      code: 'TET',
      name: 'Tết Âm lịch',
      holidays: getTetOffsets(tetPattern).map(offset => (
        makeHoliday(addDays(tetDate, offset), tetDayName(offset), 'TET', 'Tết Âm lịch')
      )),
    },
    {
      code: 'HUNG_KINGS',
      name: 'Giỗ Tổ Hùng Vương',
      holidays: [makeHoliday(hungKingsDate, 'Giỗ Tổ Hùng Vương', 'HUNG_KINGS', 'Giỗ Tổ Hùng Vương')],
    },
    {
      code: 'APRIL_MAY',
      name: 'Ngày Chiến thắng và Quốc tế lao động',
      holidays: [
        makeHoliday(`${year}-04-30`, 'Ngày Chiến thắng', 'APRIL_MAY', 'Ngày Chiến thắng và Quốc tế lao động'),
        makeHoliday(`${year}-05-01`, 'Ngày Quốc tế lao động', 'APRIL_MAY', 'Ngày Chiến thắng và Quốc tế lao động'),
      ],
    },
    {
      code: 'NATIONAL_DAY',
      name: 'Quốc khánh',
      holidays: [
        makeHoliday(nationalAdjacentDate, 'Ngày liền kề Quốc khánh', 'NATIONAL_DAY', 'Quốc khánh'),
        makeHoliday(`${year}-09-02`, 'Quốc khánh', 'NATIONAL_DAY', 'Quốc khánh'),
      ].sort((a, b) => compareISO(a.date, b.date)),
    },
  ]

  return groups.map(group => ({
    ...group,
    holidays: group.holidays.sort((a, b) => compareISO(a.date, b.date)),
  }))
}

export function isWeeklyDayOff(dateText, workweek = 'FIVE_DAYS') {
  const date = parseISO(dateText)
  const day = date?.getDay()

  if (workweek === 'SIX_DAYS') {
    return day === 0
  }

  return day === 0 || day === 6
}

function compensationHolidaysForGroup(group, workweek, occupiedDates) {
  const overlapCount = group.holidays.filter(holiday => isWeeklyDayOff(holiday.date, workweek)).length
  const compensations = []

  if (!overlapCount) {
    return compensations
  }

  let candidate = addDays(group.holidays[group.holidays.length - 1].date, 1)
  while (compensations.length < overlapCount) {
    if (!isWeeklyDayOff(candidate, workweek) && !occupiedDates.has(candidate)) {
      compensations.push(makeHoliday(
        candidate,
        `Nghỉ bù ${group.name}`,
        group.code,
        group.name,
        'COMPENSATORY'
      ))
      occupiedDates.add(candidate)
    }
    candidate = addDays(candidate, 1)
  }

  return compensations
}

export function getVietnamPublicHolidays(year, options = {}) {
  const workweek = options.workweek || 'FIVE_DAYS'
  const groups = getVietnamHolidayGroups(year, options)
  const occupiedDates = new Set(groups.flatMap(group => group.holidays.map(holiday => holiday.date)))
  const compensations = groups.flatMap(group => compensationHolidaysForGroup(group, workweek, occupiedDates))

  return groups
    .flatMap(group => group.holidays)
    .concat(compensations)
    .sort((a, b) => compareISO(a.date, b.date))
}

export function getHolidayMap(year, options = {}) {
  return getVietnamPublicHolidays(year, options).reduce((map, holiday) => {
    if (!map.has(holiday.date)) {
      map.set(holiday.date, [])
    }
    map.get(holiday.date).push(holiday)
    return map
  }, new Map())
}

export function isWorkingDay(dateText, workweek, holidayMap) {
  return !isWeeklyDayOff(dateText, workweek) && !(holidayMap?.has(dateText))
}

export function countWorkingDaysInRange(startDate, endDate, context) {
  if (!startDate || !endDate) {
    return 0
  }

  const min = compareISO(startDate, endDate) <= 0 ? startDate : endDate
  const max = compareISO(startDate, endDate) <= 0 ? endDate : startDate
  let cursor = min
  let count = 0

  while (compareISO(cursor, max) <= 0) {
    if (
      (!context.year || cursor.startsWith(`${context.year}-`)) &&
      isWorkingDay(cursor, context.workweek, context.holidayMap)
    ) {
      count += 1
    }
    cursor = addDays(cursor, 1)
  }

  return count
}

export function countLeaveWorkingDays(leave, context) {
  const fullDayCount = countWorkingDaysInRange(leave.startDate, leave.endDate, context)

  if (leave.dayUnit === 'HALF' && leave.startDate === leave.endDate && fullDayCount === 1) {
    return 0.5
  }

  return fullDayCount
}

export function completeYearsBetween(startDate, endDate) {
  const start = parseISO(startDate)
  const end = parseISO(endDate)

  if (!start || !end || start > end) {
    return 0
  }

  let years = end.getFullYear() - start.getFullYear()
  const anniversary = new Date(end.getFullYear(), start.getMonth(), start.getDate())

  if (end < anniversary) {
    years -= 1
  }

  return Math.max(0, years)
}

function daysBetweenInclusive(start, end) {
  return Math.floor((end.getTime() - start.getTime()) / DAY_MS) + 1
}

export function estimateWorkedMonthsInYear(year, hireDate, terminationDate) {
  const yearStart = new Date(year, 0, 1)
  const yearEnd = new Date(year, 11, 31)
  const hire = parseISO(hireDate) || yearStart
  const termination = parseISO(terminationDate) || yearEnd
  const rangeStart = hire > yearStart ? hire : yearStart
  const rangeEnd = termination < yearEnd ? termination : yearEnd

  if (rangeStart > rangeEnd) {
    return 0
  }

  let months = 0

  for (let month = 0; month < 12; month += 1) {
    const monthStart = new Date(year, month, 1)
    const monthEnd = new Date(year, month + 1, 0)
    const actualStart = rangeStart > monthStart ? rangeStart : monthStart
    const actualEnd = rangeEnd < monthEnd ? rangeEnd : monthEnd

    if (actualStart > actualEnd) {
      continue
    }

    const workedDays = daysBetweenInclusive(actualStart, actualEnd)
    const monthDays = daysBetweenInclusive(monthStart, monthEnd)

    if (workedDays >= monthDays / 2) {
      months += 1
    }
  }

  return months
}

export function calculateAnnualLeaveEntitlement({ baseDays, serviceYears, workedMonths }) {
  const seniorityDays = Math.floor(Math.max(0, Number(serviceYears) || 0) / 5)
  const months = Math.min(12, Math.max(0, Number(workedMonths) || 0))
  const annualBase = (Number(baseDays) || 0) + seniorityDays

  return {
    baseDays: Number(baseDays) || 0,
    seniorityDays,
    annualBase,
    workedMonths: months,
    entitlement: Math.round((annualBase / 12) * months * 100) / 100,
  }
}

export function buildMonthCells(year, month) {
  const firstDay = new Date(year, month - 1, 1)
  const offsetFromMonday = (firstDay.getDay() + 6) % 7
  const start = new Date(year, month - 1, 1 - offsetFromMonday)

  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    return {
      date: toISO(date),
      day: date.getDate(),
      month: date.getMonth() + 1,
      inMonth: date.getMonth() + 1 === month,
      weekday: date.getDay(),
    }
  })
}
