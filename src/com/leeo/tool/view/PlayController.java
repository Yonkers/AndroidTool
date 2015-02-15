/**
 * Date:2013-12-2
 *
 */
package com.leeo.tool.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

/**
 * 播放引擎控制模块
 * 比较实用于频繁切换视频的场景
 * @author leeo
 */
public class PlayController {

    private static final String TAG = "PlayController";

    /**
     * 文件开始播放
     */
    public static final int MEDIA_PLAYER_START = 5001;
    /**
     * 刷新进度条消息
     */
    public static final int MEDIA_UPDATE_PROGRESS = 5002;
    /**
     * 文件播放完成
     */
    public static final int MEDIA_PLAYER_COMPLETED = 5003;
    /**
     * 开始缓冲
     */
    public static final int MEDIA_INFO_BUFFERING_START = 5004;
    /**
     * 缓冲结束
     */
    public static final int MEDIA_INFO_BUFFERING_END = 5005;
    /**
     * 刷新音频频谱
     */
    public static final int MSG_AUDIO_VISUALIZER_UPDATE = 5006;
    /**
     * 文件播放出错
     */
    public static final int MEDIA_PLAYER_ERROR = 5007;

    /**
     * 播放引擎Seek动作完成
     */
    public static final int MEDIA_SEEK_COMPLETED = 5008;

    public static final int MEDIA_IO_ERROR = 5009;

    public static final int MEDIA_PLAYER_PREPARED = 5010;

    private enum MethodCall {
        STOP, START, PAUSE, SET_DATA_SOURCE, RESET, RELEASE, SET_DISPLAY
    }

    private PlayState mState = null;

    private Handler eventHandler;

    private List<MethodCall> callList = new ArrayList<MethodCall>();

    private static PlayController instance;

    ControlThread thread;

    private String path;

    private PlayController() {
        thread = new ControlThread();
        thread.start();
    }

    public static PlayController getInstance() {
        if (null == instance) {
            instance = new PlayController();
        }
        return instance;
    }

    /**
     * 设置与界面通讯的消息句柄
     *
     * @param handler 与界面通讯的消息句柄
     */
    public void registerHandler(Handler handler) {
        eventHandler = handler;
    }

    public boolean hasRegistHandler() {
        return eventHandler != null;
    }

    private void addCallMethod(MethodCall methodCall) {
        int size = callList.size();
        if(size > 0){
            MethodCall endMethod = callList.get(size - 1);
            if(endMethod == methodCall){
                return;
            }
            if(endMethod == MethodCall.RESET && methodCall == MethodCall.STOP){
                return;
            }
            if(endMethod == MethodCall.RELEASE && (methodCall == MethodCall.STOP || methodCall == MethodCall.RESET)){
                return;
            }
        }
        Log.e(TAG, "add call method " + methodCall);
        callList.add(methodCall);
    }

    /**
     * 释放播放器
     */
    public void release() {
        addCallMethod(MethodCall.RELEASE);
    }

