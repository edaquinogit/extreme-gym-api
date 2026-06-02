import { PieChart, Pie, ResponsiveContainer, Cell, Tooltip, Legend } from 'recharts'

const COLORS: Record<string, string> = {
  ATIVA: '#1a7a3c',
  PENDENTE: '#b7770d',
  CANCELADA: '#6b7280',
  EXPIRADA: '#c0392b',
  VENCIDA: '#c0392b',
}

export function MatriculasStatusChart({ data }: { data: Array<{ name: string; value: number }> }) {
  if (!data || data.length === 0) return null

  return (
    <ResponsiveContainer width="100%" height={220}>
      <PieChart>
        <Pie data={data} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={4}>
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

export default MatriculasStatusChart

function formatLabel(value: string) {
  return value.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, (char) => char.toUpperCase())
}
