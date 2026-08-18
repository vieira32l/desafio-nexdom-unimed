-- Reforça no banco a mesma regra já validada em BeneficiarioService.validarVinculoAtivoUnico():
-- a mesma pessoa não pode ter dois vínculos ATIVO do mesmo tipo (TITULAR/DEPENDENTE) no
-- mesmo tenant. A validação na aplicação sozinha deixa uma janela de condição de corrida
-- (duas requisições concorrentes podem passar as duas pela validação antes de qualquer
-- uma commitar) — este índice é a garantia de verdade, no nível do banco.
--
-- Índice único PARCIAL (WHERE status = 'ATIVO'): não se aplica a registros INATIVO, então
-- a mesma pessoa pode ter, por exemplo, um TITULAR ativo hoje e vários TITULAR inativos
-- (histórico) no mesmo tenant sem violar nada — só nunca dois ATIVO do mesmo tipo ao mesmo
-- tempo. Uma violação aqui cai no DataIntegrityViolationException já tratado de forma
-- genérica pelo ApiExceptionHandler, retornando 409 — nenhuma mudança necessária nele.
CREATE UNIQUE INDEX uk_beneficiario_pessoa_tenant_tipo_ativo
    ON beneficiario (pessoa_id, tenant_id, tipo)
    WHERE status = 'ATIVO';
