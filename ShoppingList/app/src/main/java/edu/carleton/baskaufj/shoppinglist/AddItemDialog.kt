package edu.carleton.baskaufj.shoppinglist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import edu.carleton.baskaufj.shoppinglist.data.Item
import kotlinx.android.synthetic.main.dialog_add_new_item.view.*

class AddItemDialog : DialogFragment(), AdapterView.OnItemSelectedListener {

    interface ItemHandler {
        fun itemCreated(item: Item)
        fun itemUpdated(item: Item)
    }

    private lateinit var itemHandler: ItemHandler

    //attach the ItemHandler to the dialog fragment
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        //check if the activity is implementing the interface - if it is, then link it to the dialog fragment
        if (context is ItemHandler) {
            itemHandler = context
        }
        else {
            throw RuntimeException("The Activity does not implement the ItemHandler interface")
        }
    }

    //categories to choose from
    companion object {
        val OTHER = "Other"
        val ATHLETIC = "Athletic"
        val CLOTHES = "Clothes"
        val ELECTRONICS = "Electronics"
        val FOOD = "Food"
        val GIFT = "Gift"
    }

    var categories = listOf(OTHER, ATHLETIC, CLOTHES, ELECTRONICS, FOOD, GIFT)

    var spinner: Spinner? = null
    var selectedCategory = OTHER

    private lateinit var etItemName: EditText
    private lateinit var etItemDescription: EditText
    private lateinit var etEstimatedPrice: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        initializeDialogBuilder(builder)

        //set up the categories dropdown
        val arrayAdapter = initializeSpinnerAdapter()

        val arguments = this.arguments
        //if editing an existing item, pre-fill all dialog fields with the current info about the item
        if (arguments != null && arguments.containsKey(ShoppingListActivity.KEY_ITEM_TO_EDIT)) {
            val item = arguments.getSerializable(ShoppingListActivity.KEY_ITEM_TO_EDIT) as Item
            initializeEditDialogFromExistingItem(item, arrayAdapter, builder)
        }

        builder.setPositiveButton(getString(R.string.ok)) {
            dialog, witch -> //this event handler gets set in onResume()
        }

        return builder.create()
    }

    private fun initializeDialogBuilder(builder: AlertDialog.Builder) {
        builder.setTitle(getString(R.string.new_item))

        val rootView = requireActivity().layoutInflater.inflate(R.layout.dialog_add_new_item, null)

        etItemDescription = rootView.etItemDescription
        etItemName = rootView.etItemName
        etEstimatedPrice = rootView.etEstimatedPrice

        spinner = rootView.spinnerCategory
        spinner!!.setOnItemSelectedListener(this)

        builder.setView(rootView)
    }

    private fun AddItemDialog.initializeSpinnerAdapter(): ArrayAdapter<String> {
        val arrayAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, categories)
        // Set layout to use when the list of choices appear
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        spinner!!.setAdapter(arrayAdapter)
        return arrayAdapter
    }

    private fun initializeEditDialogFromExistingItem(item: Item, arrayAdapter: ArrayAdapter<String>, builder: AlertDialog.Builder) {
        etItemName.setText(item.name)
        etItemDescription.setText(item.itemDescription)

        //set spinner selected item
        spinner!!.setSelection(arrayAdapter.getPosition(item.category))

        etEstimatedPrice.setText(item.estimatedPrice)

        builder.setTitle(getString(R.string.edit_item))
    }

    override fun onResume() {
        super.onResume()

        val positiveButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
        //update the current item being edited or create a new Item based on the values in the dialog
        positiveButton.setOnClickListener {
            if (etItemName.text.isNotEmpty()) {
                val arguments = this.arguments
                if (arguments != null && arguments.containsKey(ShoppingListActivity.KEY_ITEM_TO_EDIT)) handleItemEdit()
                else handleItemCreate()
                dialog.dismiss()
            } else {
                etItemName.error = getString(R.string.field_empty_error)
            }
        }

    }

    //create a new Item object with instance variable values extracted from the dialog
    private fun handleItemCreate() {
        itemHandler.itemCreated(
                Item(null, etItemName.text.toString(), selectedCategory, false, etItemDescription.text.toString(), etEstimatedPrice.text.toString())
        )
    }

    private fun handleItemEdit() {
        val itemToEdit = arguments?.getSerializable(
                ShoppingListActivity.KEY_ITEM_TO_EDIT
        ) as Item
        //update values of the given item instance variables from values in the dialog
        itemToEdit.name = etItemName.text.toString()
        itemToEdit.itemDescription = etItemDescription.text.toString()
        itemToEdit.category = selectedCategory

        itemToEdit.estimatedPrice = etEstimatedPrice.text.toString()

        itemHandler.itemUpdated(itemToEdit)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        selectedCategory = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
        selectedCategory = OTHER
    }
}