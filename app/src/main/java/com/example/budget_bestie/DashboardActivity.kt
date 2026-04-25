package com.budgetbestie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Set click listeners for navigation cards
        val transactionsCard = findViewById<CardView>(R.id.cardTransactions)
        val accountsCard = findViewById<CardView>(R.id.cardAccounts)

        transactionsCard.setOnClickListener {
            val intent = Intent(this, TransactionsActivity::class.java)
            startActivity(intent)
        }

        accountsCard.setOnClickListener {
            val intent = Intent(this, AccountsActivity::class.java)
            startActivity(intent)
        }
    }
}

