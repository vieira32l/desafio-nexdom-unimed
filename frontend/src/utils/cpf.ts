// Mesmo algoritmo de dígito verificador usado no backend (CPFValidator).
export function isValidCPF(value: string): boolean {
  const digits = value.replace(/\D/g, '')

  if (digits.length !== 11 || allDigitsEqual(digits)) {
    return false
  }

  const firstCheckDigit = calculateCheckDigit(digits.slice(0, 9))
  const secondCheckDigit = calculateCheckDigit(digits.slice(0, 9) + firstCheckDigit)

  return Number(digits[9]) === firstCheckDigit && Number(digits[10]) === secondCheckDigit
}

function allDigitsEqual(digits: string): boolean {
  return new Set(digits.split('')).size === 1
}

function calculateCheckDigit(base: string): number {
  let weight = base.length + 1
  let sum = 0
  for (const char of base) {
    sum += Number(char) * weight
    weight -= 1
  }
  const remainder = sum % 11
  return remainder < 2 ? 0 : 11 - remainder
}

export function formatCPF(cpf: string): string {
  const digits = cpf.replace(/\D/g, '')
  if (digits.length !== 11) return cpf
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`
}
