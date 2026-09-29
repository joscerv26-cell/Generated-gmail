package com.example.data

import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    suspend fun insert(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun insertAll(accounts: List<AccountEntity>) {
        accountDao.insertAccounts(accounts)
    }

    suspend fun deleteAll() {
        accountDao.deleteAllAccounts()
    }

    suspend fun deleteById(id: Long) {
        accountDao.deleteAccountById(id)
    }
}
