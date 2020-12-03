package com.geckour.homeapi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geckour.homeapi.R
import com.geckour.homeapi.databinding.ActivityMainBinding
import com.geckour.homeapi.databinding.ItemRequestBinding
import com.geckour.homeapi.model.RequestData

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.recyclerView.apply {
            val layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            this.layoutManager = layoutManager
            addItemDecoration(DividerItemDecoration(this@MainActivity, layoutManager.orientation))
            adapter = Adapter(viewModel.items)
        }
    }

    private class Adapter(private val items: List<RequestData>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_request,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(items[holder.adapterPosition])
        }

        override fun getItemCount(): Int = items.size

        private class ViewHolder(private val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root) {

            fun onBind(item: RequestData) {
                binding.apply {
                    requestData = item
                    root.setOnClickListener { item.onClick() }
                }
            }
        }
    }
}