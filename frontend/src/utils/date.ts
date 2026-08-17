// Converte "yyyy-MM-dd" (formato de LocalDate do backend) para "dd/mm/aaaa".
export function formatDate(date: string | null): string {
  if (!date) return '—'
  const [ano, mes, dia] = date.split('-')
  return `${dia}/${mes}/${ano}`
}
