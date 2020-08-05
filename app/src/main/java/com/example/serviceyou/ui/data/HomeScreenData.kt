package com.example.serviceyou.ui.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.serviceyou.BR

/**
 * @author Divya Khanduri
 */
data class HomeScreenData(@Bindable var _routineName: String) : BaseObservable() {
    var routineName: String?
        @Bindable get() = _routineName
        set(value) {
            _routineName = value!!
            notifyPropertyChanged(BR.routineName)
        }
}