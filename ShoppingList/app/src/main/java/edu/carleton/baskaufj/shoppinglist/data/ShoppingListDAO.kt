package edu.carleton.baskaufj.shoppinglist.data

import android.arch.persistence.room.*

@Dao
interface ShoppingListDAO {

    @Query("SELECT * FROM item")
    fun findAllItems(): List<Item>

    //returns the ID of the item inserted
    @Insert
    fun insertItem(item: Item) : Long

    @Delete
    fun deleteItem(item: Item)

    @Update
    fun updateItem(item: Item)

    @Query("DELETE FROM item")
    fun deleteAll()

    @Query("DELETE FROM item WHERE bought")
    fun deleteAllBought()
}