package com.example.budgetbestie

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.max

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("budget_bestie", MODE_PRIVATE) }
    private val store by lazy { BudgetStore(prefs) }
    private var selectedPhotoUri: String? = null
    private var pendingPhotoLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 45 && resultCode == RESULT_OK) {
            selectedPhotoUri = data?.data?.toString()
            data?.data?.let { uri ->
                runCatching {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            pendingPhotoLabel?.text = "Photo attached"
        }
    }

    private fun showLogin() {
        val username = input("Username")
        val password = input("Password")
        password.inputType = 0x00000081
        val savedProfile = store.load().profile

        val body = vertical {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(text(":)", 34, true).apply {
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setMargins(0, 48, 0, 170)
            })
            addView(loginCard {
                addView(TextView(this@MainActivity).apply {
                    text = "Log in"
                    textSize = 22f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setTextColor(Color.rgb(36, 39, 45))
                    setMargins(0, 8, 0, 22)
                })
                addView(label("Username"))
                addView(username)
                addView(label("Password"))
                addView(password)
                addView(primaryButton("Log in") {
                    if (username.text.isBlank() || password.text.isBlank()) {
                        toast("Enter a username and password")
                    } else if (savedProfile.username.isNotBlank() &&
                        (username.text.toString() != savedProfile.username || password.text.toString() != savedProfile.password)
                    ) {
                        toast("Incorrect username or password")
                    } else {
                        prefs.edit().putString("username", username.text.toString()).apply()
                        showDashboard()
                    }
                })
                addView(TextView(this@MainActivity).apply {
                    text = "Don't have an account? Sign up"
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setTextColor(Color.rgb(126, 121, 138))
                    setPadding(0, dp(12), 0, 0)
                    setOnClickListener { showRegister() }
                })
            })
        }
        setContentView(loginShell(body))
    }

    private fun showRegister() {
        val data = store.load()
        val fullName = input("Full Name").apply { setText(data.profile.fullName) }
        val email = input("Email Address").apply { setText(data.profile.email) }
        val username = input("Username").apply { setText(data.profile.username) }
        val password = input("Password").apply {
            setText(data.profile.password)
            inputType = 0x00000081
        }
        val confirmPassword = input("Confirm Password").apply { inputType = 0x00000081 }

        val body = vertical {
            addView(title("Register"))
            addView(fullName)
            addView(email)
            addView(username)
            addView(password)
            addView(confirmPassword)
            addView(primaryButton("Register") {
                when {
                    fullName.text.isBlank() -> toast("Enter your full name")
                    email.text.isBlank() || !email.text.contains("@") -> toast("Enter a valid email address")
                    username.text.isBlank() -> toast("Enter a username")
                    password.text.isBlank() -> toast("Enter a password")
                    password.text.toString() != confirmPassword.text.toString() -> toast("Passwords do not match")
                    else -> {
                        data.profile = UserProfile(
                            fullName = fullName.text.toString(),
                            email = email.text.toString(),
                            username = username.text.toString(),
                            password = password.text.toString(),
                            monthlyBudget = data.profile.monthlyBudget,
                            savingsGoal = data.profile.savingsGoal
                        )
                        store.save(data)
                        prefs.edit().putString("username", username.text.toString()).apply()
                        showDashboard()
                    }
                }
            })
            addView(secondaryButton("Back to login") { showLogin() })
        }
        setContentView(loginShell(body))
    }

    private fun showDashboard(selectedTab: String = "TOTAL") {
        val data = store.load()
        val lastMonth = LocalDate.now().minusDays(30)
        val recentTotal = data.expenses.filter { it.date >= lastMonth }.sumOf { it.amount }
        val recentIncome = data.incomes.filter { it.dateReceived >= lastMonth }.sumOf { it.amount }
        val balance = recentIncome - recentTotal
        val accountName = data.profile.fullName.ifBlank { prefs.getString("username", "Lerato") ?: "Lerato" }.uppercase()
        val graph = SpendingGraphView(this)
        graph.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220))
        graph.setData(
            expenses = data.expenses.filter { it.date >= lastMonth },
            minGoal = data.minGoal,
            maxGoal = data.maxGoal,
            incomes = data.incomes.filter { it.dateReceived >= lastMonth }
        )

        val body = vertical {
            addView(TextView(this@MainActivity).apply {
                text = "$accountName'S ACCOUNT"
                textSize = 21f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.rgb(30, 34, 43))
                gravity = Gravity.CENTER
                setMargins(0, 12, 0, 10)
            })
            addView(monthSelector())
            addView(tabRow(selectedTab))

            when (selectedTab) {
                "INCOME" -> addIncomeRows(data)
                "EXPENSES" -> addExpenseRows(data)
                else -> addTotalRows(recentIncome, recentTotal, balance)
            }

            addView(panel {
                addView(subtitle("Statistics"))
                addView(legendRow())
                addView(graph)
            })
            addView(primaryButton("Add expense") { showExpenseForm() })
            addView(primaryButton("Add income") { showIncomeForm() })
            addView(secondaryButton("Manage expenses") { showExpenseManager() })
            addView(secondaryButton("Account information") { showAccountInfo() })
            addView(secondaryButton("Spending graph") { showGraph() })
        }
        setContentView(shell(body))
    }

    private fun LinearLayout.addIncomeRows(data: BudgetData) {
        if (data.incomes.isEmpty()) {
            addView(summaryCard("No income yet", "Tap Add income to save your first income record.", null))
        } else {
            data.incomes.sortedByDescending { it.dateReceived }.take(5).forEach {
                addView(summaryCard(it.source, it.notes.ifBlank { it.dateReceived.toString() }, "R${money(it.amount)}"))
            }
        }
    }

    private fun LinearLayout.addExpenseRows(data: BudgetData) {
        if (data.expenses.isEmpty()) {
            addView(summaryCard("No expenses yet", "Tap Add expense to start tracking spending.", null))
        } else {
            data.expenses.sortedByDescending { it.date }.take(5).forEach {
                addView(summaryCard(it.description, it.category, "R${money(it.amount)}"))
            }
        }
    }

    private fun LinearLayout.addTotalRows(income: Double, expenses: Double, balance: Double) {
        addView(summaryCard("TOTAL INCOME", "", "R${money(income)}"))
        addView(summaryCard("TOTAL EXPENSES", "", "R${money(expenses)}"))
        addView(summaryCard("TOTAL", "", "R${money(balance)}"))
    }

    private fun showIncomeForm() {
        val data = store.load()
        val amount = input("Income Amount")
        val source = input("Income Source")
        val dateReceived = input("Date Received yyyy-MM-dd").apply { setText(LocalDate.now().toString()) }
        val notes = input("Notes")

        val body = vertical {
            addView(title("Add Income"))
            addView(amount)
            addView(source)
            addView(dateReceived)
            addView(notes)
            addView(primaryButton("Save") {
                val amountValue = amount.text.toString().toDoubleOrNull()
                val parsedDate = runCatching { LocalDate.parse(dateReceived.text.toString()) }.getOrNull()
                if (amountValue == null || amountValue <= 0.0 || source.text.isBlank() || parsedDate == null) {
                    toast("Add a valid amount, source, and date")
                } else {
                    data.incomes.add(
                        Income(
                            id = UUID.randomUUID().toString(),
                            amount = amountValue,
                            source = source.text.toString(),
                            dateReceived = parsedDate,
                            notes = notes.text.toString()
                        )
                    )
                    store.save(data)
                    showDashboard()
                }
            })
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showCategories() {
        val data = store.load()
        val category = input("New category name")
        val body = vertical {
            addView(title("Categories"))
            addView(text(if (data.categories.isEmpty()) "No categories yet." else data.categories.joinToString("\n")))
            addView(category)
            addView(primaryButton("Save category") {
                val name = category.text.toString().trim()
                if (name.isBlank()) toast("Enter a category name") else {
                    data.categories.add(name)
                    store.save(data)
                    showCategories()
                }
            })
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showGoals() {
        val data = store.load()
        val min = input("Minimum monthly goal").apply { setText(data.minGoal.toString()) }
        val max = input("Maximum monthly goal").apply { setText(data.maxGoal.toString()) }
        val body = vertical {
            addView(title("Monthly Goals"))
            addView(min)
            addView(max)
            addView(primaryButton("Save goals") {
                val minValue = min.text.toString().toDoubleOrNull()
                val maxValue = max.text.toString().toDoubleOrNull()
                if (minValue == null || maxValue == null || minValue < 0 || maxValue < minValue) {
                    toast("Enter valid goals")
                } else {
                    data.minGoal = minValue
                    data.maxGoal = maxValue
                    store.save(data)
                    showDashboard()
                }
            })
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showAccountInfo() {
        val data = store.load()
        val fullName = input("Full Name").apply { setText(data.profile.fullName) }
        val email = input("Email Address").apply { setText(data.profile.email) }
        val monthlyBudget = input("Monthly Budget").apply { setText(data.profile.monthlyBudget.toString()) }
        val savingsGoal = input("Savings Goal").apply { setText(data.profile.savingsGoal.toString()) }

        val body = vertical {
            addView(title("Account Information"))
            addView(fullName)
            addView(email)
            addView(monthlyBudget)
            addView(savingsGoal)
            addView(panel {
                addView(subtitle("Current Account"))
                addView(text("Full Name: ${data.profile.fullName.ifBlank { "Not set" }}"))
                addView(text("Email Address: ${data.profile.email.ifBlank { "Not set" }}"))
                addView(text("Username: ${data.profile.username.ifBlank { prefs.getString("username", "Not set") ?: "Not set" }}"))
                addView(text("Monthly Budget: R${money(data.profile.monthlyBudget)}"))
                addView(text("Savings Goal: R${money(data.profile.savingsGoal)}"))
            })
            addView(primaryButton("Save Account Information") {
                val budgetValue = monthlyBudget.text.toString().toDoubleOrNull()
                val savingsValue = savingsGoal.text.toString().toDoubleOrNull()
                if (fullName.text.isBlank() || email.text.isBlank() || budgetValue == null || savingsValue == null) {
                    toast("Complete the account information")
                } else {
                    data.profile = data.profile.copy(
                        fullName = fullName.text.toString(),
                        email = email.text.toString(),
                        monthlyBudget = budgetValue,
                        savingsGoal = savingsValue
                    )
                    store.save(data)
                    showDashboard()
                }
            })
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showExpenseForm(template: ExpenseTemplate? = null) {
        val data = store.load()
        if (data.categories.isEmpty()) data.categories.add("General")
        selectedPhotoUri = null
        val date = input("Date yyyy-MM-dd").apply { setText(LocalDate.now().toString()) }
        val start = input("Start time HH:mm").apply { setText(LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString()) }
        val end = input("End time HH:mm").apply { setText(LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString()) }
        val description = input("Description").apply { setText(template?.description ?: "") }
        val amount = input("Amount").apply { setText(template?.amount?.toString() ?: "") }
        val category = Spinner(this)
        category.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, data.categories)
        template?.category?.let {
            val index = data.categories.indexOf(it)
            if (index >= 0) category.setSelection(index)
        }
        pendingPhotoLabel = text("No photo attached")

        val body = vertical {
            addView(title("Expense Entry"))
            addView(date)
            addView(start)
            addView(end)
            addView(description)
            addView(amount)
            addView(label("Category"))
            addView(category)
            addView(secondaryButton("Attach photograph") { pickPhoto() })
            addView(pendingPhotoLabel)
            addView(primaryButton("Save expense") {
                val parsedDate = runCatching { LocalDate.parse(date.text.toString()) }.getOrNull()
                val amountValue = amount.text.toString().toDoubleOrNull()
                if (parsedDate == null || description.text.isBlank() || amountValue == null || amountValue < 0) {
                    toast("Add a valid date, description, and amount")
                } else {
                    val expense = Expense(
                        id = UUID.randomUUID().toString(),
                        date = parsedDate,
                        startTime = start.text.toString(),
                        endTime = end.text.toString(),
                        description = description.text.toString(),
                        category = category.selectedItem.toString(),
                        amount = amountValue,
                        photoUri = selectedPhotoUri
                    )
                    data.expenses.add(expense)
                    store.save(data)
                    showDashboard()
                }
            })
            addView(secondaryButton("Save as recurring template") {
                val amountValue = amount.text.toString().toDoubleOrNull()
                if (description.text.isBlank() || amountValue == null) toast("Add description and amount") else {
                    data.templates.add(ExpenseTemplate(description.text.toString(), category.selectedItem.toString(), amountValue))
                    store.save(data)
                    toast("Template saved")
                }
            })
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showExpenseManager() {
        val data = store.load()
        val body = vertical {
            addView(title("Manage Expenses"))
            if (data.expenses.isEmpty()) {
                addView(text("No expenses saved yet."))
            } else {
                data.expenses.sortedByDescending { it.date }.forEach { expense ->
                    addView(panel {
                        addView(subtitle(expense.description))
                        addView(text("${expense.date} | ${expense.category} | R${money(expense.amount)}"))
                        addView(primaryButton("Edit expense") { showEditExpense(expense.id) })
                        addView(secondaryButton("Delete expense") {
                            data.expenses.removeAll { it.id == expense.id }
                            store.save(data)
                            showExpenseManager()
                        })
                    })
                }
            }
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showEditExpense(expenseId: String) {
        val data = store.load()
        if (data.categories.isEmpty()) data.categories.add("General")
        val expense = data.expenses.firstOrNull { it.id == expenseId }
        if (expense == null) {
            toast("Expense not found")
            showExpenseManager()
            return
        }
        val amount = input("Update Amount").apply { setText(expense.amount.toString()) }
        val description = input("Edit Description").apply { setText(expense.description) }
        val category = Spinner(this)
        category.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, data.categories)
        val selectedIndex = data.categories.indexOf(expense.category)
        if (selectedIndex >= 0) category.setSelection(selectedIndex)

        val body = vertical {
            addView(title("Edit Expense"))
            addView(amount)
            addView(label("Change Category"))
            addView(category)
            addView(description)
            addView(primaryButton("Save Changes") {
                val amountValue = amount.text.toString().toDoubleOrNull()
                if (amountValue == null || amountValue < 0.0 || description.text.isBlank()) {
                    toast("Add a valid amount and description")
                } else {
                    val index = data.expenses.indexOfFirst { it.id == expenseId }
                    data.expenses[index] = expense.copy(
                        amount = amountValue,
                        category = category.selectedItem.toString(),
                        description = description.text.toString()
                    )
                    store.save(data)
                    showExpenseManager()
                }
            })
            addView(secondaryButton("Delete Expense") {
                data.expenses.removeAll { it.id == expenseId }
                store.save(data)
                showExpenseManager()
            })
            addView(secondaryButton("Back") { showExpenseManager() })
        }
        setContentView(shell(body))
    }

    private fun showGraph() {
        val data = store.load()
        val periods = listOf("7 days", "30 days", "90 days", "All time")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periods)
        val graph = SpendingGraphView(this)
        graph.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(280))

        fun refresh() {
            val days = when (spinner.selectedItem.toString()) {
                "7 days" -> 7L
                "30 days" -> 30L
                "90 days" -> 90L
                else -> 36500L
            }
            val start = LocalDate.now().minusDays(days)
            val expenses = data.expenses.filter { it.date >= start }
            graph.setData(expenses, data.minGoal, data.maxGoal)
        }
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) = refresh()
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
        refresh()

        val body = vertical {
            addView(title("Spending Graph"))
            addView(label("Select period"))
            addView(spinner)
            addView(graph)
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showTemplates() {
        val data = store.load()
        val body = vertical {
            addView(title("Recurring Templates"))
            if (data.templates.isEmpty()) {
                addView(text("No templates saved yet. Add an expense and save it as a template."))
            } else {
                data.templates.forEach { template ->
                    addView(panel {
                        addView(subtitle(template.description))
                        addView(text("${template.category} - R${money(template.amount)}"))
                        addView(primaryButton("Use template") { showExpenseForm(template) })
                    })
                }
            }
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun showExport() {
        val csv = store.load().expenses.joinToString("\n", "date,start,end,description,category,amount,photo\n") {
            "${it.date},${it.startTime},${it.endTime},\"${it.description}\",${it.category},${it.amount},${it.photoUri ?: ""}"
        }
        val body = vertical {
            addView(title("CSV Export"))
            addView(text(csv))
            addView(secondaryButton("Back") { showDashboard() })
        }
        setContentView(shell(body))
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, 45)
    }

    private fun buildBadges(data: BudgetData, recentTotal: Double): List<String> {
        val badges = mutableListOf<String>()
        if (recentTotal in data.minGoal..data.maxGoal) badges.add("Goal Keeper: stayed within monthly range")
        if (data.expenses.size >= 5) badges.add("Logging Streak: recorded 5 or more expenses")
        if (data.categories.size >= 3) badges.add("Organised Planner: created 3 categories")
        return badges
    }

    private fun loginShell(content: View): ScrollView = ScrollView(this).apply {
        background = gradient(Color.rgb(139, 64, 255), Color.rgb(216, 61, 219))
        addView(content)
    }

    private fun shell(content: View): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(Color.WHITE)
        addView(ScrollView(this@MainActivity).apply {
            addView(content)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        addView(bottomNav())
    }

    private fun vertical(block: LinearLayout.() -> Unit): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(22), dp(22), dp(22), dp(22))
        block()
    }

    private fun panel(block: LinearLayout.() -> Unit): LinearLayout = vertical(block).apply {
        background = rounded(Color.WHITE, dp(8), Color.rgb(222, 225, 232))
        setMargins(0, 8, 0, 8)
    }

    private fun loginCard(block: LinearLayout.() -> Unit): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(22), dp(24), dp(22), dp(26))
        background = rounded(Color.WHITE, dp(8), Color.TRANSPARENT)
        block()
    }

    private fun monthSelector(): TextView = TextView(this).apply {
        text = "${LocalDate.now().month.name.take(3)} ${LocalDate.now().year}".uppercase()
        textSize = 12f
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(84, 89, 105))
        background = rounded(Color.WHITE, dp(3), Color.rgb(205, 210, 222))
        setPadding(dp(16), dp(8), dp(16), dp(8))
        layoutParams = LinearLayout.LayoutParams(dp(160), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, dp(14))
        }
    }

    private fun tabRow(selected: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        listOf("INCOME", "EXPENSES", "TOTAL").forEach { tab ->
            addView(TextView(this@MainActivity).apply {
                text = tab
                textSize = 12f
                typeface = if (tab == selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                gravity = Gravity.CENTER
                setTextColor(if (tab == selected) Color.rgb(99, 81, 255) else Color.rgb(134, 139, 153))
                background = if (tab == selected) rounded(Color.rgb(242, 239, 255), dp(4), Color.rgb(99, 81, 255)) else null
                setPadding(dp(8), dp(8), dp(8), dp(8))
                setOnClickListener { showDashboard(tab) }
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun summaryCard(title: String, detail: String, amount: String?): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = rounded(Color.WHITE, dp(5), Color.rgb(220, 224, 234))
        setPadding(dp(12), dp(14), dp(12), dp(14))
        setMargins(0, 10, 0, 0)
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.rgb(35, 39, 49))
            })
            if (detail.isNotBlank()) addView(TextView(this@MainActivity).apply {
                text = detail
                textSize = 11f
                setTextColor(Color.rgb(118, 124, 139))
            })
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        amount?.let {
            addView(TextView(this@MainActivity).apply {
                text = it
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (it.contains("-")) Color.rgb(221, 61, 130) else Color.rgb(33, 169, 110))
                gravity = Gravity.END
            })
        }
    }

    private fun legendRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(legendText("INCOME", Color.rgb(144, 63, 255)))
        addView(legendText("EXPENSES", Color.rgb(82, 219, 146)))
    }

    private fun legendText(label: String, color: Int): TextView = TextView(this).apply {
        text = "■ $label"
        textSize = 11f
        setTextColor(color)
        setPadding(dp(8), 0, dp(8), 0)
    }

    private fun bottomNav(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(6), dp(6), dp(6), dp(8))
        background = rounded(Color.WHITE, 0, Color.rgb(226, 229, 238))
        addView(navButton("Home") { showDashboard("TOTAL") })
        addView(navButton("Activities") { showExpenseManager() })
        addView(navButton("Categories") { showCategories() })
        addView(navButton("Account") { showAccountInfo() })
    }

    private fun navButton(label: String, action: () -> Unit): TextView = TextView(this).apply {
        text = label
        textSize = 11f
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(99, 81, 255))
        setPadding(dp(4), dp(6), dp(4), dp(6))
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }

    private fun title(value: String) = text(value, 26, true)
    private fun subtitle(value: String) = text(value, 18, true)
    private fun label(value: String) = text(value, 14, true)
    private fun text(value: String, size: Int = 15, bold: Boolean = false): TextView = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(Color.rgb(34, 38, 35))
        if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD
        setPadding(0, dp(6), 0, dp(6))
    }

    private fun input(hint: String): EditText = EditText(this).apply {
        this.hint = hint
        setSingleLine(false)
        setPadding(dp(10), dp(8), dp(10), dp(8))
        background = rounded(Color.WHITE, dp(7), Color.rgb(198, 203, 214))
    }

    private fun primaryButton(label: String, action: () -> Unit) = button(label, true, action)
    private fun secondaryButton(label: String, action: () -> Unit) = button(label, false, action)
    private fun button(label: String, primary: Boolean, action: () -> Unit): Button = Button(this).apply {
        text = label
        setTextColor(if (primary) Color.WHITE else Color.rgb(99, 81, 255))
        background = if (primary) gradient(Color.rgb(139, 64, 255), Color.rgb(184, 59, 236)) else rounded(Color.WHITE, dp(8), Color.rgb(99, 81, 255))
        setOnClickListener { action() }
        setMargins(0, 8, 0, 0)
    }

    private fun goalMeter(total: Double, minGoal: Double, maxGoal: Double): ProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
        max = max(1, maxGoal.toInt())
        progress = total.toInt().coerceIn(0, max)
        setMargins(0, 8, 0, 8)
    }

    private fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dp(left), dp(top), dp(right), dp(bottom))
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun money(value: Double) = "%.2f".format(value)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int, stroke: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        if (stroke != Color.TRANSPARENT) setStroke(dp(1), stroke)
    }
    private fun gradient(start: Int, end: Int): GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(start, end)
    ).apply {
        cornerRadius = dp(8).toFloat()
    }
}

