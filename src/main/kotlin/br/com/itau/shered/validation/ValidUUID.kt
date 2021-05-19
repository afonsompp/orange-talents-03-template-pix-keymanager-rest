package br.com.itau.shered.validation

import javax.validation.Constraint
import javax.validation.constraints.Pattern

@MustBeDocumented
@Constraint(validatedBy = [])
@Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidUUID(
	val message: String = "UUID must be valid"
)
