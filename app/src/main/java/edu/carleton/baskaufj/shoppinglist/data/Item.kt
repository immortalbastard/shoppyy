package edu.carleton.baskaufj.shoppinglist.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "item")
data class Item(
        @PrimaryKey(autoGenerate = true) var itemId: Long?,
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "category") var category: String,
        @ColumnInfo(name = "bought") var bought: Boolean,
        @ColumnInfo(name = "itemdescription") var itemDescription: String,
        @ColumnInfo(name = "estimatedprice") var estimatedPrice: String
) : Serializable