class SpendingGraphView(context: android.content.Context) : View(context) {
    private var expenses: List<Expense> = emptyList()
    private var incomes: List<Income> = emptyList()
    private var minGoal = 0.0
    private var maxGoal = 0.0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setData(expenses: List<Expense>, minGoal: Double, maxGoal: Double, incomes: List<Income> = emptyList()) {
        this.expenses = expenses
        this.incomes = incomes
        this.minGoal = minGoal
        this.maxGoal = maxGoal
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val grouped = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
        val totalIncome = incomes.sumOf { it.amount }
        val left = 56f
        val top = 24f
        val bottom = height - 44f
        val right = width - 18f
        paint.textSize = 24f
        paint.color = Color.rgb(34, 38, 35)
        if (grouped.isEmpty() && totalIncome <= 0.0) {
            canvas.drawText("No spending in this period yet", left, height / 2f, paint)
            return
        }
        val maxValue = maxOf(grouped.values.maxOrNull() ?: 1.0, totalIncome, maxGoal, minGoal, 1.0)
        paint.strokeWidth = 3f
        paint.color = Color.rgb(130, 125, 115)
        canvas.drawLine(left, bottom, right, bottom, paint)
        drawGoalLine(canvas, minGoal, maxValue, top, bottom, left, right, Color.rgb(0, 108, 103), "Min")
        drawGoalLine(canvas, maxGoal, maxValue, top, bottom, left, right, Color.rgb(221, 61, 130), "Max")

        val bars = mutableListOf<Pair<String, Double>>()
        if (totalIncome > 0.0) bars.add("Income" to totalIncome)
        grouped.entries.forEach { bars.add(it.key to it.value) }
        val barWidth = (right - left) / bars.size * 0.5f
        bars.forEachIndexed { index, entry ->
            val x = left + index * ((right - left) / bars.size) + barWidth * 0.35f
            val barHeight = ((entry.second / maxValue) * (bottom - top)).toFloat()
            paint.color = if (entry.first == "Income") Color.rgb(144, 63, 255) else Color.rgb(82, 219, 146)
            canvas.drawRect(x, bottom - barHeight, x + barWidth, bottom, paint)
            paint.color = Color.rgb(34, 38, 35)
            paint.textSize = 20f
            canvas.drawText(entry.first.take(8), x, bottom + 24f, paint)
            canvas.drawText("R${"%.0f".format(entry.second)}", x, bottom - barHeight - 8f, paint)
        }
    }

