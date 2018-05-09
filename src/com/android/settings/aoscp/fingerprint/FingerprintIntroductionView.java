/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.aoscp.fingerprint;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.android.settings.R;

/**
 * A view containing a VideoView for showing the introduction to fingerprint enrollment
 */
public class FingerprintIntroductionView extends TextureView implements FingerprintIntroductionAnimation {
	
    protected float mAspect = 1.0f;
    protected MediaPlayer mMediaPlayer;

    public FingerprintIntroductionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(mAspect * originalWidth);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    protected Uri getIntroductionAnimation() {
        return resourceEntryToUri(getContext(), R.raw.fingerprint_introduction_animation);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setSurfaceTextureListener(new SurfaceTextureListener() {
            private SurfaceTexture mTextureToDestroy = null;

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,
                    int height) {
                setVisibility(View.INVISIBLE);
                Uri videoUri = getIntroductionAnimation();
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                }
                if (mTextureToDestroy != null) {
                    mTextureToDestroy.release();
                    mTextureToDestroy = null;
                }
                mMediaPlayer = createMediaPlayer(mContext, videoUri);
                if (mMediaPlayer == null) {
                    return;
                }
                mMediaPlayer.setSurface(new Surface(surfaceTexture));
                mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.setLooping(true);
                    }
                });
                mMediaPlayer.setOnInfoListener(new OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            setVisibility(View.VISIBLE);
                        }
                        return false;
                    }
                });
                mAspect = (float) mMediaPlayer.getVideoHeight() / mMediaPlayer.getVideoWidth();
                requestLayout();
                start();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                    int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                mTextureToDestroy = surfaceTexture;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });
    }

    @VisibleForTesting
    MediaPlayer createMediaPlayer(Context context, Uri videoUri) {
        return MediaPlayer.create(mContext, videoUri);
    }

    protected static Uri resourceEntryToUri (Context context, int id) {
        Resources res = context.getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                res.getResourcePackageName(id) + '/' +
                res.getResourceTypeName(id) + '/' +
                res.getResourceEntryName(id));
    }

    @Override
    public void start() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

}
