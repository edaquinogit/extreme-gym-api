export type AccessDeviceStatus = 'ATIVO' | 'INATIVO' | 'MANUTENCAO' | 'OFFLINE'

export type AccessDeviceType =
  | 'CATRACA_FACIAL'
  | 'CATRACA_QR'
  | 'RECEPCAO'
  | 'GATEWAY'
  | 'OUTRO'

export type AccessDeviceOperationMode = 'ONLINE' | 'OFFLINE' | 'HIBRIDO'

export type AccessDevice = {
  id: number
  nome: string
  tipo: AccessDeviceType
  status: AccessDeviceStatus
  modoOperacao: AccessDeviceOperationMode
  identificadorExterno: string
  fabricante: string
  modelo: string
  ipLocal: string
  unidade: string
  ultimaComunicacaoEm: string | null
  criadoEm: string
  atualizadoEm: string
}

export type AccessDeviceCreatePayload = {
  nome: string
  tipo: AccessDeviceType
  modoOperacao: AccessDeviceOperationMode
  identificadorExterno?: string
  fabricante?: string
  modelo?: string
  ipLocal?: string
  unidade?: string
}

export type AccessDeviceCreated = {
  dispositivo: AccessDevice
  apiKeyPlaintext: string
}

export type DispositivoAcessoViewModel = AccessDevice
export type StatusDispositivoAcesso = AccessDeviceStatus
export type ModoOperacaoDispositivo = AccessDeviceOperationMode
