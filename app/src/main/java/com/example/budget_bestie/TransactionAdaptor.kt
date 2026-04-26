package com.budgetbestie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.budgetbestie.R

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.textDate)
        private val titleText: TextView = itemView.findViewById(R.id.textTitle)
        private val subtitleText: TextView = itemView.findViewById(R.id.textSubtitle)
        private val amountText: TextView = itemView.findViewById(R.id.textAmount)

        fun bind(transaction: Transaction) {
            dateText.text = transaction.date
            titleText.text = transaction.title
            amountText.text = transaction.amount

            if (transaction.isPositive) {
                amountText.setTextColor(itemView.context.getColor(R.color.green))
                if (transaction.subtitle.isNotEmpty()) {
                    subtitleText.visibility = View.VISIBLE
                    subtitleText.text = transaction.subtitle
                }
            } else {
                amountText.setTextColor(itemView.context.getColor(R.color.red))
                subtitleText.visibility = View.GONE
            }
        }
    }
}