    private fun drawGoalLine(canvas: Canvas, goal: Double, maxValue: Double, top: Float, bottom: Float, left: Float, right: Float, color: Int, label: String) {
        val y = bottom - ((goal / maxValue) * (bottom - top)).toFloat()
        paint.color = color
        paint.strokeWidth = 4f
        canvas.drawLine(left, y, right, y, paint)
        paint.textSize = 20f
        canvas.drawText("$label R${"%.0f".format(goal)}", left, y - 6f, paint)
    }
}

data class BudgetData(
    val categories: MutableList<String> = mutableListOf(),
    val expenses: MutableList<Expense> = mutableListOf(),
    val incomes: MutableList<Income> = mutableListOf(),
    val templates: MutableList<ExpenseTemplate> = mutableListOf(),
    var profile: UserProfile = UserProfile(),
    var minGoal: Double = 500.0,
    var maxGoal: Double = 3000.0
)

data class Expense(
    val id: String,
    val date: LocalDate,
    val startTime: String,
    val endTime: String,
    val description: String,
    val category: String,
    val amount: Double,
    val photoUri: String?
)

data class ExpenseTemplate(
    val description: String,
    val category: String,
    val amount: Double
)

data class Income(
    val id: String,
    val amount: Double,
    val source: String,
    val dateReceived: LocalDate,
    val notes: String
)

