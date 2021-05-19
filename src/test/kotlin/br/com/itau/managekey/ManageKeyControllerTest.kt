package br.com.itau.managekey

import br.com.zup.manage.pix.*
import io.grpc.Status.*
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.*
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import org.mockito.Mockito.`when` as case

@MicronautTest
internal class ManageKeyControllerTest {
	@field:Inject
	@field:Client("/")
	lateinit var client: HttpClient

	@field:Inject
	lateinit var grpcClient: ManagePixServiceGrpc.ManagePixServiceBlockingStub

	@Test
	fun `Should register key and return key and key id with status 201`() {
		val id = (Math.random() * 100).toLong()
		val body = RegisterNewKeyRequest(
			"02654220273", KeyType.CPF, AccountType.CURRENT_ACCOUNT, UUID.randomUUID().toString()
		)
		val grpcResponse = RegisterKeyResponse.newBuilder().setKey(body.key).setKeyId(id).build()
		case(grpcClient.registerKey(any(RegisterKeyRequest::class.java))).thenReturn(grpcResponse)

		val request = HttpRequest.POST("/api/key", body)

		val response =
			client.toBlocking().exchange(request, RegisterNewKeyResponse::class.java)

		assertEquals(response.status, CREATED)
		assertEquals(response.body()!!.id, id)
		assertEquals(response.body()!!.key, body.key)
	}

	@Test
	fun `should return validation error with status 400`() {
		val body = RegisterNewKeyRequest(
			"123", KeyType.CPF, AccountType.CURRENT_ACCOUNT, UUID.randomUUID().toString()
		)
		val request = HttpRequest.POST("/api/key", body)

		val e = assertThrows<HttpClientResponseException> {
			client.toBlocking().exchange(request, RegisterNewKeyResponse::class.java)
		}

		assertEquals(BAD_REQUEST, e.status)
		assertEquals("request: The CPF is invalid", e.message)
	}

	@Test
	fun `should return key already exists error with status 422`() {
		val body = RegisterNewKeyRequest(
			"02654220273", KeyType.CPF, AccountType.CURRENT_ACCOUNT, UUID.randomUUID().toString()
		)

		case(grpcClient.registerKey(any(RegisterKeyRequest::class.java)))
			.thenThrow(StatusRuntimeException(ALREADY_EXISTS.withDescription("Key already exists")))

		val request = HttpRequest.POST("/api/key", body)

		val e = assertThrows<HttpClientResponseException> {
			client.toBlocking().exchange(request, RegisterNewKeyResponse::class.java)
		}

		assertEquals(UNPROCESSABLE_ENTITY, e.status)
		assertEquals("Key already exists", e.message)
	}

	@Test
	fun `should return customer not found error with status 404`() {
		val body = RegisterNewKeyRequest(
			"02654220273", KeyType.CPF, AccountType.CURRENT_ACCOUNT, UUID.randomUUID().toString()
		)

		case(grpcClient.registerKey(any(RegisterKeyRequest::class.java)))
			.thenThrow(StatusRuntimeException(NOT_FOUND.withDescription("Customer not found")))

		val request = HttpRequest.POST("/api/key", body)

		val e = assertThrows<HttpClientResponseException> {
			client.toBlocking().exchange(request, RegisterNewKeyResponse::class.java)
		}

		assertEquals(HttpStatus.NOT_FOUND, e.status)
		assertEquals("Customer not found", e.message)
	}
}

@Factory
@Replaces(factory = ManageKeyGrpcClient::class)
internal class MockGrpcClient {
	@Singleton
	fun grpcClient() = mock(ManagePixServiceGrpc.ManagePixServiceBlockingStub::class.java)
}
