package com.shinyst.coalpoolmobile.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shinyst.coalpoolmobile.data.daos.SubmissionResultDao
import com.shinyst.coalpoolmobile.data.entities.SubmissionResult
import kotlinx.coroutines.*

class SubmissionResultRepository(private val submissionResultDao: SubmissionResultDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertSubmissionResult(newSubmissionResult: SubmissionResult) {
        coroutineScope.launch(Dispatchers.IO) {
            submissionResultDao.insertSubmissionResult(newSubmissionResult)
        }
    }

    fun getAllSubmissionResults(): List<SubmissionResult> {
        return submissionResultDao.getAllSubmissionResults()
    }
}
