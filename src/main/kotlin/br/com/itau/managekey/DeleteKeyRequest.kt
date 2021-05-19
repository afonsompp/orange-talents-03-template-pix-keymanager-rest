package br.com.itau.managekey

import br.com.itau.shered.validation.ValidUUID
import br.com.zup.manage.pix.RemoveKeyRequest
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
class DeleteKeyRequest(
	@field:NotNull
	@field:Positive
	val keyId: Long,
	@field:NotBlank
	@field:ValidUUID
	val customerId: String
) {
	fun toRemoveKeyRequest(): RemoveKeyRequest = RemoveKeyRequest.newBuilder().setKeyId(keyId)
		.setCustomerId(customerId).build()
}
