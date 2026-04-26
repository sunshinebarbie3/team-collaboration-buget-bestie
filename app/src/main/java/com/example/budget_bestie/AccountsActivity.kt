package com.budgetbestie

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AccountsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)

        supportActionBar?.title = "Accounts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val addAccountButton = findViewById<Button>(R.id.buttonAddAccount)
        addAccountButton.setOn
    }
}