package com.iantorno.fhirtestpaging.api

import com.google.gson.Gson
import com.iantorno.fhirtestpaging.RestServiceMockUtils
import com.iantorno.fhirtestpaging.objects.PatientResponse
import junit.framework.TestCase
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PatientApiTest {


    private var patientResponseJson: String? = null
    private var patientResponse: PatientResponse? = null

    private var server: MockWebServer? = null
    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var patientApi: PatientApi? = null

    private val count: Int = 20

    @Before
    fun setUp() {
        val gson = Gson()
        patientResponseJson = RestServiceMockUtils.getStringFromFile(this.javaClass.classLoader, "patient_response.json")
        patientResponse = gson.fromJson<PatientResponse>(patientResponseJson, PatientResponse::class.java)

        server = MockWebServer()
        server!!.start()

        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val endpoint = request.path.substring(1)
                val methodHTTP = request.method
                return when (endpoint) {
                    "Patient?_count=$count" -> if ("GET" == methodHTTP) {
                        MockResponse()
                                .setResponseCode(HttpURLConnection.HTTP_OK)
                                .addHeader("Content-Type", "application/json; charset=utf-8")
                                .setBody(patientResponseJson)
                    } else {
                        MockResponse().setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                    }
                    else -> MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                }
            }
        }

        server!!.setDispatcher(dispatcher)
        val baseUrl = server!!.url("")//"https://www.reddit.com")

        okHttpClient = OkHttpClient.Builder()
                .readTimeout(RestServiceMockUtils.CONNECTION_TIMEOUT_SHORT, TimeUnit.SECONDS)
                .connectTimeout(RestServiceMockUtils.CONNECTION_TIMEOUT_SHORT, TimeUnit.SECONDS)
                .build()

        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl.toString())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient!!)
                .build()

        patientApi = retrofit!!.create(PatientApi::class.java)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        server!!.shutdown()
    }

    @Test
    @Throws(Exception::class)
    fun fetchPatientTest() {
        val latch = CountDownLatch(1)

        val call = patientApi!!.getPatients(count)
        call.enqueue(object : Callback<PatientResponse> {
            override fun onResponse(call: Call<PatientResponse>, response: retrofit2.Response<PatientResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Assert.assertNotNull(body)
                    System.out.println(body!!.toString())
                    latch.countDown()
                } else {
                    TestCase.fail("fetchPatients !isSuccessful : " + response.message())
                }
            }

            override fun onFailure(call: Call<PatientResponse>, t: Throwable) {
                TestCase.fail("fetchPatients onFailure : " + t.message)
            }
        })

        Assert.assertTrue(latch.await(RestServiceMockUtils.CONNECTION_TIMEOUT_MED, TimeUnit.SECONDS))
    }

}
