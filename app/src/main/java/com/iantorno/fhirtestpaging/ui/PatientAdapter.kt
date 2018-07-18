package com.iantorno.fhirtestpaging.ui

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.iantorno.fhirtestpaging.R
import com.iantorno.fhirtestpaging.repository.NetworkState
import com.iantorno.fhirtestpaging.objects.Resource
import com.iantorno.fhirtestpaging.ui.viewholder.NetworkStateViewHolder
import com.iantorno.fhirtestpaging.ui.viewholder.PatientViewHolder

class PatientAdapter(
        private val retryCallback: () -> Unit)
    : PagedListAdapter<Resource, RecyclerView.ViewHolder>(PatientDiffCallback) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.patient_card -> PatientViewHolder.create(parent)
            R.layout.item_network_state -> NetworkStateViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.patient_card -> (holder as PatientViewHolder).bind(getItem(position))
            R.layout.item_network_state -> (holder as NetworkStateViewHolder).bind(networkState)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            R.layout.patient_card
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    /**
     * Set the current network state to the adapter
     * but this work only after the initial load
     * and the adapter already have list to add new loading raw to it
     * so the initial loading state the activity responsible for handle it
     *
     * @param newNetworkState the new network state
     */
    fun setNetworkState(newNetworkState: NetworkState?) {
        if (currentList != null) {
            if (currentList!!.size != 0) {
                val previousState = this.networkState
                val hadExtraRow = hasExtraRow()
                this.networkState = newNetworkState
                val hasExtraRow = hasExtraRow()
                if (hadExtraRow != hasExtraRow) {
                    if (hadExtraRow) {
                        notifyItemRemoved(super.getItemCount())
                    } else {
                        notifyItemInserted(super.getItemCount())
                    }
                } else if (hasExtraRow && previousState !== newNetworkState) {
                    notifyItemChanged(itemCount - 1)
                }
            }
        }
    }

    companion object {
        val PatientDiffCallback = object : DiffUtil.ItemCallback<Resource>() {
            override fun areContentsTheSame(oldItem: Resource, newItem: Resource):
                    Boolean = (oldItem == newItem)

            override fun areItemsTheSame(oldItem: Resource, newItem: Resource):
                    Boolean = oldItem.id == newItem.id
        }
    }
}