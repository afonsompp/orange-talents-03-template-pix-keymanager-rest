package br.com.itau.shered.validation

import br.com.itau.managekey.RegisterNewKeyRequest
import br.com.zup.manage.pix.KeyType
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator
import javax.inject.Singleton
import javax.validation.Constraint

@MustBeDocumented
@Constraint(validatedBy = [ValidKeyValidator::class])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidKey(
	val message: String = "That's a invalid key "
)

@Singleton
class ValidKeyValidator : ConstraintValidator<ValidKey, RegisterNewKeyRequest> {

	override fun isValid(
		value: RegisterNewKeyRequest?,
		annotationMetadata: AnnotationValue<ValidKey>,
		context: ConstraintValidatorContext
	): Boolean {
		val key = value ?: return false

		val regex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
		if (!key.customerId.matches(regex.toRegex())) {
			context.messageTemplate("The UUID is invalid")
			return false
		}

		return when (key.keyType) {
			KeyType.CPF -> CPFValidator().run {
				initialize(null)
				context.messageTemplate("The CPF is invalid")
				isValid(key.key, null)
			}

			KeyType.EMAIL -> EmailValidator().run {
				initialize(null)
				context.messageTemplate("The Email is invalid")
				isValid(key.key, null)
			}

			KeyType.PHONE -> {
				context.messageTemplate("The Phone number is invalid")
				key.key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
			}

			KeyType.RANDOM -> {
				context.messageTemplate("Value to random key must be null or blank")
				key.key.isBlank()
			}

			else -> {
				context.messageTemplate("Key type cannot be UNKNOWN_TYPE")
				false
			}
		}
	}
}
