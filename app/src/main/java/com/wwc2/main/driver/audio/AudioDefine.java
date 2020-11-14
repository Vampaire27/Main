package com.wwc2.main.driver.audio;

import android.media.AudioManager;

/**
 * the audio define.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public class AudioDefine {

    /**
     * 音频流定义
     */
    public static class AudioStream {
        /**
         * the none audio stream.
         */
        public static final int STREAM_NONE = 0;
        /**
         * The audio stream for phone calls
         */
        public static final int STREAM_VOICE_CALL = 1;
        /**
         * The audio stream for system sounds
         */
        public static final int STREAM_SYSTEM = 2;
        /**
         * The audio stream for the phone ring and message alerts
         */
        public static final int STREAM_RING = 3;
        /**
         * The audio stream for music playback
         */
        public static final int STREAM_MUSIC = 4;
        /**
         * The audio stream for alarms
         */
        public static final int STREAM_ALARM = 5;
        /**
         * The audio stream for notifications
         */
        public static final int STREAM_NOTIFICATION = 6;
        /**
         * the audio stream for navigation.
         */
        public static final int SEREAM_NAVIGATION = 7;
        /**
         * The audio stream for DTMF tones
         */
        public static final int STREAM_DTMF = 8;
        /**
         * The audio stream for text to speech (TTS)
         */
        public static final int STREAM_TTS = 9;
        /**
         * The audio stream for Fm
         */
        public static final int STREAM_FM = 10;
        /**
         * The audio stream for MATV
         */
        public static final int STREAM_MATV = 11;
        /**
         * the defaule stream.
         */
        public static final int STREAM_DEFAULT = STREAM_NONE;

        /**
         * to string
         */
        public static final String toString(int stream) {
            String ret = null;
            switch (stream) {
                case STREAM_NONE:
                    ret = "STREAM_NONE";
                    break;
                case STREAM_VOICE_CALL:
                    ret = "STREAM_VOICE_CALL";
                    break;
                case STREAM_SYSTEM:
                    ret = "STREAM_SYSTEM";
                    break;
                case STREAM_RING:
                    ret = "STREAM_RING";
                    break;
                case STREAM_MUSIC:
                    ret = "STREAM_MUSIC";
                    break;
                case STREAM_ALARM:
                    ret = "STREAM_ALARM";
                    break;
                case STREAM_NOTIFICATION:
                    ret = "STREAM_NOTIFICATION";
                    break;
                case SEREAM_NAVIGATION:
                    ret = "SEREAM_NAVIGATION";
                    break;
                case STREAM_DTMF:
                    ret = "STREAM_DTMF";
                    break;
                case STREAM_TTS:
                    ret = "STREAM_TTS";
                    break;
                case STREAM_FM:
                    ret = "STREAM_FM";
                    break;
                case STREAM_MATV:
                    ret = "STREAM_MATV";
                    break;
                default:
                    break;
            }
            return ret;
        }
    }

    /**
     * 音频焦点定义
     */
    public static class AudioFocus {
        /**
         * Used to indicate no audio focus has been gained or lost.
         */
        public static final int AUDIOFOCUS_NONE = 0;

        /**
         * Used to indicate a gain of audio focus, or a request of audio focus, of unknown duration.
         */
        public static final int AUDIOFOCUS_GAIN = 1;
        /**
         * Used to indicate a temporary gain or request of audio focus, anticipated to last a short
         * amount of time. Examples of temporary changes are the playback of driving directions, or an
         * event notification.
         */
        public static final int AUDIOFOCUS_GAIN_TRANSIENT = 2;
        /**
         * Used to indicate a temporary request of audio focus, anticipated to last a short
         * amount of time, and where it is acceptable for other audio applications to keep playing
         * after having lowered their output level (also referred to as "ducking").
         * Examples of temporary changes are the playback of driving directions where playback of music
         * in the background is acceptable.
         */
        public static final int AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3;
        /**
         * Used to indicate a temporary request of audio focus, anticipated to last a short
         * amount of time, during which no other applications, or system components, should play
         * anything. Examples of exclusive and transient audio focus requests are voice
         * memo recording and speech recognition, during which the system shouldn't play any
         * notifications, and media playback should have paused.
         */
        public static final int AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE = 4;
        /**
         * Used to indicate a loss of audio focus of unknown duration.
         */
        public static final int AUDIOFOCUS_LOSS = -1 * AUDIOFOCUS_GAIN;
        /**
         * Used to indicate a transient loss of audio focus.
         */
        public static final int AUDIOFOCUS_LOSS_TRANSIENT = -1 * AUDIOFOCUS_GAIN_TRANSIENT;
        /**
         * Used to indicate a transient loss of audio focus where the loser of the audio focus can
         * lower its output volume if it wants to continue playing (also referred to as "ducking"), as
         * the new focus owner doesn't require others to be silent.
         */
        public static final int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK =
                -1 * AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
        /**
         * default
         */
        public static final int AUDIOFOCUS_DEFAULT = AUDIOFOCUS_NONE;

        /**
         * to string.
         */
        public static final String toString(int focus) {
            String ret = null;
            switch (focus) {
                case AUDIOFOCUS_NONE:
                    ret = "AUDIOFOCUS_NONE";
                    break;
                case AUDIOFOCUS_GAIN:
                    ret = "AUDIOFOCUS_GAIN";
                    break;
                case AUDIOFOCUS_GAIN_TRANSIENT:
                    ret = "AUDIOFOCUS_GAIN_TRANSIENT";
                    break;
                case AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    ret = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                    break;
                case AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                    ret = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
                    break;
                case AUDIOFOCUS_LOSS:
                    ret = "AUDIOFOCUS_LOSS";
                    break;
                case AUDIOFOCUS_LOSS_TRANSIENT:
                    ret = "AUDIOFOCUS_LOSS_TRANSIENT";
                    break;
                case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    ret = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
                default:
                    break;
            }
            return ret;
        }
    }

    /**
     * 音频状态定义
     */
    public static class AudioStatus {
        /**
         * A failed focus change request.
         */
        public static final int AUDIOFOCUS_REQUEST_FAILED = 0;
        /**
         * A successful focus change request.
         */
        public static final int AUDIOFOCUS_REQUEST_GRANTED = 1;
        /**
         * @hide A focus change request whose granting is delayed: the request was successful, but the
         * requester will only be granted audio focus once the condition that prevented immediate
         * granting has ended.
         */
        public static final int AUDIOFOCUS_REQUEST_DELAYED = 2;
        /**
         * default
         */
        public static final int AUDIOFOCUS_REQUEST_DEFAULT = AUDIOFOCUS_REQUEST_FAILED;

        /**
         * to string.
         */
        public static final String toString(int status) {
            String ret = null;
            switch (status) {
                case AUDIOFOCUS_REQUEST_FAILED:
                    ret = "AUDIOFOCUS_REQUEST_FAILED";
                    break;
                case AUDIOFOCUS_REQUEST_GRANTED:
                    ret = "AUDIOFOCUS_REQUEST_GRANTED";
                    break;
                case AUDIOFOCUS_REQUEST_DELAYED:
                    ret = "AUDIOFOCUS_REQUEST_DELAYED";
                    break;
                default:
                    break;
            }
            return ret;
        }
    }

    /**
     * 根据系统音频流获取APP音频流
     */
    public static int getAudioStream(int stream) {
        int ret = AudioDefine.AudioStream.STREAM_DEFAULT;
        switch (stream) {
            case AudioManager.STREAM_VOICE_CALL:
                ret = AudioDefine.AudioStream.STREAM_VOICE_CALL;
                break;
            case AudioManager.STREAM_SYSTEM:
                ret = AudioDefine.AudioStream.STREAM_SYSTEM;
                break;
            case AudioManager.STREAM_RING:
                ret = AudioDefine.AudioStream.STREAM_RING;
                break;
            case AudioManager.STREAM_MUSIC:
                ret = AudioDefine.AudioStream.STREAM_MUSIC;
                break;
            case AudioManager.STREAM_ALARM:
                ret = AudioDefine.AudioStream.STREAM_ALARM;
                break;
            case AudioManager.STREAM_NOTIFICATION:
                ret = AudioDefine.AudioStream.STREAM_NOTIFICATION;
                break;
            case AudioManager.STREAM_DTMF:
                ret = AudioDefine.AudioStream.STREAM_DTMF;
                break;
            case 9:
                ret = AudioDefine.AudioStream.STREAM_TTS;
                break;
            case 10:
                ret = AudioDefine.AudioStream.STREAM_FM;
                break;
            case 11:
                ret = AudioDefine.AudioStream.STREAM_MATV;
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * 根据APP音频流获取系统音频流
     */
    public static int getSystemAudioStream(int stream) {
        int ret = -1;//AudioManager.STREAM_DEFAULT
        switch (stream) {
            case AudioDefine.AudioStream.STREAM_VOICE_CALL:
                ret = AudioManager.STREAM_VOICE_CALL;
                break;
            case AudioDefine.AudioStream.STREAM_SYSTEM:
                ret = AudioManager.STREAM_SYSTEM;
                break;
            case AudioDefine.AudioStream.STREAM_RING:
                ret = AudioManager.STREAM_RING;
                break;
            case AudioDefine.AudioStream.STREAM_MUSIC:
                ret = AudioManager.STREAM_MUSIC;
                break;
            case AudioDefine.AudioStream.STREAM_ALARM:
                ret = AudioManager.STREAM_ALARM;
                break;
            case AudioDefine.AudioStream.STREAM_NOTIFICATION:
                ret = AudioManager.STREAM_NOTIFICATION;
                break;
            case AudioDefine.AudioStream.STREAM_DTMF:
                ret = AudioManager.STREAM_DTMF;
                break;
            case AudioDefine.AudioStream.STREAM_TTS:
                ret = 9;
                break;
            case AudioDefine.AudioStream.STREAM_FM:
                ret = 10;
                break;
            case AudioDefine.AudioStream.STREAM_MATV:
                ret = 11;
                break;
            default:
                break;
        }
        return ret;
    }

    /***
     * 根据系统音频聚焦获取APP音频聚焦
     */
    public static int getAudioFocus(int focus) {
        int ret = AudioDefine.AudioFocus.AUDIOFOCUS_DEFAULT;
        switch (focus) {
            case AudioManager.AUDIOFOCUS_GAIN:
                ret = AudioDefine.AudioFocus.AUDIOFOCUS_GAIN;
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                ret = AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT;
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                ret = AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                ret = AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                ret = AudioDefine.AudioFocus.AUDIOFOCUS_LOSS;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                ret = AudioDefine.AudioFocus.AUDIOFOCUS_LOSS_TRANSIENT;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                ret = AudioDefine.AudioFocus.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                break;
            default:
                break;
        }
        return ret;
    }

    /***
     * 根据APP音频聚焦获取系统音频聚焦
     */
    public static int getSystemAudioFocus(int focus) {
        int ret = -1;//AudioManager.AUDIOFOCUS_NONE;
        switch (focus) {
            case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN:
                ret = AudioManager.AUDIOFOCUS_GAIN;
                break;
            case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT:
                ret = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                break;
            case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                ret = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
                break;
            case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                ret = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                break;
            case AudioDefine.AudioFocus.AUDIOFOCUS_LOSS:
                ret = AudioManager.AUDIOFOCUS_LOSS;
                break;
            case AudioDefine.AudioFocus.AUDIOFOCUS_LOSS_TRANSIENT:
                ret = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                break;
            case AudioDefine.AudioFocus.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                ret = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * 根据系统音频状态获取APP音频状态
     */
    public static int getAudioStatus(int status) {
        int ret = AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_DEFAULT;
        switch (status) {
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                ret = AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_FAILED;
                break;
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                ret = AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_GRANTED;
                break;
            case 2:
                ret = AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_DELAYED;
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * 根据APP音频状态获取系统音频状态
     */
    public static int getSystemAudioStatus(int status) {
        int ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        switch (status) {
            case AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_FAILED:
                ret = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
                break;
            case AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_GRANTED:
                ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
                break;
            case AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_DELAYED:
                ret = 2;
                break;
            default:
                break;
        }
        return ret;
    }
}
