package br.com.itau.shered.validation

import br.com.itau.managekey.AccountType.CURRENT_ACCOUNT
import br.com.itau.managekey.RegisterNewKeyRequest
import br.com.zup.manage.pix.KeyType.*
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ValidKeyValidatorTest {
	@Inject
	lateinit var validator: Validator

	@ParameterizedTest
	@MethodSource("provideValues")
	fun `Should validate account, key type and key value, and return message error`(
		request: RegisterNewKeyRequest,
		error: String?
	) {

		val errors = validator.validate(request)
		val errorsMessage = errors.stream().iterator().next().messageTemplate
		assertEquals(true, errorsMessage.equals(error, true))
	}

	@Test
	fun `Don't should return error to valid key values`() {
		val request = RegisterNewKeyRequest("", RANDOM, CURRENT_ACCOUNT, UUID.randomUUID().toString())
		assertTrue(validator.validate(request).isEmpty())
	}

	private fun provideValues(): Stream<Arguments> {
		val uuid = UUID.randomUUID().toString()
		return Stream.of(
			Arguments.of(
				RegisterNewKeyRequest("", RANDOM, CURRENT_ACCOUNT, uuid + "1"),
				"The UUID is invalid"
			),
			Arguments.of(
				RegisterNewKeyRequest("02654220274", CPF, CURRENT_ACCOUNT, uuid),
				"The CPF is invalid"
			),
			Arguments.of(
				RegisterNewKeyRequest("afonso@dew.", EMAIL, CURRENT_ACCOUNT, uuid),
				"The Email is invalid"
			),
			Arguments.of(
				RegisterNewKeyRequest("5569993551645", PHONE, CURRENT_ACCOUNT, uuid),
				"The Phone number is invalid"
			),
			Arguments.of(
				RegisterNewKeyRequest(".", RANDOM, CURRENT_ACCOUNT, uuid),
				"Value to random key must be null or blank"
			),
			Arguments.of(
				RegisterNewKeyRequest(".", UNKNOWN_TYPE, CURRENT_ACCOUNT, uuid),
				"Key type cannot be UNKNOWN_TYPE"
			),
		)
	}
}
