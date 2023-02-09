package com.joaoparedes.netflixremake.util;

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.joaoparedes.netflixremake.model.Movie
import com.joaoparedes.netflixremake.model.MovieDetail
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieDetailTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onPreExecute()
        fun onResult(movieDetail: MovieDetail)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()


        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null

            try {
                val requestURL = URL(url)
                urlConnection = requestURL.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 2000
                urlConnection.connectTimeout = 2000

                val statusCode = urlConnection.responseCode

                if( statusCode == 400) {
                    stream = urlConnection.errorStream
                    val jsonAsString = stream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonAsString)
                    val message = json.getString("message")
                    throw IOException(message)

                } else if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }

                stream = urlConnection.inputStream

                val jsonAsString = stream.bufferedReader().use { it.readText() }
                val movieDetail = toMovieDetail(jsonAsString)

                handler.post {
                    callback.onResult(movieDetail)
                }


            } catch (e: IOException) {
                val message = e.message ?: "erro desconhecido"
                Log.e("Teste", message, e)
                handler.post {
                    callback.onFailure(message)
                }
            } finally {
                urlConnection?.disconnect()
                stream?.close()
            }

        }
    }

    private fun toMovieDetail(jsonAsString: String) : MovieDetail {

        val json = JSONObject(jsonAsString)

        val id = json.getInt("id")
        val title = json.getString("title")
        val desc = json.getString("desc")
        val cast = json.getString("cast")
        val coverUrl = json.getString("cover_url")

        val jsonMovies = json.getJSONArray("movie")

        val similars = mutableListOf<Movie>()
        for(j in 0 until jsonMovies.length()) {
            val jsonMovie = jsonMovies.getJSONObject(j)

            val similarId = jsonMovie.getInt("id")
            val similarUrl = jsonMovie.getString("cover_url")

            similars.add(Movie(similarId,similarUrl))
        }

        val movie = Movie(id, coverUrl, title, desc, cast)
        return MovieDetail(movie, similars)
    }

}