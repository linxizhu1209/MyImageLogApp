package com.example.myimagelogapp.viewModel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.Test

class PostCreateViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @Test
    fun addPhotos_distinctAndLimit() {
        // given (준비)
        val vm = PostCreateViewModel()

        val u1 = "content://photo/1"
        val u2 = "content://photo/2"

        // when (행동)
        vm.addPhotos(listOf(u1,u2,u1), max = 2)

        // then (검증)
        val result = vm.photos.value.orEmpty()
        assertEquals(listOf(u1,u2), result)
    }

    @Test
    fun removePhoto_removesOnlyTarget() {
        val vm = PostCreateViewModel()

        val u1 = "content://photo/1"
        val u2 ="content://photo/2"
        vm.addPhotos(listOf(u1,u2), max=10)

        vm.removePhoto(u1)

        val result = vm.photos.value.orEmpty()
        assertEquals(listOf(u2), result)
    }
}