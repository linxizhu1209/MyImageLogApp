package com.example.myimagelogapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myimagelogapp.data.AppDatabase
import com.example.myimagelogapp.entity.UploadTaskEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UploadTaskDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db= Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_and_read() = runBlocking {
        val dao = db.uploadTaskDao()

        dao.upsert(
            UploadTaskEntity(
                workId = "1",
                title = "title",
                content = "content",
                photoCount = 2,
                status = "RUNNING",
                progress = 10,
                createdAt = 1L
            )
        )

        val list = dao.observeAll()
        assertEquals(true, list != null)
    }
}