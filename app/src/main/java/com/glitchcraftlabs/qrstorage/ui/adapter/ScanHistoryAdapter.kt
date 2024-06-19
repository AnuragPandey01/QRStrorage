package com.glitchcraftlabs.qrstorage.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.databinding.ScanHistoryItemViewBinding
import com.glitchcraftlabs.qrstorage.ui.home.HomeFragmentDirections
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryAdapter(
    private var scanHistoryList: List<History>,
    private val limit: Int = Int.MAX_VALUE
): RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>(){

    private val simpleDateTimeFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var onItemClick : (History) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_history_item_view,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = minOf(scanHistoryList.size,limit)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = scanHistoryList[position]
        holder.binding.apply {
            tvTag.text = current.tag
            tvHistoryType.text = if(current.isGenerated) "generated" else "scanned"
            tvDate.text = simpleDateTimeFormatter.format(Date(current.createdAt))

            btnMore.setOnClickListener {
               onItemClick(current)
            }
        }
    }

    fun updateData(it: List<History>) {
        scanHistoryList = it
        notifyDataSetChanged()
    }

    fun setOnItemClick(onItemClick: (History) -> Unit){
        this.onItemClick = onItemClick
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding = ScanHistoryItemViewBinding.bind(itemView)
    }



}