    public void reset(boolean play) {
        addCallMethod(MethodCall.RESET);
        if (play) {
            tryPrepareCount = 0;
            addCallMethod(MethodCall.SET_DATA_SOURCE);
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        addCallMethod(MethodCall.PAUSE);
    }

    /**
     * 停止播放
     */
    public void stop() {
        addCallMethod(MethodCall.STOP);
    }

    /**
     * 开始播放
     */
    public void start() {
        addCallMethod(MethodCall.START);
    }

    private int tryPrepareCount = 0;
    private int TRY_PREPARE_MAX_COUNT = 5;

    /**
     * 设置播放url
     * @param path 播放url
     */
    public void setDataSource(final String path) {
        this.path = path;
        tryPrepareCount = 0;
        addCallMethod(MethodCall.SET_DATA_SOURCE);
    }

    /**
     * 获取播放状态
     *
     * @return 播放状态
     */
    public PlayState getPlayState() {
        return mState;
    }

    public void setDisplay(SurfaceHolder holder) {
        if (null != thread) {
            thread.setDisplay(holder);
        }
    }

    /**
     * 释放引用
     */
    public synchronized void destory() {
        if (null != eventHandler) {
            eventHandler = null;
        }

        if (instance != null) {
            instance = null;
        }
    }

    class ControlThread extends Thread implements OnBufferingUpdateListener,
            OnCompletionListener, OnErrorListener, OnInfoListener,
            OnPreparedListener, OnSeekCompleteListener,
            OnVideoSizeChangedListener {

        ControlThread() {
            initPlayer();
        }

        private MediaPlayer mp;
        private int bufProgress;//预缓冲

        private int lastPosition;
        private Timer progressTimer;//获取播放引擎播放进度的计时器
        private TimerTask progressTask;
        //是否处于缓冲状态
        private boolean isBuffering = false;

        private synchronized void initPlayer() {
            if (null == mp) {
                mp = new MediaPlayer();
                mp.setOnBufferingUpdateListener(this);
                mp.setOnCompletionListener(this);
                mp.setOnErrorListener(this);
                mp.setOnInfoListener(this);
                mp.setOnPreparedListener(this);
                mp.setOnSeekCompleteListener(this);
                mp.setOnVideoSizeChangedListener(this);
                setPlayerState(PlayState.IDLE);
            }
//        mp.reset();
            lastPosition = 0;
        }


        @Override
        public void run() {
            while (true) {
                if(callList.size() == 0){
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (callList.size() > 0) {
                    MethodCall methodCall = callList.remove(0);
                    Log.d(TAG, "METHOD LIST : " + callList.toString());
                    Log.d(TAG, "START CALL METHOD : " + methodCall);
                    switch (methodCall) {
                        case RESET:
                            doReset(false);
                            break;
                        case SET_DATA_SOURCE:
                            doSetDataSource(path);
                            break;
                        case START:
                            doStart();
                            break;
                        case PAUSE:
                            doPause();
                            break;
                        case STOP:
                            doStop();
                            break;
                        case RELEASE:
                            doRelease();
                            break;
                    }
                    Log.d(TAG, "METHOD LIST : " + callList.toString());
                }
            }
        }

        private void doSetDataSource(String path) {
            if (null != mState && mState == PlayState.RESETTING) {
                Log.d(TAG, "MediaPlayer is resetting, setDataSource fail.");
                return;
            }
            playNew(path);
//            if (mState == PlayState.RESETTED) {
//                playNew(path);
//            } else {
//                reset(true);
//            }
        }

        private void playNew(String path) {
            try {
                initPlayer();
                purgeUpdateProgress();
                if (PlayState.RESETTED != mState) {
                    Log.d(TAG, "do reset");
                    mp.reset();
                    Log.d(TAG, "do reset ok");
                }
                setPlayerState(PlayState.IDLE);
                mp.setDataSource(path);
                setPlayerState(PlayState.INITIALIZED);
                Log.d(TAG, "start prepare");
                mp.prepareAsync();
                setPlayerState(PlayState.PREPARING);
                if(null != eventHandler){
                    eventHandler.removeCallbacks(prepareListenRunnable);
                    if(tryPrepareCount < TRY_PREPARE_MAX_COUNT){
                        eventHandler.postDelayed(prepareListenRunnable,8000);
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "prepare error " + e.toString());
                e.printStackTrace();
                sendMessage(MEDIA_IO_ERROR);
            } catch (Exception e) {
                Log.d(TAG, "prepare error " + e.toString() + "---" + path);
                e.printStackTrace();
                sendMessage(MEDIA_PLAYER_ERROR);
            }
        }

        /**
         * 播放器调用prepare()后开始监控状态，如果在８s后还是处于prepare状态，那么释放掉播放器，并重新初始化
         * 最多执行次数：TRY_PREPARE_MAX_COUNT
         */
        private Runnable prepareListenRunnable = new Runnable() {
            @Override
            public void run() {
                if(PlayState.PREPARING == mState){
                    if(tryPrepareCount < TRY_PREPARE_MAX_COUNT){
                        tryPrepareCount ++;
                        Log.w(TAG,"MediaPlayer prepare out of time,try release player and init again.time " + tryPrepareCount);
                        addCallMethod(MethodCall.RELEASE);
                        addCallMethod(MethodCall.SET_DATA_SOURCE);
                    }
                }
            }
        };

        public boolean hasRelased() {
            return null == mp;
        }


        private void doRelease() {
            if (mp != null) {
                Log.d(TAG, "release");
                purgeUpdateProgress();
                mp.release();
                mp = null;
                setPlayerState(PlayState.END);
            }
        }


        private void doReset(boolean play) {
            if(PlayState.END == mState){
                Log.d(TAG,"player end,can't reset");
                return;
            }
            if (null != mp) {
                if (PlayState.RESETTING == mState || PlayState.RESETTED == mState) {
                    Log.w(TAG, "MP is resetting or resetted, call reset would not work.");
                    return;
                }
                Log.d(TAG, "reset");
                setPlayerState(PlayState.RESETTING);
                mp.reset();
                Log.d(TAG, "reset finish.");
                setPlayerState(PlayState.RESETTED);

                if (PlayState.RESETTED == mState) {
                    if (play) {
                        setDataSource(path);
                    }
                }
            }
        }


        private void doPause() {
            if (null == mp) return;
            if (mState == PlayState.STARTED) {
                Log.d(TAG, "pause");
                mp.pause();
                setPlayerState(PlayState.PAUSED);
            }
        }


        private void doStop() {
            if (mState == PlayState.PREPARED
                    || mState == PlayState.STARTED
                    || mState == PlayState.PAUSED
                    || mState == PlayState.STOPPED) {
//				|| mState == PlayState.PLAYBACK_COMPLETED) {
                if (null != mp) {
                    Log.d(TAG, "stop");
                    mp.stop();
                    setPlayerState(PlayState.STOPPED);
                }
            }
        }


        private void doStart() {
            if (null == mp) return;
            if (mState == PlayState.STARTED
                    || mState == PlayState.PAUSED
                    || mState == PlayState.PREPARED
                    || mState == PlayState.PLAYBACK_COMPLETED) {
                Log.d(TAG, "start");
                mp.start();
                setPlayerState(PlayState.STARTED);
            }
        }

        /**
         * 注册绘制句柄
         *
         * @param holder 绘制句柄
         */
        public void setDisplay(SurfaceHolder holder) {
            if (null == mp) {
                return;
            }
            try {
                mp.setDisplay(holder);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        /**
         * 开始每隔一段时间获取播放进度
         */
        public void startUpdateProgress() {
            purgeUpdateProgress();
            if (null == progressTimer) {
                progressTimer = new Timer();
            }

            if (null == progressTask) {
                progressTask = new TimerTask() {
                    @Override
                    public void run() {
                        int posi = mp.getCurrentPosition();
                        if ((PlayState.STARTED == getPlayState() || PlayState.STOPPED == getPlayState())
                                && (lastPosition > 0 && posi == 0)) {//切换清晰度时 时间会闪一次0:00
                            return;
                        }
                        lastPosition = posi;
                        if (null == eventHandler) return;
                        Message msg = eventHandler.obtainMessage();
                        msg.what = MEDIA_UPDATE_PROGRESS;
                        msg.arg1 = posi;
                        msg.arg2 = getBuffer();
                        eventHandler.sendMessage(msg);
                    }
                };
            }

            progressTimer.schedule(progressTask, 0, 1000);
        }

        /**
         * 保存上次显示进度
         */
        public int getLastPosition() {
            return lastPosition;
        }

        /**
         * 停止进度更新
         */
        public void purgeUpdateProgress() {
            if (progressTask != null) {
                progressTask.cancel();
                progressTask = null;
            }

            if (progressTimer != null) {
                progressTimer.cancel();
                progressTimer.purge();
                progressTimer = null;
            }

        }

        /**
         * 拖动进度条时更新播放进度
         *
         * @param progress 播放进度
         * @return
         */
        public synchronized boolean seekTo(int progress) {
            if (mp != null) {
                int duration = mp.getDuration();
                //如果拖动到最后，就把进度设置为倒数第三秒
                if (progress >= duration - 1000) {
                    if (duration > 1000) {
                        mp.seekTo(duration - 2000);
                    }
                } else {
                    Log.d(TAG, "seek to---" + progress);
                    mp.seekTo(progress);
                    return true;
                }
            }
            return false;
        }


        /**
         * 播放界面大小改变的回调
         */
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        	Log.d(TAG, "====onVideoSizeChanged, width:" + width + "  height:" + height);
        	if (width == 0 || height == 0) {
        		sendMessage(MEDIA_PLAYER_COMPLETED);
        	}
    	}

        /**
         * 文件Seek完成
         */
        public void onSeekComplete(MediaPlayer mp) {
            Log.d(TAG, "====onSeekComplete");
            sendMessage(MEDIA_SEEK_COMPLETED);
        }

        /**
         * 准备就绪
         */
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "====onPrepared");
            setPlayerState(PlayState.PREPARED);
            sendMessage(MEDIA_PLAYER_PREPARED);
            mp.start();
            setPlayerState(PlayState.STARTED);
            sendMessage(MEDIA_PLAYER_START);
        }

        /**
         * 获取一般播放信息并发送给界面
         */
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "====onInfo, what:" + what + "  extra:" + extra);
            switch (what) {
                case 32773:
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    setBuffering(true);
                    sendMessage(MEDIA_INFO_BUFFERING_START);
                    break;
                case 32774:
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    setBuffering(false);
                    sendMessage(MEDIA_INFO_BUFFERING_END);
                    break;
            }
            return false;
        }

        /**
         * 是否处于缓冲状态
         *
         * @return
         */
        public boolean isBuffering() {
            return isBuffering;
        }

        /**
         * 设置缓冲状态
         *
         * @param is 是否缓冲
         */
        public void setBuffering(boolean is) {
            isBuffering = is;
        }

        /**
         * 获取播放错误信息并发送给界面
         */
        public boolean onError(MediaPlayer mp, int what, int extra) {
            setPlayerState(PlayState.ERROR);
            if (MediaPlayer.MEDIA_ERROR_UNKNOWN == what) {
                Log.d(TAG, "====MEDIA_ERROR_UNKNOWN, extra:" + extra);
            } else if (MediaPlayer.MEDIA_ERROR_SERVER_DIED == what) {
                Log.d(TAG, "====MEDIA_ERROR_SERVER_DIED, extra:" + extra);
            } else {
                Log.d(TAG, "====onError, what:" + what + "  extra:" + extra);
            }
            sendMessage(MEDIA_PLAYER_ERROR);
            mp.reset();
            setPlayerState(PlayState.IDLE);
            return false;
        }

        /**
         * 播放完成并通知界面
         */
        public void onCompletion(MediaPlayer mp) {
            //避免有两次回调
            if (PlayState.PLAYBACK_COMPLETED == getPlayState()) {
                return;
            }
            setPlayerState(PlayState.PLAYBACK_COMPLETED);
            purgeUpdateProgress();
            sendMessage(MEDIA_PLAYER_COMPLETED);
            Log.d(TAG, "====onCompletion");
        }

        /**
         * 开始缓冲数据并通知界面
         */
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.d(TAG, "buffering update:  " + percent);
            setBuffer(percent);
        }

