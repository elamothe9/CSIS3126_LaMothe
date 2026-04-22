package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PitchingAdapter(
    private val stats: List<PitchingStat>
) : RecyclerView.Adapter<PitchingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val ip: TextView = view.findViewById(R.id.tvIP)
        val hits: TextView = view.findViewById(R.id.tvH)
        val runs: TextView = view.findViewById(R.id.tvR)
        val ra: TextView = view.findViewById(R.id.tvRA)
        val bb: TextView = view.findViewById(R.id.tvBB)
        val k: TextView = view.findViewById(R.id.tvK)
        val whip: TextView = view.findViewById(R.id.tvWHIP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pitching_stat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = stats.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = stats[position]

        holder.name.text = p.name
        holder.ip.text = p.ip.toString()
        holder.hits.text = p.hits.toString()
        holder.runs.text = p.runs.toString()
        holder.ra.text = p.era.toString()
        holder.bb.text = p.walks.toString()
        holder.k.text = p.strikeouts.toString()
        holder.whip.text = p.whip.toString()
    }
}