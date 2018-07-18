package com.iantorno.fhirtestpaging.api

import com.google.gson.Gson
import com.iantorno.fhirtestpaging.RestServiceMockUtils
import com.iantorno.fhirtestpaging.RxImmediateSchedulerRule
import com.iantorno.fhirtestpaging.objects.PatientResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PatientApiTest {

    companion object {
        @ClassRule
        @JvmField
        val schedulers: RxImmediateSchedulerRule = RxImmediateSchedulerRule()
    }

    private var patientResponseJson: String? = null
    private var patientResponse: PatientResponse? = null

    private var server: MockWebServer? = null
    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var patientApi: PatientApi? = null

    private val count: Int = 10

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
        val baseUrl = server!!.url("")//" http://hapi.fhir.org/baseDstu3")

        okHttpClient = OkHttpClient.Builder()
                .readTimeout(RestServiceMockUtils.CONNECTION_TIMEOUT_SHORT, TimeUnit.SECONDS)
                .connectTimeout(RestServiceMockUtils.CONNECTION_TIMEOUT_SHORT, TimeUnit.SECONDS)
                .build()

        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl.toString())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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

        patientApi!!.getPatients(10)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe({ t: PatientResponse? ->
                    Assert.assertNotNull(t)
                    System.out.println(t!!.toString())
                    latch.countDown()
                }, { t ->
                    TestCase.fail("fetchPatients onFailure : " + t.message)
                })

        Assert.assertTrue(latch.await(RestServiceMockUtils.CONNECTION_TIMEOUT_MED, TimeUnit.SECONDS))
    }

}
