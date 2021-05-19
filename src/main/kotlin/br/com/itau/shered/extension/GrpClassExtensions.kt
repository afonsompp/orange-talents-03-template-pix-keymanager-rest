package br.com.itau.shered.extension

import br.com.itau.managekey.AccountType.*
import br.com.zup.manage.pix.AccountType
import br.com.zup.manage.pix.AccountType.*

fun AccountType.toRestAccountType() = when (this) {
	CONTA_CORRENTE -> CURRENT_ACCOUNT
	CONTA_POUPANCA -> SAVINGS_ACCOUNT
	else -> throw IllegalArgumentException("Invalid enum")
}
