package com.geckour.homeapi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geckour.homeapi.R
import com.geckour.homeapi.databinding.ActivityMainBinding
import com.geckour.homeapi.databinding.ItemRequestBinding
import com.geckour.homeapi.model.RequestData
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.VERTICAL,
                false
            ).apply { stackFromEnd = true }

            layoutManager = linearLayoutManager
            addItemDecoration(DividerItemDecoration(this@MainActivity, linearLayoutManager.orientation))

            adapter = Adapter(viewModel.items)
        }
        binding.requestEnvironmental.setOnClickListener { viewModel.requestEnvironmentalData() }
        binding.loadingIndicator.setOnClickListener {
            viewModel.cancelPendingRequest()
            toggleLoadingIndicator(false)
        }

        viewModel.data.observe(this) {
            toggleLoadingIndicator(it.isLoading)

            it.error?.let { t ->
                Snackbar.make(binding.root, t.message.orEmpty(), Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK") {}
                    .show()
            }

            it.environmentalData?.let { data ->
                AlertDialog
                    .Builder(this)
                    .setTitle("📡 環境値")
                    .setMessage("気温: ${data.temperature} [℃]\n湿度: ${data.humidity} [%]\n気圧: ${data.pressure} [hPa]\n照度: ${data.illuminance} [lux]")
                    .setCancelable(true)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    private fun toggleLoadingIndicator(visibility: Boolean) {
        binding.loadingIndicator.visibility = if (visibility) View.VISIBLE else View.GONE
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