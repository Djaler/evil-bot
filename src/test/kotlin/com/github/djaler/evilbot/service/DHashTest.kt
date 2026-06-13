package com.github.djaler.evilbot.service

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.MemoryCacheImageOutputStream
import kotlin.random.Random

class DHashTest {

    @Test
    fun `recompressed copy is close to original`() {
        val original = testImage(seed = 1)
        val recompressed = recompressJpeg(original, quality = 0.3f)

        val distance = hammingDistance(dHash(original), dHash(recompressed))

        distance shouldBeLessThanOrEqual 5
    }

    @Test
    fun `resized copy is close to original`() {
        val original = testImage(seed = 1)
        val resized = resize(original, 200, 150)

        val distance = hammingDistance(dHash(original), dHash(resized))

        distance shouldBeLessThanOrEqual 5
    }

    @Test
    fun `different images are far apart`() {
        val distance = hammingDistance(dHash(testImage(seed = 1)), dHash(testImage(seed = 2)))

        distance shouldBeGreaterThan 10
    }

    @Test
    fun `solid color image hashes without error`() {
        dHash(BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
    }

    @Test
    fun `custom type image hashes without error`() {
        val image = customTypeImage()
        image.type shouldBe BufferedImage.TYPE_CUSTOM

        dHash(image)
    }

    private fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

    private fun testImage(seed: Int): BufferedImage {
        val random = Random(seed)
        val image = BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        repeat(30) {
            graphics.color = Color(random.nextInt(0xFFFFFF))
            graphics.fillRect(
                random.nextInt(280),
                random.nextInt(200),
                40 + random.nextInt(80),
                40 + random.nextInt(80)
            )
        }
        graphics.dispose()
        return image
    }

    private fun resize(image: BufferedImage, width: Int, height: Int): BufferedImage {
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = resized.createGraphics()
        graphics.drawImage(image, 0, 0, width, height, null)
        graphics.dispose()
        return resized
    }

    private fun recompressJpeg(image: BufferedImage, quality: Float): BufferedImage {
        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val output = ByteArrayOutputStream()
        writer.output = MemoryCacheImageOutputStream(output)
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality
        }
        writer.write(null, IIOImage(image, null, null), param)
        writer.dispose()
        return ImageIO.read(ByteArrayInputStream(output.toByteArray()))
    }

    private fun customTypeImage(): BufferedImage {
        val colorModel = ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            intArrayOf(16, 16, 16),
            false,
            false,
            Transparency.OPAQUE,
            DataBuffer.TYPE_USHORT
        )
        val raster = colorModel.createCompatibleWritableRaster(50, 50)
        return BufferedImage(colorModel, raster, false, null)
    }
}
