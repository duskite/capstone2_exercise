package com.mju.exercise.Calendar

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import com.mju.exercise.model.Memo
import java.util.*
import kotlin.collections.ArrayList

class DBLoader(context: Context ) {

    private val context = context
    private var db: DBHelper

    init {
        db = DBHelper(context);
    }

    fun save(title:String, memo: String, getCalendar: Calendar?) {
        val calendar :Calendar
        if(getCalendar == null){
            calendar = Calendar.getInstance()
        }else {
            calendar = getCalendar
        }
        val contentValues = ContentValues()
        contentValues.put("title", title)
        contentValues.put("memo", memo)
        contentValues.put("datetime", calendar.timeInMillis)

        db.writableDatabase.insert("note", null, contentValues)
        db.close()
        Toast.makeText(context, "저장됨",Toast.LENGTH_SHORT).show()
    }
    fun delete(id:Int) {
        db.writableDatabase.delete("note", "id=?", arrayOf(id.toString()))
        db.close()
        Toast.makeText(context, "삭제됨",Toast.LENGTH_SHORT).show()
    }

    fun update(id: Int, title: String, memo: String){

        val contentValues = ContentValues()
        contentValues.put("title",title)
        contentValues.put("memo",memo)
        db.writableDatabase.update("note", contentValues, "id=?", arrayOf(id.toString()))
        db.close()
    }

    @SuppressLint("Range")
    fun memoList(datetime : Long?) : kotlin.collections.ArrayList<Memo> {
        val array = ArrayList<Memo>()
        var sql = ""
        if(datetime == null){
            sql = "select * from note order by datetime desc"
        }else{
            sql=  "select * from note where datetime like '%"+ datetime + "%' order by datetime desc"
        }
        val cursor = db.readableDatabase.rawQuery(sql, null)
        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val title = cursor.getString(cursor.getColumnIndex("title"))
            val memo = cursor.getString(cursor.getColumnIndex("memo"))
            val getDatetime = cursor.getLong(cursor.getColumnIndex("datetime"))
            val memoItem = Memo(id, title, memo, getDatetime)
            array.add(memoItem)
        }

        return array
    }

}