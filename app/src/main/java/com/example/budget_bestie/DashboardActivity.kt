package com.budgetbestie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnTransactions = findViewById<Button>(R.id.btnTransactions)
        val btnAccounts = findViewById<Button>(R.id.btnAccounts)

        btnTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
        }

        btnAccounts.setOnClickListener {
            startActivity(Intent(this, AccountsActivity::class.java))
        }
    }
}