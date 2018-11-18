package edu.carleton.baskaufj.shoppinglist.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.carleton.baskaufj.shoppinglist.AddItemDialog
import edu.carleton.baskaufj.shoppinglist.R
import edu.carleton.baskaufj.shoppinglist.ShoppingListActivity
import edu.carleton.baskaufj.shoppinglist.data.AppDatabase
import edu.carleton.baskaufj.shoppinglist.data.Item
import kotlinx.android.synthetic.main.activity_shopping_list.*
import kotlinx.android.synthetic.main.item_row.view.*

class ShoppingListAdapter : RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon = itemView.ivIcon
        val tvName = itemView.tvName
        val tvDescription = itemView.tvDescription
        val tvEstimatedPrice = itemView.tvEstimatedPrice
        val tvDollarSign = itemView.tvDollarSign
        val cbBought = itemView.cbBought
        val btnDelete = itemView.btnDelete
    }

    var items = mutableListOf<Item>()

    val context : Context

    constructor(context: Context, items: List<Item>) : super() {
        this.context = context
        this.items.addAll(items)

        //if the shopping list is not empty, hide the label saying it is empty
        updateTvNoItemsVisibility()
    }

    constructor(context: Context) : super() {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.item_row, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        linkItemPropertiesToViewHolder(holder, item)

        //update item's bought property based on checkbox value
        holder.cbBought.setOnClickListener {
            item.bought = holder.cbBought.isChecked

            //update item in the database
            Thread {
                AppDatabase.getInstance(context).shoppingListDao().updateItem(item)
            }.start()
        }

        holder.btnDelete.setOnClickListener {
            deleteItem(holder.adapterPosition)
        }

        //if you click anywhere on the card, edit the item
        holder.itemView.setOnClickListener {
            (context as ShoppingListActivity).showEditItemDialog(item, holder.adapterPosition)
        }
    }

    private fun linkItemPropertiesToViewHolder(holder: ViewHolder, item: Item) {
        holder.tvName.text = item.name
        holder.tvDescription.text = item.itemDescription
        holder.tvEstimatedPrice.text = item.estimatedPrice
        holder.cbBought.isChecked = item.bought

        updateEstPriceViewVisibility(item, holder)
        chooseIconForCategory(item, holder)
    }

    //if no estimated price given for the item, hide the estimated price and dollar sign views
    private fun updateEstPriceViewVisibility(item: Item, holder: ViewHolder) {
        if (item.estimatedPrice == "") {
            holder.tvEstimatedPrice.visibility = View.GONE
            holder.tvDollarSign.visibility = View.GONE
        } else {
            holder.tvEstimatedPrice.visibility = View.VISIBLE
            holder.tvDollarSign.visibility = View.VISIBLE
        }
    }

    //update the icon of the shopping list item based on item category
    private fun chooseIconForCategory(item: Item, holder: ViewHolder) {
        if (item.category == AddItemDialog.FOOD) {
            holder.ivIcon.setImageResource(R.drawable.fork_knife)
        } else if (item.category == AddItemDialog.CLOTHES) {
            holder.ivIcon.setImageResource(R.drawable.clothes_tag)
        } else if (item.category == AddItemDialog.ELECTRONICS) {
            holder.ivIcon.setImageResource(R.drawable.phone)
        } else if (item.category == AddItemDialog.GIFT) {
            holder.ivIcon.setImageResource(R.drawable.gift)
        } else if (item.category == AddItemDialog.ATHLETIC) {
            holder.ivIcon.setImageResource(R.drawable.dumbbell)
        } else {
            holder.ivIcon.setImageResource(R.drawable.dollar_sign)
        }
    }

    private fun deleteItem(adapterPosition: Int) {
        //remove from the database
        Thread {
            AppDatabase.getInstance(context).shoppingListDao().deleteItem(items[adapterPosition])

            //remove from the recycler view
            items.removeAt(adapterPosition)
            (context as ShoppingListActivity).runOnUiThread {
                notifyItemRemoved(adapterPosition)
                updateTvNoItemsVisibility()
            }
        }.start()
    }

    //show 'no items' label only if the shopping list is empty
    fun updateTvNoItemsVisibility() {
        if (items.size != 0) {
            (context as ShoppingListActivity).tvNoItems.visibility = View.GONE
        }

        if (items.size == 0) {
            (context as ShoppingListActivity).tvNoItems.visibility = View.VISIBLE
        }
    }

    fun addItem(item: Item) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
        //hide the 'no items' label
        if ((context as ShoppingListActivity).tvNoItems.visibility == View.VISIBLE) {
            (context as ShoppingListActivity).tvNoItems.visibility = View.GONE
        }
    }

    fun updateItem(item: Item, idx: Int) {
        items[idx] = item
        notifyItemChanged(idx)
    }

    fun removeAll() {
        Thread {
            //delete all from database
            AppDatabase.getInstance(context).shoppingListDao().deleteAll()

            //update the recycler view
            items.clear()
            (context as ShoppingListActivity).runOnUiThread {
                notifyDataSetChanged()

                //show the 'no items' label
                (context as ShoppingListActivity).tvNoItems.visibility = View.VISIBLE
            }
        }.start()
    }

    fun clearChecked() {
        Thread {
            //delete checked items from database
            AppDatabase.getInstance(context).shoppingListDao().deleteAllBought()

            //update the recycler view
            items.clear()
            items.addAll(AppDatabase.getInstance(context).shoppingListDao().findAllItems())

            (context as ShoppingListActivity).runOnUiThread {
                notifyDataSetChanged()
                updateTvNoItemsVisibility()
            }
        }.start()
    }


}