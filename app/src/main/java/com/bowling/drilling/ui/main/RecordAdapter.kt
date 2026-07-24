/** app/src/main/java/com/bowling/drilling/ui/main/RecordAdapter.kt – RecyclerView 어댑터 (DiffUtil 적용) */
package com.bowling.drilling.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.databinding.ItemRecordBinding

class RecordAdapter(
    private val onClick: (BowlingRecord) -> Unit,
    private val onLongClick: (BowlingRecord) -> Unit
) : ListAdapter<BowlingRecord, RecordAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    inner class ViewHolder(private val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: BowlingRecord) {
            binding.tvName.text = record.name.ifEmpty { "이름 없음" }
            binding.tvPhone.text = record.phone.ifEmpty { "전화 없음" }
            binding.tvHand.text = when (record.hand) {
                "LH" -> "왼손"
                else -> "오른손"
            }
            binding.tvDate.text = record.date.ifEmpty { "날짜 미입력" }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BowlingRecord>() {
        override fun areItemsTheSame(oldItem: BowlingRecord, newItem: BowlingRecord) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BowlingRecord, newItem: BowlingRecord) =
            oldItem == newItem
    }
}
