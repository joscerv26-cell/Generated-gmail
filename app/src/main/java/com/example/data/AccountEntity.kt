package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gmail_accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val email: String,
    val password: String,
    val screenId: Int,
    val status: String, // "SUCCESS", "FAILED", "PENDING"
    val ipAddress: String,
    val createdAt: Long = System.currentTimeMillis()
)
