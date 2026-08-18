import { ref } from 'vue'

// Mensagem de sucesso que desaparece sozinha depois de alguns segundos — usado como
// feedback visual após criar/editar/excluir (ver PessoasView e BeneficiariosView).
export function useSuccessMessage(timeoutMs = 6000) {
  const successMessage = ref<string | null>(null)
  let timer: ReturnType<typeof setTimeout> | undefined

  function mostrarSucesso(mensagem: string) {
    successMessage.value = mensagem
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      successMessage.value = null
    }, timeoutMs)
  }

  return { successMessage, mostrarSucesso }
}
