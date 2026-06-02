import type { ReactNode } from 'react'

type DataTableProps = {
  headers: string[]
  children: ReactNode
}

export function DataTable({ children, headers }: DataTableProps) {
  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            {headers.map((header) => (
              <th key={header}>{header}</th>
            ))}
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  )
}
