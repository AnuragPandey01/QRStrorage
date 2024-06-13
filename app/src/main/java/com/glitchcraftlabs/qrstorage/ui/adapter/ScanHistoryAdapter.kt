package com.glitchcraftlabs.qrstorage.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.ScanHistoryItemViewBinding

class ScanHistoryAdapter(
    private val scanHistoryList: List<ScanHistory>,
    private val limit: Int = scanHistoryList.size
): RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_history_item_view,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = minOf(scanHistoryList.size,limit)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = scanHistoryList[position]
        holder.binding.apply {
            // TODO: Set the data to the view
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding = ScanHistoryItemViewBinding.bind(itemView)
    }

}

// Temporary data class for testing
data class ScanHistory(
    val scanId: String,
    val scanData: String,
    val scanTime: String,
    val scanType: String,
    val tag: String
)