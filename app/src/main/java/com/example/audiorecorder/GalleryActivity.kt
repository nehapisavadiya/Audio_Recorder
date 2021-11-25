package com.example.audiorecorder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    lateinit var deleteRecord : AudioRecord
    private lateinit var records : ArrayList<AudioRecord>
    private lateinit var oldRecords : ArrayList<AudioRecord>
    private lateinit var myAdapter : Adapter
    private lateinit var db : AppDatabase

    private lateinit var searchInput : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        records = ArrayList()

        db = Room.databaseBuilder(this,AppDatabase::class.java,"audioRecords").build()

        myAdapter = Adapter(records, this)

        recyclerview.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(context)
        }
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
        {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteRecords(records[viewHolder.adapterPosition])
            }

        }
        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerview)

        fetchAll()

        searchInput = findViewById(R.id.search_input)
        searchInput.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var query = s.toString()
                searchDatabase(query)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun deleteRecords(audioRecord: AudioRecord) {
        deleteRecord = audioRecord
        oldRecords = records

        GlobalScope.launch {
            db.audioRecordDao().delete(audioRecord)

            records = records.filter { it.id != audioRecord.id } as ArrayList<AudioRecord>
            runOnUiThread {
                myAdapter.setData(records)
                showSnackBar()

            }
        }


    }

    private fun showSnackBar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view,"Record deleted!!", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()

        }.setActionTextColor(ContextCompat.getColor(this,R.color.red))
            .setTextColor(ContextCompat.getColor(this,R.color.white)).show()
    }

    private fun undoDelete() {
        GlobalScope.launch {
            db.audioRecordDao().insert(deleteRecord)
            records = oldRecords
            runOnUiThread {
                myAdapter.setData(records)

            }
        }
    }

    private fun searchDatabase(query: String) {
        GlobalScope.launch {
            records.clear()
            var queryResult = db.audioRecordDao().searchDatabase("%$query%")
            records.addAll(queryResult)

            runOnUiThread {
                myAdapter.notifyDataSetChanged()
            }


        }
    }

    private fun fetchAll(){
        GlobalScope.launch {
            records.clear()
            var queryResult = db.audioRecordDao().getAll()
            records.addAll(queryResult)

            myAdapter.notifyDataSetChanged()
        }
    }

    override fun onItemClickListener(position: Int) {
        var audioRecord = records[position]
        var intent = Intent(this,AudioPlayerActivity::class.java)

        intent.putExtra("filepath", audioRecord.filepath)
        intent.putExtra("filename",audioRecord.filename)
        startActivity(intent)
    }

    override fun onItemLongClickListener(position: Int) {
        Toast.makeText(this,"long click",Toast.LENGTH_LONG).show()
    }
}