package com.example.dancetimetableapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dancetimetableapp.datasources.CachedDataSource
import com.example.dancetimetableapp.datasources.RemoteDataSource
import com.example.dancetimetableapp.model.FilterParams
import com.example.dancetimetableapp.model.Lesson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Repository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val remoteDataSource by lazy {
        RemoteDataSource()
    }

    fun getCourses(context: Context): LiveData<List<String>> {
        val coursesLiveData = MutableLiveData<List<String>>()

        val cachedDataSource = CachedDataSource("courses")
        cachedDataSource.getCourses(context)?.let {
            coursesLiveData.postValue(it)
        }

        if(!cachedDataSource.isUpToDate(context)) {
            scope.launch {
                remoteDataSource.getCourses(context)?.let {
                    coursesLiveData.postValue(it)
                }
            }
        }

        return coursesLiveData
    }

    fun loadLessons(context: Context, list: MutableLiveData<List<Lesson>>, filterParams: FilterParams) {
        val cachedDataSource = CachedDataSource("lessons")
        cachedDataSource.getLessons(context, filterParams)?.let {
            list.postValue(it)
        }

        if(!cachedDataSource.isUpToDate(context)) {
            scope.launch {
                remoteDataSource.getLessons(context, filterParams)?.let {
                    list.postValue(it)
                } ?: run {
                    if(!cachedDataSource.exists(context))
                        list.postValue(ArrayList())
                }
            }
        }
    }
}