package com.danielkim.soundrecorder.fragments;

import android.os.Bundle;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;

/**
 * Created by Daniel on 12/23/2014.
 */
public class FileViewerFragment extends Fragment{
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";

    private int position;
    private FileViewerAdapter mFileViewerAdapter;
    protected TextView totalItem;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        setHasOptionsMenu(true);
        observer.startWatching();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setTotalItem(Integer.toString(mFileViewerAdapter.getItemCount()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        totalItem = (TextView)v.findViewById(R.id.totalItem2);
        final RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm);
        setTotalItem(Integer.toString(mFileViewerAdapter.getItemCount()));
        mRecyclerView.setAdapter(mFileViewerAdapter);

        return v;
    }

    public void setTotalItem(String item){
        totalItem.setText(item);
    }


    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + "/SoundRecorder") {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if(event == FileObserver.DELETE){
                        // user deletes a recording file out of the app

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };
}




