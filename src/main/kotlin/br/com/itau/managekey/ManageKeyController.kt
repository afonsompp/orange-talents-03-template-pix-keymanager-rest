package br.com.itau.managekey

import br.com.itau.shered.validation.ValidUUID
import br.com.zup.manage.pix.KeyDetailsRequest
import br.com.zup.manage.pix.ListOfKeysRequest
import br.com.zup.manage.pix.ManagePixServiceGrpc
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.uri.UriBuilder
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Controller("/api/key")
@Validated
class ManageKeyController(@Inject val grpcClient: ManagePixServiceGrpc.ManagePixServiceBlockingStub) {

	@Post
	fun registerKey(@Valid @Body request: RegisterNewKeyRequest):
			HttpResponse<RegisterNewKeyResponse> {
		val response = grpcClient.registerKey(request.toGrpcRequest())
		val uri = UriBuilder.of("/key/{key}").expand(mutableMapOf(Pair("key", response.key)))
		return HttpResponse.created(RegisterNewKeyResponse(response.keyId, response.key), uri)
	}

	@Delete
	fun removeKey(@Valid @Body request: DeleteKeyRequest): HttpResponse<Any> {
		grpcClient.removeKey(request.toRemoveKeyRequest())
		return HttpResponse.noContent()
	}

	@Get("/{key}")
	fun findByKeyValue(@Size(max = 77) @NotBlank key: String): HttpResponse<KeyResponse> {
		val response = grpcClient.findKey(KeyDetailsRequest.newBuilder().setKey(key).build())

		return HttpResponse.ok(KeyResponse.of(response))
	}

	@Get("/{customerId}/{keyId}")
	fun findByKeyIdAndCustomerId(@ValidUUID @NotBlank customerId: String, @NotNull keyId: Long):
			HttpResponse<KeyResponse> {
		val response = grpcClient.findKey(
			KeyDetailsRequest.newBuilder().setPixId(
				KeyDetailsRequest.PixKey.newBuilder()
					.setKeyId(keyId)
					.setCustomerId(customerId)
					.build()
			).build()
		)

		return HttpResponse.ok(KeyResponse.of(response))
	}

	@Get("/{customerId}/all")
	fun listAllKeysByCustomerI(@ValidUUID @NotBlank customerId: String): HttpResponse<List<KeyResponse>> {
		val response = grpcClient.listKeysOfCustomer(
			ListOfKeysRequest.newBuilder()
				.setCustomerId(customerId).build()
		)

		response.keyList.map(KeyResponse::of)
		return HttpResponse.ok(response.keyList.map(KeyResponse::of))
	}
}
