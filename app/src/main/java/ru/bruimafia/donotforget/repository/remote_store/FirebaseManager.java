package ru.bruimafia.donotforget.repository.remote_store;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.util.Constants;

public class FirebaseManager implements FirebaseBase {

    private CollectionReference db;

    public FirebaseManager() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null)
            db = FirebaseFirestore.getInstance().collection(auth.getCurrentUser().getUid());
    }

    @Override
    public List<Note> getAll() {
        List<Note> notes = new ArrayList<>();
        if (db != null) {
            db.get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot document : snapshotList)
                    notes.add(document.toObject(Note.class));
            });
        }

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return notes;
    }

    @Override
    public void insertOrUpdate(Note note) {
        if (db != null) {
            db.document(String.valueOf(note.getId()))
                    .set(note, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(Constants.TAG, "DocumentSnapshot successfully insert or update!"));
        }
    }

    @Override
    public void delete(long id) {
        if (db != null) {
            db.document(String.valueOf(id))
                    .update("inHistory", true)
                    .addOnSuccessListener(aVoid -> Log.d(Constants.TAG, "DocumentSnapshot successfully delete!"));
        }
    }

    @Override
    public void recover(long id) {
        if (db != null) {
            db.document(String.valueOf(id))
                    .update("inHistory", false)
                    .addOnSuccessListener(aVoid -> Log.d(Constants.TAG, "DocumentSnapshot successfully recover!"));
        }
    }

    @Override
    public void clear() {
        if (db != null) {
            db.whereEqualTo("inHistory", false)
                    .get()
                    .addOnSuccessListener(documents -> {
                        for (DocumentSnapshot document : documents.getDocuments())
                            document.getReference().delete();
                        Log.d(Constants.TAG, "DocumentSnapshot successfully clear!");
                    });
        }
    }

    @Override
    public void clearHistory() {
        if (db != null) {
            db.whereEqualTo("inHistory", true)
                    .get()
                    .addOnSuccessListener(documents -> {
                        for (DocumentSnapshot document : documents.getDocuments())
                            document.getReference().delete();
                        Log.d(Constants.TAG, "DocumentSnapshot successfully clearHistory!");
                    });
        }
    }
}
