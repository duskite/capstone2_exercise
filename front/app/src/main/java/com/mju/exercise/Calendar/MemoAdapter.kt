package com.mju.exercise.Calendar

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mju.exercise.R
import com.mju.exercise.model.Memo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemoAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val context = context
    private val arrayList = ArrayList<Memo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_memo, parent, false)
        return HolderView(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item : Memo = arrayList.get(position)
        val view : HolderView = holder as HolderView
        view.text_memo.setText(item.memo)
        view.text_title.setText(item.title)
        val date = Date()

        //오픈매치 참가로 생성된 달력메모 중 날짜 미정인거는 미정 표시
        if(item.datetime.toLong() == 1L){
            view.text_datetime.setText("미정")
        }else{
            date.time = item.datetime.toLong()
            view.text_datetime.setText(SimpleDateFormat("yyyy/MM/dd hh:MM:ss").format(date))
        }

        view.itemView.setOnClickListener {

            val intent = Intent(context, MemoActivity::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))

        }


    }


    fun setList(array : ArrayList<Memo>){

        arrayList.clear()
        arrayList.addAll(array)
        notifyDataSetChanged()
    }

    private class HolderView(view: View) : RecyclerView.ViewHolder(view){

        val text_title: TextView = view.findViewById(R.id.text_title)
        val text_memo: TextView = view.findViewById(R.id.text_memo)
        val text_datetime: TextView = view.findViewById(R.id.text_datetime)

    }


}