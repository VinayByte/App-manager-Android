package com.egnize.appmanager.helpers

import android.content.Context
import android.content.DialogInterface
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.egnize.appmanager.R

object CustomAlertDialog {
    fun showAlertDialogWithOneButton(context: Context,
                                     title: String?,
                                     message: String?,
                                     textButton: String?,
                                     buttonListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(textButton, buttonListener)
        val dialog = builder.create()
        dialog.show()
        val textView = dialog.findViewById<View>(android.R.id.message) as TextView?
        textView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.alert_dialog_message_size))
    }

    fun showAlertDialogWithTwoButton(context: Context,
                                     title: String?,
                                     message: String?,
                                     textPositiveButton: String?,
                                     positveButtonListener: DialogInterface.OnClickListener?,
                                     textNegativeButton: String?,
                                     negativeButtonListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(textPositiveButton, positveButtonListener)
        builder.setNegativeButton(textNegativeButton, negativeButtonListener)
        val dialog = builder.create()
        dialog.show()
        val textView = dialog.findViewById<View>(android.R.id.message) as TextView?
        textView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.alert_dialog_message_size))
    }

    fun showProgressDialog(context: Context?, title: String?): AlertDialog {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setView(R.layout.dialog_progress_circular)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun stopProgressDialog(dialog: AlertDialog?) {
        if (dialog == null) return
        dialog.hide()
    }

    fun showAlertDialogWithRadio(
            context: Context?,
            title: String?,
            items: Array<String?>?,
            positionOfCheckedItem: Int,
            itemsClickListener: DialogInterface.OnClickListener?
    ): AlertDialog {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setSingleChoiceItems(items, positionOfCheckedItem, itemsClickListener)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        return dialog
    }
}