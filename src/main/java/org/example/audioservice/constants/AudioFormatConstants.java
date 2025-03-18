package org.example.audioservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AudioFormatConstants {

    private final Map<String, AudioConfig> configs;

    public AudioFormatConstants() {
        this.configs = Map.of(
                "mp3", new AudioConfig("libmp3lame", "192k", 44100, 2),
                "aac", new AudioConfig("aac", "128k", 44100, 2),
                "opus", new AudioConfig("libopus", "96k", 48000, 2),
                "flac", new AudioConfig("flac", null, 44100, 2),
                "wav", new AudioConfig("pcm_s16le", null, 44100, 2),
                "ogg", new AudioConfig("libvorbis", "160k", 44100, 2)
        );
    }

    public AudioConfig getConfig(String format) {
        return configs.getOrDefault(format, configs.get("mp3"));
    }

    @Getter
    @AllArgsConstructor
    public static class AudioConfig {
        private final String codec;
        private final String bitrate;
        private final int sampleRate;
        private final int channels;
    }
}
