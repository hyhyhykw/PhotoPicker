package com.hy.picker.core

import java.util.*

/**
 * Created time : 2018/8/27 15:43.
 *
 * @author HY
 */

data class Crystal(val res: String, val length: Int)
data class CrystalResult(val data: ArrayList<Crystal>)

data class Category(val id: Int, val name: String, val image: String)
data class CrystalCategory(val category: ArrayList<Category>)



