package com.example.appchat.`interface`

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.appchat.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomDialogFragment: BottomSheetDialogFragment(){
    private var mapView : MapView? = null
    private var btnShareLocation: Button? = null
    private var listener: OnButtonClickListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.map_bottom_dialog, container, false)
        mapView = view.findViewById(R.id.mapV)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { googleMap ->
            // Truy vấn và hiển thị marker, zoom tùy ý
        }

        btnShareLocation = view.findViewById(R.id.btnShareLocation)

        btnShareLocation?.setOnClickListener {
            listener?.onButtonClicked()
            dismiss()
        }
        return view
    }
    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        this.listener = listener
    }

    // Interface callback
    interface OnButtonClickListener {
        fun onButtonClicked()
    }

}