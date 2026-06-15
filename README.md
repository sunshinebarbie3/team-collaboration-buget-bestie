# Budget Bestie

# Android Budget Tracker Application

# User Guide \& Feature Walkthrough

# 

# Final POE Submission

#  

# 1\. Application Overview

# Budget Bestie is a Kotlin-based Android budget tracking application developed as a final POE (Proof of Evidence) project. The app helps users manage their personal finances by tracking income and expenses, setting spending goals, visualising financial statistics, and earning gamification badges for responsible budgeting behaviour.

# The interface uses a purple and pink gradient colour scheme with green and purple chart colours, tabbed Income/Expenses/Total dashboard sections, transaction-style cards, and a bottom navigation bar for quick access to key areas of the app.

# 

# 2\. Key Features

# Core Features

# •	Login and registration with username and password

# •	Account information screen with name, email, monthly budget, and savings goal

# •	Create and manage spending categories

# •	Log expense entries with date, start/end time, description, category, amount, and optional photo

# •	Add income with source, amount, date received, and notes

# •	Edit or delete expense entries

# •	Set minimum and maximum monthly spending goals

# •	Spending graph per category over a user-selectable period

# •	Visual monthly goal progress panel (below minimum / within range / above maximum)

# •	Gamification badges for meeting budget goals and consistent expense logging

# 

# Own Features (Custom Additions)

# •	CSV Export — export all expense entries as a text-based CSV file from inside the app

# •	Recurring Expense Templates — save a common expense as a template and quickly re-add it later

# 

# 3\. Step-by-Step Screen Walkthrough

# Step 1 — Login Screen

# When the app opens, the user is presented with a Log In screen set against a purple-to-pink gradient background. A white card in the centre contains a Username field, a Password field, and a LOG IN button. Below the button is a "Don't have an account? Sign up" link that navigates to the Register screen.

# &#x20;

# Figure 1: Login Screen

# Step 2 — Register Screen

# New users tap "Sign up" to reach the Register screen. This screen collects:

# •	Full name

# •	Email address

# •	Username

# •	Password

# •	Confirm password

# After filling in all fields the user taps REGISTER to create their account. A BACK TO LOGIN button returns them to the login screen without registering.

# &#x20;

# Figure 2: Register Screen — filled in and ready to submit

#  

# Step 3 — Dashboard (Total View)

# After logging in, the user lands on the main dashboard titled "LERATO'S ACCOUNT". A month/year selector (e.g. JUN 2026) sits at the top so the user can browse different months. Three tabs divide the dashboard:

# •	INCOME — lists all income transactions for the selected month

# •	EXPENSES — lists all expense transactions for the selected month

# •	TOTAL — shows a summary card with Total Income, Total Expenses, and the net Total

# The TOTAL tab (shown below) displays R11†44,00 income, R9†00,00 expenses, and a net total of R2†44,00 for the month. Below the summary cards sits a Statistics bar chart comparing income (purple bar) versus total expenses per category (green bars), with red and dark-green horizontal lines marking the user's maximum and minimum spending goals.

# At the bottom of the dashboard are action buttons: ADD EXPENSE, ADD INCOME, MANAGE EXPENSES, ACCOUNT INFORMATION, and SPENDING GRAPH.

# &#x20;

# Figure 3: Dashboard — Total tab with summary and statistics chart

#  

# Step 4 — Dashboard (Income Tab)

# Switching to the INCOME tab replaces the summary with a list of income transaction cards for the selected month. Each card shows the income source name (e.g. "work"), a note (e.g. "project 1", "project 2"), and the amount in green on the right. The statistics chart remains visible below the list so the user can compare income against expenses at a glance.

# &#x20;

# Figure 4: Dashboard — Income tab showing two income entries

#  

# Step 5 — Dashboard (Expenses Tab)

# The EXPENSES tab shows all expense entries for the month as transaction-style cards. Each card displays the expense description (e.g. "petrol", "food", "electricity", "transport", "mr price account"), the category underneath (e.g. "General"), and the amount in green on the right. The statistics chart below the list provides a visual breakdown of spending.

# Expenses shown for June 2026:

# •	Petrol — R2†00,00

# •	Food — R2†00,00

# •	Electricity — R3†00,00

# •	Transport — R700,00

