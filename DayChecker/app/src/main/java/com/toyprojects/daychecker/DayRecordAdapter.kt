package com.toyprojects.daychecker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.recyclerview.widget.RecyclerView
import com.toyprojects.daychecker.database.Record
import com.toyprojects.daychecker.databinding.RecyclerDayRecordBinding

class DayRecordAdapter: RecyclerView.Adapter<Holder>() {
    var listData = mutableListOf<Record>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RecyclerDayRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val record = listData[position]
        holder.setRecord(record)
    }

    override fun getItemCount(): Int = listData.size
}

class Holder(val binding: RecyclerDayRecordBinding): RecyclerView.ViewHolder(binding.root) {
    init {
        // option button
        binding.btnActions.setOnClickListener {
            Toast.makeText(binding.root.context, "옵션 클릭됨", Toast.LENGTH_SHORT).show()
        }
    }

    fun setRecord(record: Record) {
        binding.textRecordTime.text = record.record_time

        if (record.memo != "") {
            binding.textRecordMemo.visibility = View.VISIBLE
            binding.textRecordMemo.text = record.memo
        }
        else {
            binding.textRecordMemo.visibility = View.GONE
        }

        binding.ratingBarRecord.rating = record.rating

        when(record.condition) {
            1 -> {
                binding.recordColor.setBackgroundResource(R.color.whitegray)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.whitegray)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.black))
            }
            2 -> {
                binding.recordColor.setBackgroundResource(R.color.black)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.black)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.white))
            }
            3 -> {
                binding.recordColor.setBackgroundResource(R.color.Rred)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.Rred)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.white))
            }
            4 -> {
                binding.recordColor.setBackgroundResource(R.color.Rpink)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.Rpink)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.white))
            }
            5 -> {
                binding.recordColor.setBackgroundResource(R.color.Ryellow)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.Ryellow)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.black))
            }
            6 -> {
                binding.recordColor.setBackgroundResource(R.color.Rgreen)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.Rgreen)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.black))
            }
            7 -> {
                binding.recordColor.setBackgroundResource(R.color.Rlightblue)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.Rlightblue)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.white))
            }
            8 -> {
                binding.recordColor.setBackgroundResource(R.color.Rblue)
                binding.chipRecordState.setChipBackgroundColorResource(R.color.Rblue)
                binding.chipRecordState.setTextColor(getColorStateList(binding.root.context, R.color.white))
            }
        }

        when(record.state) {
            1 -> binding.chipRecordState.setText(R.string.record_state_first)
            2 -> binding.chipRecordState.setText(R.string.record_state_second)
            3 -> binding.chipRecordState.setText(R.string.record_state_third)
        }
    }
}