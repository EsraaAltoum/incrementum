package com.example.incrementum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //button to test habit effect activity, not there in final project
    Button button;
    Button goto_login_button;
    Button journal;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.title);


        final StitchAppClient client =
                Stitch.initializeDefaultAppClient("incrementum-xjkms");

        final RemoteMongoClient mongoClient =
                client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");

        final RemoteMongoCollection<Document> coll =
                mongoClient.getDatabase("Incrementum").getCollection("test");

        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(
                new Continuation<StitchUser, Task<RemoteUpdateResult>>() {

                    @Override
                    public Task<RemoteUpdateResult> then(@NonNull Task<StitchUser> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.e("STITCH", "Login failed!");
                            throw task.getException();
                        }

                        final Document updateDoc = new Document(
                                "owner_id",
                                task.getResult().getId()
                        );

                        updateDoc.put("number", 25);
                        return coll.updateOne(
                                null, updateDoc, new RemoteUpdateOptions().upsert(true)
                        );
                    }
                }
        ).continueWithTask(new Continuation<RemoteUpdateResult, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<RemoteUpdateResult> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.e("STITCH", "Update failed!");
                    throw task.getException();
                }
                List<Document> docs = new ArrayList<>();
                return coll
                        .find(new Document("owner_id", client.getAuth().getUser().getId()))
                        .limit(100)
                        .into(docs);
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    Log.d("STITCH", "Found docs: " + task.getResult().toString());
                    return;
                }
                Log.e("STITCH", "Error: " + task.getException().toString());
                task.getException().printStackTrace();
            }
        });




      // find button by id
        button = findViewById(R.id.button);
        goto_login_button = findViewById(R.id.goto_login_button);
        journal = findViewById(R.id.journal);

        // on click function
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openHabitEffectActivity();
            }
        });

        // on click function
        goto_login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openLoginActivity();
            }
        });

        journal.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            openJournalActivity();
          }
        });
    }

    // open habit effect
    public void openHabitEffectActivity(){
        Intent intent = new Intent(this, HabitEffectActivity.class);
        startActivity(intent);
    }

    public void openLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

  public void openJournalActivity(){
    Intent intent = new Intent(this, ViewJournalActivity.class);
    startActivity(intent);
  }

}
