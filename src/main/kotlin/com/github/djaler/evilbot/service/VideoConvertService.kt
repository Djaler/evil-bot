package com.github.djaler.evilbot.service

import org.springframework.stereotype.Service
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import ws.schild.jave.encode.VideoAttributes
import java.io.File

@Service
class VideoConvertService {
    fun convertToMp4(input: File, output: File) {
        val audioAttributes = AudioAttributes()
        val videoAttributes = VideoAttributes()
        val encodingAttributes = EncodingAttributes().apply {
            setAudioAttributes(audioAttributes)
            setVideoAttributes(videoAttributes)
            setOutputFormat("mp4")
        }

        val encoder = Encoder()
        encoder.encode(MultimediaObject(input), output, encodingAttributes)
    }
}
