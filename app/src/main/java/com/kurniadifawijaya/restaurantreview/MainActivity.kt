package com.kurniadifawijaya.restaurantreview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kurniadifawijaya.restaurantreview.data.response.CustomerReviewsItem
import com.kurniadifawijaya.restaurantreview.data.response.PostReviewResponse
import com.kurniadifawijaya.restaurantreview.data.response.Restaurant
import com.kurniadifawijaya.restaurantreview.data.response.RestaurantResponse
import com.kurniadifawijaya.restaurantreview.data.retrofit.ApiConfig
import com.kurniadifawijaya.restaurantreview.databinding.ActivityMainBinding
import com.kurniadifawijaya.restaurantreview.databinding.ItemReviewBinding
import com.kurniadifawijaya.restaurantreview.ui.ReviewAdapter
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object{
        private const val TAG = "MainActivity"
        private const val RESTAURANT_ID = "uewq1zg2zlskfw1e867"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val layoutManager = LinearLayoutManager(this)
        binding.rvReview.layoutManager = layoutManager

        val itemDescription = DividerItemDecoration(this,layoutManager.orientation)
        binding.rvReview.addItemDecoration(itemDescription)

        findRestaurant()

        //Kirim Review
        binding.btnSend.setOnClickListener { view ->
            postReview(binding.edReview.text.toString())
            val  imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun findRestaurant(){
        //tampilkan loading
        showLoading(true)

        val client = ApiConfig.getApiService().getRestaurant(RESTAURANT_ID)
        client.enqueue(object : retrofit2.Callback<RestaurantResponse> {
            override fun onResponse(
                call: Call<RestaurantResponse>,
                response: Response<RestaurantResponse>
            ) {
                showLoading(false)
                if (response.isSuccessful){
                    val responseBody = response.body()
                    if (responseBody !=null){
                        setRestaurantData(responseBody.restaurant)
                        setReviewData(responseBody.restaurant.customerReviews)
                    }
                } else {
                    Log.e(TAG,"onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RestaurantResponse>, t: Throwable) {
                showLoading(false)
                Log.e(TAG,"onFailure: ${t.message}")
            }

        })
    }
    private fun setReviewData (consumereview : List<CustomerReviewsItem>){
        val adapter = ReviewAdapter()
        adapter.submitList(consumereview)
        binding.rvReview.adapter = adapter
        binding.edReview.setText("")

    }
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else{
            binding.progressBar.visibility = View.GONE
        }

    }

    private fun setRestaurantData(restaurant : Restaurant) {
        binding.tvTitle.text = restaurant.name
        binding.tvDescription.text = restaurant.description

        Glide.with(this)
            .load("https://restaurant-api.dicoding.dev/images/large/${restaurant.pictureId}")
            .into(binding.vPicture)

    }
    private fun  postReview(review : String) {
        showLoading(false)
        val client = ApiConfig.getApiService().postReview(RESTAURANT_ID,"Kurnia Difa Wijaya - 312010024",review)
        client.enqueue(object : retrofit2.Callback<PostReviewResponse>{
            override fun onResponse(
                call: Call<PostReviewResponse>,
                response: Response<PostReviewResponse>
            ) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody !=null){
                    setReviewData(responseBody.customerReviews)
                } else{
                    Log.e(TAG,"onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PostReviewResponse>, t: Throwable) {
                showLoading(false)
                Log.e(TAG,"onFailure: ${t.message}")
            }


        })

    }
}