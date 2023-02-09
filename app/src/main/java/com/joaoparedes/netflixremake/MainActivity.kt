package com.joaoparedes.netflixremake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joaoparedes.netflixremake.model.Category
import com.joaoparedes.netflixremake.model.Movie
import com.joaoparedes.netflixremake.util.CategoryTask

class MainActivity : AppCompatActivity(), CategoryTask.Callback {

    private lateinit var progress: ProgressBar
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        progress = findViewById(R.id.progress_main)
        adapter = CategoryAdapter(categories) { id ->
            val intent = Intent(this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id",id)
            startActivity(intent)
        }

        val rv: RecyclerView = findViewById(R.id.rv_main)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        CategoryTask(this).execute("https://api.tiagoaguiar.co/netflixapp/home?apiKey=01271260-5f5b-474c-a8f0-0976a5352172")
    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }


    override fun onResult(categories: List<Category>) {
        progress.visibility = View.GONE
        this.categories.clear()
        this.categories.addAll(categories)
        adapter.notifyDataSetChanged()

    }

    override fun onFailure(message: String) {
        progress.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}