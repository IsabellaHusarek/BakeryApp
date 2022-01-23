package org.wit.bakeryapp

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {

    private val firestoreDb = FirebaseFirestore.getInstance()

    fun readRecipes(): CollectionReference {
        val collectionReference = firestoreDb.collection("recipes")
        return collectionReference
    }

    companion object {
        const val TAG = "FirestoreRepository"
    }
}
