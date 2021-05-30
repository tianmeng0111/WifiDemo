package com.tm.wifidemo.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.tm.wifidemo.R
import java.io.File
import java.lang.Boolean
import java.util.*

class FileSelectActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    private val listItem: MutableList<HashMap<String, Any>> = arrayListOf()

    private lateinit var adapter: SimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_select)
        Toast.makeText(applicationContext, "选择要发送的文件", Toast.LENGTH_SHORT).show()

        listView = findViewById<ListView>(R.id.fileView)

        FilesListView(Environment.getExternalStorageDirectory().path + "/DCIM")

        adapter = SimpleAdapter(
                applicationContext,
                listItem,
                R.layout.item_view, arrayOf("image", "name", "path", "type", "parent"), intArrayOf(R.id.image, R.id.file_name, R.id.file_path, R.id.file_type, R.id.file_parent))

        listView.adapter = adapter
        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val isDirectory = view.findViewById<View>(R.id.file_type) as TextView
            val path = view.findViewById<View>(R.id.file_path) as TextView
            val name = view.findViewById<View>(R.id.file_name) as TextView
            if (Boolean.parseBoolean(isDirectory.text.toString())) {
                FilesListView(path.text.toString())
                adapter.notifyDataSetChanged()
            } else {
                val intent = Intent()
                intent.putExtra("FileName", name.text.toString())
                intent.putExtra("FilePath", path.text.toString())
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun FilesListView(selectedPath: String) {
        val selectedFile = File(selectedPath)
        if (selectedFile.canRead()) {
            val file = selectedFile.listFiles()
            listItem.clear()
            for (i in file.indices) {
                val map = HashMap<String, Any>()
                map["image"] = if (file[i].isDirectory) R.mipmap.folder else R.mipmap.file
                map["name"] = file[i].name
                map["path"] = file[i].path
                map["type"] = file[i].isDirectory
                map["parent"] = file[i].parent
                listItem.add(map)
            }
            //判断有无父目录，增加返回上一级目录菜单
            if (selectedFile.parent != null) {
                val map = java.util.HashMap<String, Any>()
                map["name"] = "返回上一级目录"
                map["path"] = selectedFile.parent
                map["type"] = true
                map["parent"] = selectedFile.parent
                listItem.add(0, map)
            }
        } else {
            Toast.makeText(applicationContext, "该目录不能读取", Toast.LENGTH_SHORT).show()
        }
    }
}