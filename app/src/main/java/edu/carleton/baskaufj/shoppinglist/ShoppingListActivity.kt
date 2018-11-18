package edu.carleton.baskaufj.shoppinglist

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import edu.carleton.baskaufj.shoppinglist.R.id.*
import edu.carleton.baskaufj.shoppinglist.adapter.ShoppingListAdapter
import edu.carleton.baskaufj.shoppinglist.data.AppDatabase
import edu.carleton.baskaufj.shoppinglist.data.Item
import kotlinx.android.synthetic.main.activity_shopping_list.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class ShoppingListActivity : AppCompatActivity(), AddItemDialog.ItemHandler {

    companion object {
        val KEY_ITEM_TO_EDIT = "KEY_ITEM_TO_EDIT"
        val KEY_FIRST = "KEY_FIRST"
    }

    lateinit var shoppingListAdapter: ShoppingListAdapter

    //remember which item is under edit mode
    private var editIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        setSupportActionBar(toolbar)

        fabAddItem.setOnClickListener { view ->
            showAddItemDialog()
        }

        initRecyclerView()

        //show a tutorial the first time the user opens the app
        if (isFirstStart()) {
            MaterialTapTargetPrompt.Builder(this)
                    .setTarget(R.id.fabAddItem)
                    .setPrimaryText(getString(R.string.tutorial_header))
                    .setSecondaryText(getString(R.string.tutorial_body))
                    .show()

            //remember that it will no longer be the first time the user opened the app
            saveStart()
        }

    }

    //if saveStart() has not yet been called (i.e. it is the first time the app has been opened), KEY_FIRST = true
    fun isFirstStart() : Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        return sp.getBoolean(KEY_FIRST, true)
    }

    //after starting for the first time, KEY_FIRST will be false
    fun saveStart() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putBoolean(KEY_FIRST, false)
        editor.apply()
    }

    private fun initRecyclerView() {
        Thread {
            val items = AppDatabase.getInstance(this@ShoppingListActivity).shoppingListDao().findAllItems()
            //add the items loaded from database
            shoppingListAdapter = ShoppingListAdapter(this@ShoppingListActivity, items)

            runOnUiThread {
                recyclerShopList.adapter = shoppingListAdapter
            }
        }.start()
    }

    //open a dialog with empty fields to create a new item
    private fun showAddItemDialog() {
        AddItemDialog().show(supportFragmentManager, "TAG_CREATE")
    }

    //load values into the dialog fields based on current Item values
    public fun showEditItemDialog(itemToEdit: Item, idx: Int) {
        editIndex = idx
        val editItemDialog = AddItemDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEM_TO_EDIT, itemToEdit)
        editItemDialog.arguments = bundle

        editItemDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the toolbar
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handles toolbar item clicks
        when (item.itemId) {
            R.id.action_clear_checked -> {
                shoppingListAdapter.clearChecked()
            }
            R.id.action_delete_all -> {
                shoppingListAdapter.removeAll()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun itemCreated(item: Item) {
        Thread {
            val id = AppDatabase.getInstance(this).shoppingListDao().insertItem(item)
            //update the item's id based on the generated id value so that we can access this item later
            item.itemId = id

            runOnUiThread {
                //add the item object to the recycler view
                shoppingListAdapter.addItem(item)
            }
        }.start()
    }

    override fun itemUpdated(item: Item) {
        val dbThread = Thread {
            //update in the database
            AppDatabase.getInstance(this@ShoppingListActivity).shoppingListDao().updateItem(item)

            //update in the recycler view
            runOnUiThread { shoppingListAdapter.updateItem(item, editIndex) }
        }
        dbThread.start()
    }
}
