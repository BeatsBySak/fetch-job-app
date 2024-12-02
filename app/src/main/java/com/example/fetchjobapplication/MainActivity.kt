package com.example.fetchjobapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.items.observe(this) { items ->
            recyclerView.adapter = ItemAdapter(items)
        }

        viewModel.loadItems()
    }
}

class MainViewModel : ViewModel() {
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> get() = _items

    fun loadItems() {
        viewModelScope.launch {
            try {
                val fetchedItems = ApiClient.service.fetchItems()
                val filteredItems = fetchedItems.filter { !it.name.isNullOrBlank() }
                    .sortedWith(compareBy({ it.listId }, { it.name }))
                _items.postValue(filteredItems)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class Item (
    val id: Int,
    val listId: Int,
    val name: String?
)

interface ApiService {
    @GET("hiring.json")
    suspend fun fetchItems(): List<Item>
}

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fetch-hiring.s3.amazonaws.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}

class ItemAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listIdText: TextView = view.findViewById(R.id.listIdText)
        val nameText: TextView = view.findViewById(R.id.nameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.listIdText.text = "List ID: ${item.listId}"
        holder.nameText.text = "Name: ${item.name}"

        // Add fade-in effect
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(500).start()
    }

    override fun getItemCount(): Int = items.size
}