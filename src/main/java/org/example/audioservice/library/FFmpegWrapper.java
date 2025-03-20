package org.example.audioservice.library;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import org.example.audioservice.constant.AudioFormatConstants;
import org.example.audioservice.controller.FileController;
import org.example.audioservice.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
public class FFmpegWrapper {

    private static final Logger Log = LoggerFactory.getLogger(FileController.class);
    private final AudioFormatConstants audioFormatConstants;

    public FFmpegWrapper(AudioFormatConstants audioFormatConstants) {
        this.audioFormatConstants = audioFormatConstants;
    }

    public File convertAudio(File inputFile, String outputFormat) {
        String outputFileName = inputFile.getName().replaceAll("\\.[^.]+$", "") + "." + outputFormat;
        Path outputPath = inputFile.toPath().getParent().resolve(outputFileName);

        Log.info("Starting conversion to format: {}", outputFormat);

        try {
            AudioFormatConstants.AudioConfig config = audioFormatConstants.getConfig(outputFormat);

            FFmpeg.atPath()
                    .addInput(UrlInput.fromPath(inputFile.toPath()))
                    .addOutput(setAudioEncoding(UrlOutput.toPath(outputPath), config))
                    .execute();

            Log.info("Converted file saved to: {}", outputPath.toString());
            return outputPath.toFile();

        } catch (Exception e) {
            Log.error("FFmpeg conversion failed: {}", e.getMessage());
            throw new StorageException("Failed to convert file: " + e.getMessage());
        }
    }

    private UrlOutput setAudioEncoding(UrlOutput output, AudioFormatConstants.AudioConfig config) {
        output.addArguments("-c:a", config.getCodec());
        if (config.getBitrate() != null) {
            output.addArguments("-b:a", config.getBitrate());
        }
        output.addArguments("-ac", String.valueOf(config.getChannels()));
        output.addArguments("-ar", String.valueOf(config.getSampleRate()));
        return output;
    }
}
