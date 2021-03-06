package com.jaax.edsa.controlador

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaax.edsa.R
import com.jaax.edsa.controlador.*
import com.jaax.edsa.modelo.Cuenta
import com.jaax.edsa.modelo.DBHelper
import com.jaax.edsa.modelo.Email
import kotlinx.android.synthetic.main.show_accounts.*
import kotlinx.android.synthetic.main.show_emails.*
import java.lang.ClassCastException
import java.sql.SQLException

class VerCuentas(emailElegido: Email): DialogFragment() {
    private lateinit var db: DBHelper
    private lateinit var addCuenta: FloatingActionButton
    private lateinit var listaCuentas: ListView
    private lateinit var txtNoAccount: TextView
    private lateinit var imgNoAccount: ImageView
    private lateinit var toast: Toast
    private lateinit var cuentaAdapter: CuentaAdapter
    private lateinit var callBack: OnCallbackReceivedView
    private var emailActual: Email

    init {
        emailActual = emailElegido
    }

    interface OnCallbackReceivedView {
        fun refreshByViewing()
    }

    @SuppressLint("ShowToast")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.show_accounts, null)
        val builder = AlertDialog.Builder(activity)

        addCuenta = view.findViewById(R.id.account_add)
        listaCuentas = view.findViewById(R.id.accounts_lista)
        txtNoAccount = view.findViewById(R.id.txtNoAccount)
        imgNoAccount = view.findViewById(R.id.imgNoAccount)
        db = DBHelper(activity!!.applicationContext, DBHelper.nombreDB, null, DBHelper.version)
        toast = Toast.makeText(activity!!.applicationContext, "txt", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
        builder.setView(view)

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        mostrarCuentas()
        MobileAds.initialize( activity!!.applicationContext )
        val adRequest = AdRequest.Builder().build()
        adview3.loadAd( adRequest )
    }

    override fun onResume() {
        super.onResume()
        onClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        try{
            callBack = context as OnCallbackReceivedView
        }catch (cce: ClassCastException){cce.printStackTrace()}
        callBack.refreshByViewing()
    }

    private fun onClick() {
        addCuenta.setOnClickListener {
            AddCuentaFragment(emailActual).show(
                this@VerCuentas.activity!!.supportFragmentManager, "addAccount"
            )
            this@VerCuentas.dismiss()
        }
        listaCuentas.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, pos, _ ->
            view?.isSelected = true
            val popupMenu = PopupMenu(this@VerCuentas.context, view)
            popupMenu.menuInflater.inflate(R.menu.opc_cuenta, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.menu_edacc -> {
                        val updt = UpdateCuentaFragment(emailActual.cuentas[pos], emailActual)
                        updt.show(this@VerCuentas.activity!!.supportFragmentManager, "updateCuenta")
                        this@VerCuentas.dismiss()
                    }
                    R.id.menu_delacc -> {
                        val del = DeleteCuentaFragment(emailActual.cuentas[pos], emailActual)
                        del.show(this@VerCuentas.activity!!.supportFragmentManager, "delCuenta")
                        this@VerCuentas.dismiss()
                    }
                }
                true
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                popupMenu.gravity = Gravity.CENTER_HORIZONTAL
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true)
            }
            popupMenu.show()
            true
        }
    }

    private fun mostrarCuentas(){
        val allCuentas = ArrayList<Cuenta>()
        try {
            val cursor = db.getCuentasById(emailActual.nombre)
            if( cursor.count>0 ){
                while(cursor.moveToNext()){
                    val cuenta = Cuenta(
                        emailActual.nombre,
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                        )
                    allCuentas.add(cuenta)
                }
                emailActual = setMissingDataCuenta(emailActual)
                txtNoAccount.visibility = View.GONE
                imgNoAccount.visibility = View.GONE
            }
            cuentaAdapter = CuentaAdapter(activity!!.applicationContext, allCuentas)
            listaCuentas.adapter = cuentaAdapter
        }catch (sql: SQLException){}
    }

    private fun setMissingDataCuenta( currentEmail: Email ): Email {

        val cursor = db.getCuentasById(currentEmail.nombre)
        val listaCuentas = ArrayList<Cuenta>()
        try { //no agrego 'if' xq si no tiene cuentas entonces no hay nada que cliquear
            while( cursor.moveToNext() ) {
                val cuenta = Cuenta(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
                )
                listaCuentas.add(cuenta)
                currentEmail.cuentas = listaCuentas
            }
        } catch(sqli: SQLiteException){ sqli.toString() }
        return currentEmail
    }
}