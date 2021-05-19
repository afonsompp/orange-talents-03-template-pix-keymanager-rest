package br.com.itau.managekey

import br.com.zup.manage.pix.ManagePixServiceGrpc
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.uri.UriBuilder
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.validation.Valid

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
}
