package com.psm2.inva.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.psm2.inva.model.Blog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BlogRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : BlogRepository {

    override fun getAllBlogs(): Flow<List<Blog>> {
        return firestore.collection("blogs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Blog>()
            }
    }

    override suspend fun addBlog(blog: Blog, imageUri: Uri?): Result<Unit> {
        return try {
            val blogDocRef = firestore.collection("blogs").document()
            var blogToSave = blog.copy(id = blogDocRef.id)

            if (imageUri != null) {
                val storageRef = storage.reference
                val imageRef = storageRef.child("blog_images/${blogDocRef.id}/${UUID.randomUUID()}.jpg")

                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                blogToSave = blogToSave.copy(coverImageUrl = downloadUrl)
            }

            blogDocRef.set(blogToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBlogById(blogId: String): Blog? {
        return try {
            val snapshot = firestore.collection("blogs").document(blogId).get().await()
            snapshot.toObject(Blog::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateBlog(blog: Blog, imageUri: Uri?): Result<Unit> {
        return try {
            var blogToUpdate = blog

            if (imageUri != null) {
                val storageRef = storage.reference
                // Use a new UUID for the new image or overwrite if preferred, UUID is safer for caching issues
                val imageRef = storageRef.child("blog_images/${blog.id}/${UUID.randomUUID()}.jpg")

                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                blogToUpdate = blog.copy(coverImageUrl = downloadUrl)
            }

            firestore.collection("blogs")
                .document(blog.id)
                .set(blogToUpdate)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBlog(blogId: String): Result<Unit> {
        return try {
            // Delete document
            firestore.collection("blogs").document(blogId).delete().await()

            // Attempt to clean up storage folder (requires listing, which might be complex,
            // or just rely on the structure. Since we use random UUIDs, exact cleanup is hard without listing.
            // For this scope, we'll try to delete the folder container logic if possible,
            // but Firebase Storage folder deletion isn't direct.
            // We usually just leave orphaned images or use Cloud Functions.
            // We will proceed with success.

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}