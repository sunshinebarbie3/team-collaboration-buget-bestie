package com.budgetbestie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TransactionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        supportActionBar?.title = "Transactions"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val transactions = listOf(
            Transaction("Sarah Allen", "R1000", "Monday 01/04/24", false),
            Transaction("Divertible Theater", "$50", "Monday 01/04/24", false),
            Transaction("Wimpy", "R25", "Monday 01/04/24", false),
            Transaction("Coffee", "R10", "Monday 01/04/24", false),
            Transaction("mom's money", "+129.00", "Mother", true)
        )

        transactionAdapter = TransactionAdapter(transactions)
        recyclerView.adapter = transactionAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class Transaction(
    val title: String,
    val amount: String,
    val date: String,
    val isPositive: Boolean
)