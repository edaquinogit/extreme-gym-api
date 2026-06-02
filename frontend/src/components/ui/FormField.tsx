import { Children, cloneElement, isValidElement, useId, type ReactNode } from 'react'

type FormFieldProps = {
  label: string
  error?: string
  hint?: string
  required?: boolean
  children: ReactNode
}

export function FormField({ label, error, hint, required = false, children }: FormFieldProps) {
  const generatedId = useId()
  const errorId = error ? `${generatedId}-error` : undefined
  const hintId = hint ? `${generatedId}-hint` : undefined
  const describedBy = [errorId, hintId].filter(Boolean).join(' ') || undefined
  const child = Children.only(children)
  const control = isValidElement<{ id?: string; 'aria-describedby'?: string; 'aria-required'?: boolean }>(child)
    ? cloneElement(child, {
        id: child.props.id ?? generatedId,
        'aria-describedby': [child.props['aria-describedby'], describedBy].filter(Boolean).join(' ') || undefined,
        'aria-required': required || undefined,
      })
    : children
  const labelFor = isValidElement<{ id?: string }>(child) ? child.props.id ?? generatedId : undefined

  return (
    <div className={`form-field ${error ? 'has-error' : ''}`}>
      <label htmlFor={labelFor}>
        {label}
        {required && <span aria-hidden="true"> *</span>}
      </label>
      <div className="form-field-control">{control}</div>
      {error && <p className="field-error" id={errorId}>{error}</p>}
      {hint && <p className="field-hint" id={hintId}>{hint}</p>}
    </div>
  )
}
