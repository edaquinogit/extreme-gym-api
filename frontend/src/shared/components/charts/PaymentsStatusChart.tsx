import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts'

type DataItem = { name: string; value: number }

const COLORS: Record<string, string> = {
  PAGO: '#1a7a3c',
  PENDENTE: '#b7770d',
  CANCELADO: '#6b7280',
}

export function PaymentsStatusChart({ data }: { data: DataItem[] }) {
  if (!data || data.length === 0) return null

  return (
    <ResponsiveContainer width="100%" height={220}>
      <PieChart>
        <Pie data={data} dataKey="value" nameKey="name" innerRadius={52} outerRadius={82} paddingAngle={4}>
          {data.map((entry) => (
            <Cell key={entry.name} fill={COLORS[entry.name] ?? '#94a3b8'} />
          ))}
        </Pie>
        <Tooltip
          formatter={(value: number, name: string) => [value.toLocaleString('pt-BR'), formatLabel(name)]}
          contentStyle={{ borderRadius: 8, borderColor: '#dce3df' }}
        />
        <Legend verticalAlign="bottom" height={28} formatter={(value) => formatLabel(String(value))} />
      </PieChart>
    </ResponsiveContainer>
  )
}

export default PaymentsStatusChart

function formatLabel(value: string) {
  return value.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, (char) => char.toUpperCase())
}
