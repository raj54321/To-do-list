/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.practicalwork.todoapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.practicalwork.todoapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.practicalwork.todoapp.data.TaskContract;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TASK_LOADER_ID = 0;

    private CustomCursorAdapter mAdapter;
    RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewTasks);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new CustomCursorAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                int id = (int) viewHolder.itemView.getTag();

                String stringId = Integer.toString(id);
                Uri uri = TaskContract.TaskEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                getContentResolver().delete(uri, null, null);

                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);

            }
        }).attachToRecyclerView(mRecyclerView);


        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });


        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mTaskData = null;

            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    deliverResult(mTaskData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

                try {
                    return getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            TaskContract.TaskEntry.COLUMN_PRIORITY);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mAdapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

}

