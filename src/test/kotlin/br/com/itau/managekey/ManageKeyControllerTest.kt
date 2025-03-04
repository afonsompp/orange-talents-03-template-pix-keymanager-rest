package br.com.itau.managekey

import br.com.zup.manage.pix.*
import br.com.zup.manage.pix.AccountType.CONTA_CORRENTE
import com.google.protobuf.Timestamp
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
import java.time.Instant
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
	fun `should return customer not found error with status 404 in register key operation`() {
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

	@Test
	fun `should delete key and return status 204`() {
		val body = DeleteKeyRequest(1, UUID.randomUUID().toString())

		val grpcResponse = RemoveKeyResponse.newBuilder().setMessage("Success").build()
		case(grpcClient.removeKey(any(RemoveKeyRequest::class.java))).thenReturn(grpcResponse)

		val request = HttpRequest.DELETE("/api/key", body)

		val response = client.toBlocking().exchange(request, Any::class.java)

		assertEquals(NO_CONTENT, response.status)
	}

	@Test
	fun `should return customer not found error with status 404 in delete key operation`() {
		val body = DeleteKeyRequest(1, UUID.randomUUID().toString())

		case(grpcClient.removeKey(any(RemoveKeyRequest::class.java)))
			.thenThrow(StatusRuntimeException(NOT_FOUND.withDescription("Customer not found")))

		val request = HttpRequest.DELETE("/api/key", body)

		val e = assertThrows<HttpClientResponseException> {
			client.toBlocking().exchange(request, RegisterNewKeyResponse::class.java)
		}

		assertEquals(HttpStatus.NOT_FOUND, e.status)
		assertEquals("Customer not found", e.message)
	}

	@Test
	fun `Should return details of a key if it exists`() {
		val grpcResponse = newKeyDetailsResponse().build()
		case(grpcClient.findKey(any(KeyDetailsRequest::class.java))).thenReturn(grpcResponse)

		val response =
			client.toBlocking().exchange("/api/key/${grpcResponse.key}", KeyResponse::class.java)

		assertEquals(HttpStatus.OK, response.status)
		with(grpcResponse) expected@{
			with(response.body()!!) {
				assertEquals(this@expected.keyId, keyId)
				assertEquals(this@expected.key, key)
				assertEquals(this@expected.keyType, keyType)
				assertEquals(this@expected.customerId, customerId)
			}
			with(response.body()!!.account) {
				assertEquals(account.customerCPF, customerCPF)
				assertEquals(account.customerName, customerName)
				assertEquals(account.accountType, accountType.grpcType)
				assertEquals(account.branch, accountBranch)
				assertEquals(account.number, accountNumber)
				assertEquals(account.institution, institution)
			}
		}
	}

	@Test
	fun `Should return details of a key if it exists when find by customer and key id`() {
		val grpcResponse = newKeyDetailsResponse().build()
		case(grpcClient.findKey(any(KeyDetailsRequest::class.java))).thenReturn(grpcResponse)

		val response = client.toBlocking()
			.exchange(
				"/api/key/${grpcResponse.customerId}/${grpcResponse.keyId}",
				KeyResponse::class.java
			)

		assertEquals(HttpStatus.OK, response.status)
		with(grpcResponse) expected@{
			with(response.body()!!) {
				assertEquals(this@expected.keyId, keyId)
				assertEquals(this@expected.key, key)
				assertEquals(this@expected.keyType, keyType)
				assertEquals(this@expected.customerId, customerId)
			}
			with(response.body()!!.account) {
				assertEquals(account.customerCPF, customerCPF)
				assertEquals(account.customerName, customerName)
				assertEquals(account.accountType, accountType.grpcType)
				assertEquals(account.branch, accountBranch)
				assertEquals(account.number, accountNumber)
				assertEquals(account.institution, institution)
			}
		}
	}

	@Test
	fun `should return not found error with status 404`() {
		case(grpcClient.findKey(any(KeyDetailsRequest::class.java)))
			.thenThrow(StatusRuntimeException(NOT_FOUND.withDescription("Key not found")))

		val e = assertThrows<HttpClientResponseException> {
			client.toBlocking().exchange("/api/key/1245", KeyResponse::class.java)
		}

		assertEquals(HttpStatus.NOT_FOUND, e.status)
		assertEquals("Key not found", e.message)
	}

	@Test
	fun `should return not found error with status 404 when find by customer and key id`() {
		case(grpcClient.findKey(any(KeyDetailsRequest::class.java)))
			.thenThrow(StatusRuntimeException(NOT_FOUND.withDescription("Key not found")))

		val e = assertThrows<HttpClientResponseException> {
			client.toBlocking().exchange("/api/key/${UUID.randomUUID()}/1", KeyResponse::class.java)
		}

		assertEquals(HttpStatus.NOT_FOUND, e.status)
		assertEquals("Key not found", e.message)
	}

	@Test
	fun `Should return  key list of a customer when customer exists`() {
		val grpcResponse =
			ListOfKeysResponse.newBuilder().addKey(newKeyDetailsResponse().build()).build()
		case(grpcClient.listKeysOfCustomer(any(ListOfKeysRequest::class.java))).thenReturn(grpcResponse)

		val response = client.toBlocking()
			.exchange(
				"/api/key/${UUID.randomUUID()}/all",
				KeyResponse::class.java
			)

		with(grpcResponse.keyList[0]) grpc@{
			assertEquals(HttpStatus.OK, response.status)
			with(response.body()!!) {
				assertEquals(this@grpc.keyId, keyId)
				assertEquals(this@grpc.key, key)
				assertEquals(this@grpc.keyType, keyType)
				assertEquals(this@grpc.customerId, customerId)
			}
			with(response.body()!!.account) {
				assertEquals(account.customerCPF, customerCPF)
				assertEquals(account.branch, accountBranch)
				assertEquals(account.number, accountNumber)
				assertEquals(account.institution, institution)
				assertEquals(account.customerName, customerName)
				assertEquals(account.accountType, accountType.grpcType)
			}
		}
	}

	@Test
	fun `should return not found error with status 404 when not have customer`() {
		case(grpcClient.listKeysOfCustomer(any(ListOfKeysRequest::class.java)))
			.thenThrow(StatusRuntimeException(NOT_FOUND.withDescription("Key not found")))

		val e = assertThrows<HttpClientResponseException> {
			client.toBlocking().exchange("/api/key/${UUID.randomUUID()}/all", KeyResponse::class.java)
		}

		assertEquals(HttpStatus.NOT_FOUND, e.status)
		assertEquals("Key not found", e.message)
	}

	private fun newKeyDetailsResponse(): KeyDetailsResponse.Builder {
		val now = Instant.now()
		return KeyDetailsResponse.newBuilder()
			.setKey("02654220273")
			.setKeyId(1)
			.setCustomerId(UUID.randomUUID().toString())
			.setKeyType(KeyType.CPF)
			.setCreatedAt(
				Timestamp.newBuilder()
					.setSeconds(now.epochSecond)
					.setNanos(now.nano)
			)
			.setAccount(
				KeyDetailsResponse.AccountDetailsResponse.newBuilder()
					.setCustomerCPF("02654220273")
					.setCustomerName("Afonso")
					.setAccountType(CONTA_CORRENTE)
					.setBranch("123")
					.setNumber("123")
					.setInstitution("ITAÚ UNIBANCO S.A.")
					.build()
			)
	}
}

@Factory
@Replaces(factory = ManageKeyGrpcClient::class)
internal class MockGrpcClient {
	@Singleton
	fun grpcClient(): ManagePixServiceGrpc.ManagePixServiceBlockingStub =
		mock(ManagePixServiceGrpc.ManagePixServiceBlockingStub::class.java)
}
