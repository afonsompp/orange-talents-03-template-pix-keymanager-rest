package br.com.itau.managekey

import br.com.zup.manage.pix.AccountType.*
import br.com.zup.manage.pix.AccountType as GrpAccountType

enum class AccountType(val grpcType: GrpAccountType) {
	CURRENT_ACCOUNT(CONTA_CORRENTE),
	SAVINGS_ACCOUNT(CONTA_POUPANCA)
}
