package com.example.gpstracker

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface

fun Activity.showDialog(
    title:String,
    postiveText:String,
    negativeText:String,
    onPostiveClickListner:DialogInterface.OnClickListener,
    onNegativeClickListner:DialogInterface.OnClickListener? = null
){
    val dialog = AlertDialog.Builder(this)
    dialog.setTitle(title)
    dialog.setPositiveButton(postiveText,onPostiveClickListner)
    if (onNegativeClickListner!= null){
        dialog.setNegativeButton(negativeText?: "", onNegativeClickListner)
    }
    dialog.show()
}