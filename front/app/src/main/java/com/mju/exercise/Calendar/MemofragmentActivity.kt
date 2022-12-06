package com.mju.exercise.Calendar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mju.exercise.R

class MemofragmentActivity : AppCompatActivity() {

    private lateinit var adapter : MemoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memofragment)




        val btnAdd : FloatingActionButton = findViewById<FloatingActionButton>(R.id.btn_add)
        btnAdd.setOnClickListener {
            startActivity(Intent(applicationContext, MemoActivity::class.java))
        }

        adapter = MemoAdapter(applicationContext)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.setList(DBLoader(applicationContext).memoList(null))
    }


}