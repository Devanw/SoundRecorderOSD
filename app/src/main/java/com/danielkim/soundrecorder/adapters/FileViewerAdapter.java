package com.danielkim.soundrecorder.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateUtils;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.fragments.PlaybackFragment;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * Created by Daniel on 12/29/2014.
 */
public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
    implements OnDatabaseChangedListener{

    private static final String LOG_TAG = "FileViewerAdapter";

    private DBHelper mDatabase;

    RecordingItem item;
    Context mContext;
    LinearLayoutManager llm;

    private boolean isMultiSelect = false;
    private List<Integer> selectedIds = new ArrayList<>();
    private ActionMode actionMode;

    public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

        cleararray();

        item = getItem(position);
        long itemDuration = item.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(item.getName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(
            DateUtils.formatDateTime(
                mContext,
                item.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
            )
        );

        if (item.isImportant()) {
            holder.star.setVisibility(View.VISIBLE);
        }else{
            holder.star.setVisibility(View.INVISIBLE);
        }

        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMultiSelect) {
                    ArrayList<String> entrys = new ArrayList<String>();
                    entrys.add("Play File");
                    entrys.add(mContext.getString(R.string.dialog_file_share));
                    entrys.add(mContext.getString(R.string.dialog_file_rename));
                    entrys.add(mContext.getString(R.string.dialog_file_delete));
                    entrys.add("Mark Importance");
                    entrys.add("check");

                    final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);


                    // File delete confirm
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(mContext.getString(R.string.dialog_title_options));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (item == 0) {
                                try {
                                    PlaybackFragment playbackFragment =
                                            new PlaybackFragment().newInstance(getItem(holder.getPosition()));

                                    FragmentTransaction transaction = ((FragmentActivity) mContext)
                                            .getSupportFragmentManager()
                                            .beginTransaction();

                                    playbackFragment.show(transaction, "dialog_playback");

                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "exception", e);
                                }
                            } else if (item == 1) {
                                shareFileDialog(holder.getPosition());
                            } else if (item == 2) {
                                renameFileDialog(holder.getPosition());
                            } else if (item == 3) {
                                if (holder.star.isShown()) {
                                    deleteFileDialog2(holder.getPosition());
                                } else {
                                    deleteFileDialog(holder.getPosition());
                                }
                            } else if (item == 4) {
                                markImportance(holder.getPosition());
                                if (!holder.getVisible()) {
                                    holder.star.setVisibility(View.VISIBLE);
                                    holder.setPicVisible(true);
                                } else {
                                    holder.star.setVisibility(View.INVISIBLE);
                                    holder.setPicVisible(false);
                                }
                            } else if (item == 5) {
                                Toast.makeText(mContext, String.valueOf(getItem(holder.getPosition()).isImportant()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setCancelable(true);
                    builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else{
                    addtoarraylist(holder.getPosition());
                    view.setBackgroundColor(Color.parseColor("#35c7e8"));

                    if (!holder.getSelected()){
                        view.setBackgroundColor(Color.parseColor("#35c7e8"));
                        holder.setSelected(true);}
                    else{
                        view.setBackgroundColor(Color.WHITE);
                        holder.setSelected(false);
                    }
                }
            }

        });


        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                if (!isMultiSelect){
//                    Toast.makeText(mContext, "Hold again to delete selected files", Toast.LENGTH_SHORT).show();
//                    v.setBackgroundColor(Color.parseColor("#35c7e8"));
//                    isMultiSelect = true;
//                    if (!holder.getSelected()){
//                        a.add(holder.getPosition());
//                        v.setBackgroundColor(Color.parseColor("#35c7e8"));
//                        holder.setSelected(true);}
//                }
//                else {
//                    Toast.makeText(mContext, "size = "+a.size(), Toast.LENGTH_SHORT).show();
//                    for (int i = 0; i<a.size(); i++) {
//                        Collections.sort(a);
//                        deleteFileDialog2(a.get(i));
//                    }
//                    cleararray();
//                    isMultiSelect = false;
//                }
                return false;
            }
        });
    }

    ArrayList<Integer> a = new ArrayList<>();
    private void addtoarraylist(int pos){
        boolean haha = false;
        for (int i = 0; i< a.size(); i++){
            if (!a.contains(pos)){
                haha = true;
            }
        }
        if (haha) {
            a.add(pos);
        }
    }
    private void cleararray(){a.clear();}

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected ImageView star;
        protected View cardView;
        protected boolean picVisible;
        protected boolean selected;

        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            star =  (ImageView)v.findViewById(R.id.imageView_on);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            cardView = v.findViewById(R.id.card_view);
        }
        public boolean getVisible(){
            return picVisible;
        }

        public void setPicVisible(boolean b){
            picVisible = b;
        }

        public boolean getSelected(){
            return selected;
        }

        public void setSelected(boolean a){
            selected = a;
        }

    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public RecordingItem getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(
            mContext,
            String.format(
                mContext.getString(R.string.toast_file_delete),
                getItem(position).getName()
            ),
            Toast.LENGTH_SHORT
        ).show();

        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

    public void rename(int position, String name) {
        //rename a file

        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    public void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
    }

    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim() + ".mp4";
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void markImportance (final int position){
        if (!getItem(position).isImportant()){
            getItem(position).setImportant();
            Log.d(LOG_TAG, "markImportance: setimportant");
        }
        else{
            getItem(position).setUnimportant();
            Log.d(LOG_TAG, "markImportance: setunimportant");
        }
    }

    public void deleteFileDialog (final int position) {
        // File delete confirm
        if (getItem(position).isImportant()) {
            AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
            confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
            confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
            confirmDelete.setCancelable(true);
            confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                //remove item from database, recyclerview, and storage
                                remove(position);

                            } catch (Exception e) {
                                Log.e(LOG_TAG, "exception", e);
                            }

                            dialog.cancel();
                        }
                    });
            confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = confirmDelete.create();
            alert.show();
        } else {
            try {
                //remove item from database, recyclerview, and storage
                remove(position);

            } catch (Exception e) {
                Log.e(LOG_TAG, "exception", e);
            }
        }
    }
    public void deleteFileDialog2 (final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();}
}