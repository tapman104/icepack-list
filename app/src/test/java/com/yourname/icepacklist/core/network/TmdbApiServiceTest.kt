package com.yourname.icepacklist.core.network

import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TmdbApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: TmdbApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = Moshi.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit.create(TmdbApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getPopularMovies parses response correctly`() = runTest {
        val mockResponseJson = """
            {
                "page": 1,
                "results": [
                    {
                        "id": 550,
                        "title": "Fight Club",
                        "overview": "A ticking-time-bomb insomniac...",
                        "poster_path": "/pB8O4LaSqruRUPE49.jpg",
                        "backdrop_path": "/fCayJrkfRaCRCTh8GqN.jpg",
                        "vote_average": 8.4,
                        "release_date": "1999-10-15"
                    }
                ],
                "total_pages": 1,
                "total_results": 1
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponseJson)
        )

        val response = apiService.getPopularMovies(1)

        assertNotNull(response)
        assertEquals(1, response.page)
        assertEquals(1, response.results.size)
        assertEquals("Fight Club", response.results[0].title)
        assertEquals(550, response.results[0].id)
        
        val request = mockWebServer.takeRequest()
        assertEquals("/movie/popular?page=1", request.path)
    }
}
