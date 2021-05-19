package br.com.itau.managekey

import br.com.itau.shered.validation.ValidKey
import br.com.zup.manage.pix.KeyType
import br.com.zup.manage.pix.RegisterKeyRequest
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidKey
data class RegisterNewKeyRequest(
	@field:Size(max = 77)
	val key: String,
	@field:NotNull
	val keyType: KeyType,
	@field:NotNull
	val accountType: AccountType,
	@field:NotBlank
	val customerId: String,
) {
	fun toGrpcRequest(): RegisterKeyRequest {
		return RegisterKeyRequest.newBuilder()
			.setValue(key)
			.setType(keyType)
			.setAccountType(accountType.grpcType)
			.setCustomerId(customerId)
			.build()
	}
}
