package com.budgetbestie

data class Transaction(
    val title: String,
    val amount: String,
    val date: String,
    val isPositive: Boolean,
    val subtitle: String = ""
)