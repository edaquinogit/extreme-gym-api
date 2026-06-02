import { LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

export function RevenueByMonthChart({ data }: { data: Array<{ month: string; value: number }> }) {
  if (!data || data.length === 0) return null

  return (
    <ResponsiveContainer width="100%" height={220}>
      <LineChart data={data} margin={{ top: 10, right: 12, left: 0, bottom: 8 }}>
        <CartesianGrid stroke="#e7ece9" vertical={false} />
        <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#6b7c73' }} axisLine={false} tickLine={false} />
        <YAxis tick={{ fontSize: 12, fill: '#6b7c73' }} axisLine={false} tickLine={false} tickFormatter={(v) => formatCurrencyShort(Number(v))} />
        <Tooltip formatter={(value: number) => value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} contentStyle={{ borderRadius: 8, borderColor: '#dce3df' }} />
        <Line type="monotone" dataKey="value" stroke="#1a7a3c" strokeWidth={3} dot={{ r: 3 }} activeDot={{ r: 5 }} />
      </LineChart>
    </ResponsiveContainer>
  )
}

export default RevenueByMonthChart

function formatCurrencyShort(value: number) {
  if (value >= 1000) {
    return `${Math.round(value / 1000)} mil`
  }

  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 })
}
