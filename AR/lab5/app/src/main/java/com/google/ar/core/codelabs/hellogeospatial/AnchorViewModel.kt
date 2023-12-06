package com.google.ar.core.codelabs.hellogeospatial
/*
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.ar.core.codelabs.hellogeospatial.model.AnchorDatabase
import com.google.ar.core.codelabs.hellogeospatial.model.AnchorEntity
import com.google.ar.core.codelabs.hellogeospatial.model.AnchorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnchorViewModel (application: Application) : AndroidViewModel(application) {
    private val repository: AnchorRepository
    val allAnchor: LiveData<List<AnchorEntity>>


    init {
        val dao = AnchorDatabase.getDatabase(application).getAnchorDao()
        repository = AnchorRepository(dao)
        allAnchor = repository.allAnchor
    }

    fun deleteAnchor(anchor: AnchorEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(anchor)

    }

    fun insertAnchor(anchor: AnchorEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(anchor)
    }

}

 */