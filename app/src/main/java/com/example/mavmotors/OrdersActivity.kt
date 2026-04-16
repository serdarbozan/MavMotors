package com.example.mavmotors

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_orders)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.ordersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Placeholder - orders will be implemented later
        findViewById<TextView>(R.id.emptyOrdersText).visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
}