        /**
         * 获取缓冲数据
         *
         * @return 缓冲
         */
        public int getBuffer() {
            return bufProgress;
        }

        /**
         * 设置缓冲数据
         *
         * @param buffer 缓冲
         */
        private void setBuffer(int buffer) {
            bufProgress = buffer;
        }

        /**
         * 设置当前播放器状态
         *
         * @param state 播放状态
         */
        private boolean setPlayerState(PlayState state) {
            if ((null == mState || PlayState.END == mState) && PlayState.IDLE == state) {
                mState = state;
                return true;
            } else if (PlayState.RESETTING == state) {
                mState = state;
                return true;
            } else if (PlayState.RESETTING == mState && PlayState.RESETTED == state) {
                mState = state;
                return true;
            } else if (PlayState.RESETTED == mState && PlayState.IDLE == state) {
                mState = state;
                return true;
            } else if (PlayState.IDLE == mState && PlayState.INITIALIZED == state) {
                mState = state;
                return true;
            } else if (PlayState.INITIALIZED == mState && (PlayState.PREPARING == state || PlayState.PREPARED == state)) {
                mState = state;
                return true;
            } else if (PlayState.PREPARING == mState && PlayState.PREPARED == state) {
                mState = state;
                return true;
            } else if (PlayState.PREPARED == mState && (PlayState.STOPPED == state || PlayState.STARTED == state || PlayState.PREPARED == state)) {
                mState = state;
                return true;
            } else if (PlayState.STARTED == mState && (PlayState.STOPPED == state || PlayState.STARTED == state || PlayState.PAUSED == state || PlayState.PLAYBACK_COMPLETED == state)) {
                mState = state;
                return true;
            } else if (PlayState.PAUSED == mState && (PlayState.STOPPED == state || PlayState.STARTED == state || PlayState.PAUSED == state)) {
                mState = state;
                return true;
            } else if (PlayState.PLAYBACK_COMPLETED == mState && (PlayState.STOPPED == state || PlayState.STARTED == state || PlayState.PLAYBACK_COMPLETED == state)) {
                mState = state;
                return true;
            } else if (PlayState.STOPPED == mState && (PlayState.PREPARING == state || PlayState.PREPARED == state || PlayState.STOPPED == state)) {
                mState = state;
                return true;
            } else if (PlayState.ERROR == mState && PlayState.IDLE == state) {
                mState = state;
                return true;
            } else if (null != mState && (PlayState.ERROR == state || PlayState.END == state)) {
                mState = state;
                return true;
            }
            return false;
        }


        /**
         * 获取总时长
         *
         * @return 总时长
         */
        public synchronized int getDuration() {
            int duration = 0;
            if (mState == PlayState.PREPARED
                    || mState == PlayState.STARTED
                    || mState == PlayState.PAUSED
                    || mState == PlayState.STOPPED
                    || mState == PlayState.PLAYBACK_COMPLETED) {
                duration = mp.getDuration();
            }
            return duration;
        }

        /**
         * 获取当前进度
         *
         * @return 当前进度
         */
        public synchronized int getCurrentPosition() {
            int position = 0;
            if (mState == PlayState.INITIALIZED
                    || mState == PlayState.PREPARED
                    || mState == PlayState.STARTED
                    || mState == PlayState.PAUSED
                    || mState == PlayState.STOPPED) {
                position = mp.getCurrentPosition();
            } else if (mState == PlayState.PLAYBACK_COMPLETED) {
                position = mp.getDuration();
            }
            return position;
        }

        private void sendMessage(int what) {
            if (null == eventHandler) return;
            eventHandler.sendEmptyMessage(what);
        }
    }
}
