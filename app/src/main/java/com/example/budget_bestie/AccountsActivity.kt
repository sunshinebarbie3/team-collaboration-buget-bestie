package com.budgetbestie.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budgetbestie.R

class AccountsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts_screen)

        supportActionBar?.title = "Accounts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val addAccountButton = findViewById<Button>(R.id.buttonAddAccount)
        addAccountButton.setOnClickListener {
            Toast.makeText(this, "Add Account feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}