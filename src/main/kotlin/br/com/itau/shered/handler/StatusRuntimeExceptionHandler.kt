package br.com.itau.shered.handler

import io.grpc.Status
import io.grpc.Status.*
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.*
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import javax.inject.Singleton

@Produces
@Singleton
@Requirements(
	Requires(classes = [StatusRuntimeException::class, ExceptionHandler::class])
)
class StatusRuntimeExceptionHandler(private val errorResponseProcessor: ErrorResponseProcessor<Any>) :
	ExceptionHandler<StatusRuntimeException, HttpResponse<*>> {

	override fun handle(request: HttpRequest<*>, exception: StatusRuntimeException): HttpResponse<*> {
		return errorResponseProcessor.processResponse(
			ErrorContext.builder(request)
				.cause(exception)
				.errorMessage(exception.status.description!!)
				.build(), HttpResponse.status<Any>(exception.status.toHttpStatus())
		)
	}

	private fun Status.toHttpStatus(): HttpStatus {
		return when (this.code) {
			INVALID_ARGUMENT.code -> BAD_REQUEST
			NOT_FOUND.code -> HttpStatus.NOT_FOUND
			ALREADY_EXISTS.code -> UNPROCESSABLE_ENTITY
			else -> INTERNAL_SERVER_ERROR
		}
	}
}
