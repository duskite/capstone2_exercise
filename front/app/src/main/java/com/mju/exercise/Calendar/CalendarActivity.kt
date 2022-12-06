package com.mju.exercise.Calendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mju.exercise.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var adapter : MemoAdapter
    private  lateinit var calendarView : CalendarView
    private var selectday = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById<CalendarView>(R.id.calendar_view)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        val button1 = findViewById<View>(R.id.btn_memo_add) as Button
        button1.setOnClickListener {
            val intent = Intent(applicationContext, MemofragmentActivity::class.java)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(applicationContext)

        adapter = MemoAdapter(applicationContext)
        recyclerView.adapter = adapter

        calendarView.setOnDateChangeListener(object  : CalendarView.OnDateChangeListener{
            override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
                if(selectday.equals(String.format("%04d/%02d/%02d",year,month +1,dayOfMonth))){
                    val date = year.toString() + "/" + month.toString() + "/" + dayOfMonth.toString()
                    val intent = Intent(applicationContext, MemoActivity::class.java)
                    intent.putExtra("date",date)
                    startActivity(intent)
                }else {
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, dayOfMonth)
                    changeList(calendar)
                    selectday =String.format("%04d/%02d/%02d",year,month +1,dayOfMonth)
                }
            }
        })


        val date = Date()
        date.time = calendarView.date
        selectday = SimpleDateFormat("yyyy/MM/dd").format(date)
        adapter.setList(
            DBLoader(applicationContext).memoList(
            calendarView.date.toString().substring(0, 6).toLong()))

    }

    override fun onResume() {
        super.onResume()
        val date = selectday.split("/")
        val calendar = Calendar.getInstance()
        calendar.set(date[0].toInt(), date[1].toInt() -1, date[2].toInt())
        changeList(calendar)

    }

    fun changeList(calendar: Calendar){
        val datetime = calendar.timeInMillis.toString().substring(0, 6)
        adapter.setList(DBLoader(applicationContext).memoList(datetime.toLong()))
    }

}