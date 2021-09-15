package com.cliffordlab.amoss.network.json

/**
 * Created by tonynguyen on 2/27/18.
 */
class FhirDataRequest(
    private val participant_ID: String,
    private val patient_token: String,
    private val patient_ID: String,
    private val category: String
)