export const PRECISION_OPTIONS = [
  { label: '1 秒', value: 1000 },
  { label: '10 秒', value: 10000 },
  { label: '半分钟', value: 30000 },
  { label: '1 分钟', value: 60000 },
  { label: '10 分钟', value: 600000 },
  { label: '半小时', value: 1800000 },
  { label: '1 小时', value: 3600000 },
]

export function fmtTime(ms) {
  if (!ms && ms !== 0) return '-'
  const d = new Date(Number(ms))
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(
    d.getMinutes()
  )}:${p(d.getSeconds())}`
}

export function fmtGranularity(ms) {
  const hit = PRECISION_OPTIONS.find((o) => o.value === Number(ms))
  return hit ? hit.label : `${ms} ms`
}


export const MULTIPLE_OPTIONS = [
  { label: "1 倍（最细）", value: 1 },
  { label: "2 倍", value: 2 },
  { label: "5 倍", value: 5 },
  { label: "10 倍", value: 10 },
  { label: "30 倍", value: 30 },
  { label: "60 倍", value: 60 },
]
