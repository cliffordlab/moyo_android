package com.cliffordlab.amoss.datacollector

import com.cliffordlab.amoss.network.DataUploader
import com.evernote.android.job.Job
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


/**
 * Created by ChristopherWainwrightAaron on 2/8/17.
 */

class FileUploadJob : Job() {

    val JOB_TAG = jobTag
    var disposable = CompositeDisposable()
    private var mDisposable: Disposable? = null


    override fun onRunJob(params: Job.Params): Job.Result {
        val uploader = DataUploader(context = context)
        uploader.uploadData()
        return Job.Result.SUCCESS
    }

    companion object {
        const val jobTag = "FILE_UPLOAD_JOB"
    }
}
