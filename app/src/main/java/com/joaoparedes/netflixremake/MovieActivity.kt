package com.joaoparedes.netflixremake

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joaoparedes.netflixremake.model.Category
import com.joaoparedes.netflixremake.model.Movie
import com.joaoparedes.netflixremake.model.MovieDetail
import com.joaoparedes.netflixremake.util.DownloadImageTask
import com.joaoparedes.netflixremake.util.MovieDetailTask
import com.squareup.picasso.Picasso
import java.lang.IllegalStateException

class MovieActivity : AppCompatActivity(), MovieDetailTask.Callback {

    private lateinit var progress: ProgressBar
    private lateinit var adapter: MovieAdapter
    private lateinit var txtTitle: TextView
    private lateinit var txtDesc: TextView
    private lateinit var txtCast: TextView

    private val movies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        progress = findViewById(R.id.movie_progress)
        txtTitle = findViewById(R.id.movie_txt_title)
        txtDesc = findViewById(R.id.movie_txt_desc)
        txtCast = findViewById(R.id.movie_txt_cast)
        adapter = MovieAdapter(movies, R.layout.movie_item_similar)

        val rv: RecyclerView = findViewById(R.id.movie_rv_similar)

        val id = intent?.getIntExtra("id", 0) ?: throw IllegalStateException("ID não foi encontrado!")

        MovieDetailTask(this).execute("https://api.tiagoaguiar.co/netflixapp/movie/$id?apiKey=01271260-5f5b-474c-a8f0-0976a5352172")

        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = adapter

        val toolbar: Toolbar = findViewById(R.id.movie_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null
        



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }


    override fun onResult(movieDetail: MovieDetail) {
        progress.visibility = View.GONE

        txtTitle.text = movieDetail.movie.title
        txtDesc.text = movieDetail.movie.desc
        txtCast.text = getString(R.string.cast, movieDetail.movie.cast)


        this.movies.clear()
        this.movies.addAll(movieDetail.similars)
        adapter.notifyDataSetChanged()

        DownloadImageTask(object  : DownloadImageTask.Callback {
            override fun onResult(bitmap: Bitmap) {
                val layerDrawable: LayerDrawable = ContextCompat.getDrawable(this@MovieActivity, R.drawable.shadows) as LayerDrawable
                val movieCover = BitmapDrawable(resources, bitmap)
                layerDrawable.setDrawableByLayerId(R.id.cover_drawable, movieCover)
                val coverImg: ImageView = findViewById(R.id.movie_img)
                coverImg.setImageDrawable(layerDrawable)

            }
        }).execute(movieDetail.movie.coverUrl)
    }

    override fun onFailure(message: String) {
        progress.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}