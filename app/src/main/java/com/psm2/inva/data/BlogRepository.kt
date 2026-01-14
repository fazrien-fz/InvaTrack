package com.psm2.inva.data

import android.net.Uri
import com.psm2.inva.model.Blog
import kotlinx.coroutines.flow.Flow

interface BlogRepository {
    fun getAllBlogs(): Flow<List<Blog>>
    suspend fun addBlog(blog: Blog, imageUri: Uri?): Result<Unit>
    suspend fun getBlogById(blogId: String): Blog?
    suspend fun updateBlog(blog: Blog, imageUri: Uri?): Result<Unit>
    suspend fun deleteBlog(blogId: String): Result<Unit>
}