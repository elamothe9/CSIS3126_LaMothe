package com.example.myapplication
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class BattingAdapter(
    private val stats: List<BattingStat>
) : RecyclerView.Adapter<BattingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val pa: TextView = view.findViewById(R.id.tvPA)
        val ab: TextView = view.findViewById(R.id.tvAB)
        val hits: TextView = view.findViewById(R.id.tvH)
        val avg: TextView = view.findViewById(R.id.tvAVG)
        val runs: TextView = view.findViewById(R.id.tvR)
        val bb: TextView = view.findViewById(R.id.tvBB)
        val so: TextView = view.findViewById(R.id.tvSO)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_batting_stat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = stats.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = stats[position]

        holder.name.text = player.name
        holder.pa.text = player.pa.toString()
        holder.ab.text = player.ab.toString()
        holder.hits.text = player.hits.toString()
        holder.runs.text = player.runs.toString()
        holder.bb.text = player.walks.toString()
        holder.so.text = player.strikeouts.toString()

        // Batting Average
        val avg = if (player.ab > 0)
            player.hits.toDouble() / player.ab
        else 0.0

        holder.avg.text = String.format("%.3f", avg)
    }
}