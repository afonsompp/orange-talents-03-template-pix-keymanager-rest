package br.com.itau.managekey

import br.com.zup.manage.pix.ManagePixServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class ManageKeyGrpcClient {
	@Singleton
	fun pixClient(@GrpcChannel("pix") channel: ManagedChannel): ManagePixServiceGrpc
	.ManagePixServiceBlockingStub {
		return ManagePixServiceGrpc.newBlockingStub(channel)
	}
}
