package com.mju.exercise.Calendar

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mju.exercise.R
import com.mju.exercise.model.Memo
import java.util.Calendar

class MemoActivity : AppCompatActivity() {

    private lateinit var edit_title: EditText
    private lateinit var edit_memo: EditText
    private var item: Memo? = null
    private var date: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_memo)
        edit_memo = findViewById(R.id.edit_memo)
        edit_title = findViewById(R.id.edit_title)

        date = intent!!.getStringExtra("date")
        item = intent.getSerializableExtra("item") as Memo?

        if(item != null){

            edit_memo.setText(item?.memo)
            edit_title.setText(item?.title)
        }

        var Button_save = findViewById<Button>(R.id.btn_memo_save)
        var Button_delete = findViewById<Button>(R.id.btn_memo_delete)


        Button_save.setOnClickListener(View.OnClickListener {
            val title = edit_title.text.toString()
            val memo = edit_memo.text.toString()
            if(!memo.equals("")) {
                var calendar : Calendar? = null
                if(date != null){
                    calendar = Calendar.getInstance()
                    val date = this.date!!.split("/")
                    calendar.set(date[0].toInt(), date[1].toInt() , date[2].toInt())
                }
                if(this.item != null){
                    DBLoader(applicationContext).update(this.item!!.id, title, memo)
                    finish()
                }else {
                    DBLoader(applicationContext).save(title, memo, calendar)
                    finish()
                }

            }

        })
        if(this.item == null){
            Button_delete.isVisible=false
        }
        Button_delete.setOnClickListener {
            if(this.item != null){
                DBLoader(applicationContext).delete(this.item!!.id)
                finish()
            }

        }




    }
}