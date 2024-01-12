/*
 * Copyright 2017 Zhihu Inc.
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
package com.zhihu.matisse2.sample;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.echat.matisse.Matisse;
import com.echat.matisse.MimeType;
import com.echat.matisse.engine.impl.PicassoEngine;
import com.echat.matisse.filter.Filter;
import com.echat.matisse.internal.entity.CaptureStrategy;
import com.echat.matisse.listener.OnCheckedListener;
import com.echat.matisse.listener.OnMaxFileSizeListener;
import com.echat.matisse.listener.OnSelectedListener;
import com.echatsoft.echatsdk.permissions.EPermissions;
import com.echatsoft.echatsdk.permissions.OnPermissionCallback;
import com.echatsoft.echatsdk.permissions.Permission;

import java.util.List;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private UriAdapter mAdapter;
    private Toast sToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.zhihu).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);

        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //content://media/external/video/media/1417095
                //content://media/external/video/media/1417110
                //content://media/external/video/media/1417094
                //ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), 1417094)
                //Uri.parse("content://media/external/video/media/1417110")

//Uri.parse("content://media/external/images/media/1417113")

//                String s = "content://media/external/images/media/1417113";
//                int beginIndex = s.lastIndexOf("/");
//                Log.e("TEST", "onClick: " + beginIndex);
//                Log.e("TEST", "onClick: " + s.substring(beginIndex + 1));
//                query(Uri.parse("content://media/external/images/media/1417113"));
//                query(ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), 1417113));
//                Uri external = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), 1417094);
//                Log.e("TEST", "onClick: " + external.toString());
//                query(external);
//                delete();
//                queryDelete();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());
    }

    private void query(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            Log.e("Sample", "onClick: " + "空或者没有数据");
        }

        Log.e("Sample", "onClick: " + DatabaseUtils.dumpCursorToString(cursor));
        Log.e("DATA", "getString: DATA -> " + cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
        Log.e("DATA", "getString: DISPLAY_NAME -> " + cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)));

    }


    private void queryDelete() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                null,
                MediaStore.MediaColumns.DATA + " like ?",
                new String[]{"%com.echat.echatjsdemo.single/files/DCIM/Echat%"},
                null);
        //Android/data/com.echat.echatjsdemo.single/files/DCIM/Echat
        Log.w("queryDelete", "onClick: " + DatabaseUtils.dumpCursorToString(cursor));
    }

    private void delete() {
        ContentResolver contentResolver = getContentResolver();
        int external = contentResolver.delete(MediaStore.Files.getContentUri("external"),
                MediaStore.MediaColumns.DATA + " like ?",
                new String[]{"%com.echat.echatjsdemo.single/files/DCIM/Echat%"});
        Log.w("delete", "delete: " + external);

    }

    private void openAlbum(View v) {
        EPermissions.with(this)
                .permission(Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_VISUAL_USER_SELECTED)
                //.interceptor()
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (allGranted) {
                            openMatisse(v);
                        } else {
                            Toast.makeText(SampleActivity.this, "获取部分权限成功，但部分权限未正常授予", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            Toast.makeText(SampleActivity.this, "被永久拒绝授权", Toast.LENGTH_LONG)
                                    .show();
                            EPermissions.startPermissionActivity(SampleActivity.this, permissions);
                        } else {
                            Toast.makeText(SampleActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    private void openMatisse(View v) {
        switch (v.getId()) {
            case R.id.zhihu:
                Matisse.from(SampleActivity.this)
                        .choose(MimeType.ofAll(), false)
                        .countable(true)
                        .capture(true)
                        .captureStrategy(
                                new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
                        .maxSelectable(9)
                        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(
                                getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
//                                            .imageEngine(new GlideEngine())  // for glide-V3
                        .imageEngine(new Glide4Engine())    // for glide-V4
                        .setOnSelectedListener(new OnSelectedListener() {
                            @Override
                            public void onSelected(
                                    @NonNull List<Uri> uriList, @NonNull List<String> pathList) {
                                // DO SOMETHING IMMEDIATELY HERE
                                Log.e("onSelected", "onSelected: pathList=" + pathList);

                            }
                        })
                        .originalEnable(true)
                        .maxOriginalSize(8)
                        .autoHideToolbarOnSingleTap(true)
                        .setOnCheckedListener(new OnCheckedListener() {
                            @Override
                            public void onCheck(boolean isChecked) {
                                // DO SOMETHING IMMEDIATELY HERE
                                Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                            }
                        })
//                                            .maxFileSize(10 * 1024 * 1024)
//                                            .setOnMaxFileSizeListener(new OnMaxFileSizeListener() {
//                                                @Override
//                                                public void triggerLimit() {
//                                                    showToast("超出10M大小，无法上传", Toast.LENGTH_SHORT);
//                                                }
//                                            })
                        .forResult(REQUEST_CODE_CHOOSE);
                break;
            case R.id.dracula:
                Matisse.from(SampleActivity.this)
                        .choose(MimeType.ofAll())
                        .theme(R.style.Matisse_Dracula)
                        .countable(false)
                        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .maxSelectablePerMediaType(9, 1)
                        .originalEnable(false)
                        .imageEngine(new PicassoEngine())
                        .maxFileSize(20 * 1024 * 1024)
                        .setOnMaxFileSizeListener(new OnMaxFileSizeListener() {
                            @Override
                            public void triggerLimit() {
                                showToast("超出20M大小，无法上传", Toast.LENGTH_SHORT);
                            }
                        })
                        .forResult(REQUEST_CODE_CHOOSE);
                break;
            default:
                break;
        }
        mAdapter.setData(null, null);
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        switch (id) {
            case R.id.zhihu:
            case R.id.dracula:
                openAlbum(v);
        }
    }

    /**
     * @param text
     * @param duration
     */
    private void showToast(final CharSequence text, final int duration) {
        cancelToast();
        sToast = Toast.makeText(this, text, duration);
        sToast.show();
    }

    private void cancelToast() {
        if (sToast != null) {
            sToast.cancel();
            sToast = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
            Log.e("OnActivityResult ", String.valueOf(Matisse.obtainOriginalState(data)));
            query(Matisse.obtainResult(data).get(0));
        }
    }

    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (TextView) contentView.findViewById(R.id.uri);
                mPath = (TextView) contentView.findViewById(R.id.path);
            }
        }
    }


}
