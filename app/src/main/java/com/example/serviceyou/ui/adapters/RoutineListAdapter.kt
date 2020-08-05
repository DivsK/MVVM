package com.example.serviceyou.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceyou.databinding.RvItemRowLayoutBinding
import com.example.serviceyou.ui.data.HomeScreenData

/**
 * @author Divya Khanduri
 */

class RoutineListAdapter(val listRoutineData: List<HomeScreenData>) :
    RecyclerView.Adapter<RoutineListAdapter.RoutineListViewHolder>() {
    inner class RoutineListViewHolder(var rvItemRowLayoutBinding: RvItemRowLayoutBinding) :
        RecyclerView.ViewHolder(rvItemRowLayoutBinding.root){
        fun bindData(homeScreenData: HomeScreenData) {
            rvItemRowLayoutBinding.homeScreenData=homeScreenData
            rvItemRowLayoutBinding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvItemRowLayoutBinding.inflate(layoutInflater,parent,false)

        return RoutineListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listRoutineData.size
    }

    override fun onBindViewHolder(holder: RoutineListViewHolder, position: Int) {
        val currentItem = listRoutineData.get(position)
        holder.bindData(currentItem)
    }
}