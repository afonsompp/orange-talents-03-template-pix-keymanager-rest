package br.com.itau.managekey

import br.com.itau.shered.extension.toRestAccountType
import br.com.zup.manage.pix.KeyDetailsResponse.AccountDetailsResponse
import br.com.zup.manage.pix.KeyType
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import br.com.zup.manage.pix.KeyDetailsResponse as GrpcResponse

class KeyResponse(
	val keyId: Long,
	val customerId: String,
	val key: String,
	val keyType: KeyType,
	val account: AccountResponse,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	val createdAt: LocalDateTime

) {
	companion object {
		fun of(response: GrpcResponse): KeyResponse = KeyResponse(
			response.keyId,
			response.customerId,
			response.key,
			response.keyType,
			AccountResponse.of(response.account),
			Instant.ofEpochSecond(response.createdAt.seconds, response.createdAt.nanos.toLong())
				.atZone(ZoneId.of("UTC")).toLocalDateTime()

		)
	}
}

class AccountResponse(
	val customerName: String,
	val customerCPF: String,
	val accountType: AccountType,
	val accountBranch: String,
	val accountNumber: String,
	val institution: String,
) {
	companion object {
		fun of(acc: AccountDetailsResponse) = AccountResponse(
			acc.customerName,
			acc.customerCPF,
			acc.accountType.toRestAccountType(),
			acc.branch,
			acc.number,
			acc.institution
		)
	}
}
