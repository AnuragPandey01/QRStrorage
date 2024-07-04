package com.glitchcraftlabs.qrstorage.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdOptionsView
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdListener
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.databinding.AdLayoutBinding
import com.glitchcraftlabs.qrstorage.databinding.ScanHistoryItemViewBinding
import com.glitchcraftlabs.qrstorage.util.Constants.AD_FREQ
import com.glitchcraftlabs.qrstorage.util.Constants.AD_PLACEMENT_ID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryAdapter(
    private var scanHistoryList: List<History>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object{
        const val AD_TYPE = 1
        const val ITEM_TYPE = 0
    }

    private val simpleDateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private var onItemClick : (History) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == AD_TYPE){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ad_layout,parent,false)
            return AdViewHolder(view)
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_history_item_view,parent,false)
        return ItemViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if(position % AD_FREQ == 0 && position != 0) AD_TYPE else ITEM_TYPE
    }

    override fun getItemCount() = scanHistoryList.size + scanHistoryList.size/AD_FREQ

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder is AdViewHolder){
            holder.bind()
            return
        }
        if(holder is ItemViewHolder){
            val realPosition = position - position/AD_FREQ
            val current = scanHistoryList[realPosition]
            holder.binding.apply {
                tvTag.text = current.tag
                tvHistoryType.text = if(current.isGenerated!!) "generated" else "scanned"
                val date = simpleDateTimeFormatter.format(Date(current.createdAt!!))
                tvDate.text = date
                qrType.text = if(current.isFile!!) "File" else "Text"
                root.setOnClickListener {
                    onItemClick(current)
                }
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

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding = ScanHistoryItemViewBinding.bind(itemView)
    }

    inner class AdViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding = AdLayoutBinding.bind(itemView)

        fun bind(){
            val nativeAd = NativeAd(itemView.context, AD_PLACEMENT_ID)
            nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                    .withAdListener(
                        object : NativeAdListener {
                            override fun onAdClicked(ad: Ad?) {}

                            override fun onMediaDownloaded(ad: Ad?) {}

                            override fun onError(ad: Ad?, adError: AdError?) {
                                binding.root.removeAllViews()
                            }

                            override fun onAdLoaded(ad: Ad?) {
                                if(nativeAd != ad) return;
                                binding.nativeAdTitle.text = nativeAd.advertiserName
                                binding.nativeAdBody.text = nativeAd.adBodyText
                                binding.nativeAdSocialContext.text = nativeAd.adSocialContext
                                binding.nativeAdCallToAction.text = nativeAd.adCallToAction
                                binding.nativeAdSponsoredLabel.text = nativeAd.sponsoredTranslation
                                binding.nativeAdCallToAction.text = nativeAd.adCallToAction
                                binding.nativeAdCallToAction.visibility = if(nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE

                                val adOptionsView = AdOptionsView(
                                    itemView.context,
                                    nativeAd,
                                    binding.root
                                )
                                binding.adChoicesContainer.removeAllViews()
                                binding.adChoicesContainer.addView(adOptionsView)

                                nativeAd.registerViewForInteraction(
                                    binding.root,
                                    binding.nativeAdMedia,
                                    binding.nativeAdIcon,
                                    listOf(
                                        binding.nativeAdIcon,
                                        binding.nativeAdMedia,
                                        binding.nativeAdCallToAction
                                    )
                                )
                            }

                            override fun onLoggingImpression(ad: Ad?) {}
                        }
                    ).build()
            )
        }
    }

}