# •	Mr Price account — R300,00

# &#x20;

# Figure 5: Dashboard — Expenses tab listing all June 2026 expenses

#  

# Step 6 — Add Expense Entry

# Tapping ADD EXPENSE opens the Expense Entry screen. The user fills in:

# •	Date (pre-filled with today's date, e.g. 2026-06-14)

# •	Start time (e.g. 17:17)

# •	End time (e.g. 17:17)

# •	Description (free text)

# •	Amount (numeric)

# •	Category (drop-down selector, defaults to "General")

# An ATTACH PHOTOGRAPH button allows the user to optionally add a photo receipt. Three action buttons are available at the bottom:

# •	SAVE EXPENSE — saves the entry to the ledger

# •	SAVE AS RECURRING TEMPLATE — stores the expense as a reusable template (own feature)

# •	BACK — returns to the dashboard without saving

# &#x20;

# Figure 6: Expense Entry screen with all fields and options

#  

# Step 7 — Account Information

# The Account Information screen lets the user update their personal details and financial goals. The editable fields are:

# •	Full name

# •	Email address

# •	Monthly budget amount

# •	Savings goal amount

# A "Current Account" summary card below the fields shows the stored values in read-only format, confirming what is saved. The SAVE ACCOUNT INFORMATION button commits any changes, while BACK returns to the dashboard.

# &#x20;

# Figure 7: Account Information screen showing current account details

#  

# Step 8 — Categories

# The Categories screen is accessible from the bottom navigation bar. It lists all existing spending categories (e.g. "General", "Christmas", "new years") and provides a text field to add a new category name. Tapping SAVE CATEGORY adds it to the list immediately. The BACK button returns to the previous screen.

# Categories created here appear in the drop-down selector on the Expense Entry screen, allowing the user to organise expenses meaningfully.

# &#x20;

# Figure 8: Categories screen with existing categories and add-new field

#  

# Step 9 — Spending Graph

# The Spending Graph screen gives the user a detailed per-category bar chart of spending for a chosen period. A "Select period" drop-down lets the user choose from options such as 7 days, 30 days, or a custom range.

# The chart displays:

# •	Green bars representing the total spend per category

# •	A red horizontal line showing the maximum spending goal

# •	A dark-green horizontal line showing the minimum spending goal

# In the example below, the "General" category shows R9†00,00 of spending over 7 days, significantly above the Max R3†00 line, alerting the user that they have exceeded their maximum goal. The BACK button returns to the dashboard.

# &#x20;

# Figure 9: Spending Graph — 7-day view showing expenses above maximum goal

#  

# 4\. Navigation

# A bottom navigation bar is present on all main screens with four tabs:

# •	Home — returns to the main dashboard

# •	Activities — navigates to the transaction activity list

# •	Categories — opens the Categories management screen

# •	Account — opens the Account Information screen

# The dashboard itself also contains quick-action buttons (ADD EXPENSE, ADD INCOME, MANAGE EXPENSES, ACCOUNT INFORMATION, SPENDING GRAPH) so the most common tasks are always one tap away.

# 

# 5\. How To Run the App

# To run Budget Bestie on a development machine:

# •	Open the project folder in Android Studio

# •	Allow Gradle to sync all dependencies

# •	Select the "app" run configuration

# •	Run on an Android emulator or a connected Android device

# App icon and all drawable image assets are located in app/src/main/res/drawable.

# 

# 6\. Technology Stack

# •	Language: Kotlin

# •	Platform: Android

# •	Build system: Gradle

# •	UI: XML layouts with purple/pink gradient theming

# •	Charts: Custom bar chart with min/max goal lines

# •	Storage: Local on-device storage for user data, expenses, and income

# 

# 7\. Own Features Detail

# CSV Export

# From within the app the user can export a plain-text CSV (Comma-Separated Values) file containing all their expense entries. This file can be opened in any spreadsheet application for further analysis or record-keeping. The export is initiated from the main dashboard area.

# Recurring Expense Templates

# When adding a new expense, the user can tap "SAVE AS RECURRING TEMPLATE" to store that expense configuration as a reusable template. The next time they need to log the same regular expense (e.g. a monthly subscription or weekly petrol fill-up) they can apply the template instead of filling in all fields again, saving time and reducing data entry errors.