data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val monthlyBudget: Double = 0.0,
    val savingsGoal: Double = 0.0
)

class BudgetStore(private val prefs: android.content.SharedPreferences) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun load(): BudgetData {
        val json = JSONObject(prefs.getString("data", "{}") ?: "{}")
        val data = BudgetData(
            minGoal = json.optDouble("minGoal", 500.0),
            maxGoal = json.optDouble("maxGoal", 3000.0)
        )
        json.optJSONObject("profile")?.let { profile ->
            data.profile = UserProfile(
                fullName = profile.optString("fullName"),
                email = profile.optString("email"),
                username = profile.optString("username"),
                password = profile.optString("password"),
                monthlyBudget = profile.optDouble("monthlyBudget", 0.0),
                savingsGoal = profile.optDouble("savingsGoal", 0.0)
            )
        }
        json.optJSONArray("categories")?.let { array ->
            for (i in 0 until array.length()) data.categories.add(array.getString(i))
        }
        json.optJSONArray("expenses")?.let { array ->
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                data.expenses.add(
                    Expense(
                        id = item.getString("id"),
                        date = LocalDate.parse(item.getString("date"), dateFormatter),
                        startTime = item.getString("startTime"),
                        endTime = item.getString("endTime"),
                        description = item.getString("description"),
                        category = item.getString("category"),
                        amount = item.getDouble("amount"),
                        photoUri = item.optString("photoUri").ifBlank { null }
                    )
                )
            }
        }
        json.optJSONArray("incomes")?.let { array ->
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                data.incomes.add(
                    Income(
                        id = item.getString("id"),
                        amount = item.getDouble("amount"),
                        source = item.getString("source"),
                        dateReceived = LocalDate.parse(item.getString("dateReceived"), dateFormatter),
                        notes = item.optString("notes")
                    )
                )
            }
        }
        json.optJSONArray("templates")?.let { array ->
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                data.templates.add(ExpenseTemplate(item.getString("description"), item.getString("category"), item.getDouble("amount")))
            }
        }
        return data
    }

    fun save(data: BudgetData) {
        val json = JSONObject()
        json.put("minGoal", data.minGoal)
        json.put("maxGoal", data.maxGoal)
        json.put("profile", JSONObject().apply {
            put("fullName", data.profile.fullName)
            put("email", data.profile.email)
            put("username", data.profile.username)
            put("password", data.profile.password)
            put("monthlyBudget", data.profile.monthlyBudget)
            put("savingsGoal", data.profile.savingsGoal)
        })
        json.put("categories", JSONArray(data.categories))
        json.put("expenses", JSONArray().apply {
            data.expenses.forEach {
                put(JSONObject().apply {
                    put("id", it.id)
                    put("date", it.date.toString())
                    put("startTime", it.startTime)
                    put("endTime", it.endTime)
                    put("description", it.description)
                    put("category", it.category)
                    put("amount", it.amount)
                    put("photoUri", it.photoUri ?: "")
                })
            }
        })
        json.put("incomes", JSONArray().apply {
            data.incomes.forEach {
                put(JSONObject().apply {
                    put("id", it.id)
                    put("amount", it.amount)
                    put("source", it.source)
                    put("dateReceived", it.dateReceived.toString())
                    put("notes", it.notes)
                })
            }
        })
        json.put("templates", JSONArray().apply {
            data.templates.forEach {
                put(JSONObject().apply {
                    put("description", it.description)
                    put("category", it.category)
                    put("amount", it.amount)
                })
            }
        })
        prefs.edit().putString("data", json.toString()).apply()
    }
}
