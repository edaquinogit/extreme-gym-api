import { BarChart, Bar, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

export function CheckinsLast7DaysChart({ data }: { data: Array<{ day: string; count: number }> }) {
  if (!data || data.length === 0) return null

  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={data} margin={{ top: 10, right: 8, left: -16, bottom: 8 }}>
        <CartesianGrid stroke="#e7ece9" vertical={false} />
        <XAxis dataKey="day" tick={{ fontSize: 12, fill: '#6b7c73' }} axisLine={false} tickLine={false} />
        <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: '#6b7c73' }} axisLine={false} tickLine={false} />
        <Tooltip
          formatter={(value: number) => [value.toLocaleString('pt-BR'), 'Check-ins']}
          contentStyle={{ borderRadius: 8, borderColor: '#dce3df' }}
        />
        <Bar dataKey="count" fill="#1a7a3c" radius={[6, 6, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}

export default CheckinsLast7DaysChart
