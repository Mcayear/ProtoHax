package dev.sora.relay.game.world.chunk

import dev.sora.relay.game.registry.BlockMapping
import dev.sora.relay.game.registry.LegacyBlockMapping
import io.netty.buffer.ByteBuf
import kotlin.math.abs

class Chunk(val x: Int, val z: Int, val is384World: Boolean,
            private val blockMapping: BlockMapping, private val legacyBlockMapping: LegacyBlockMapping) {

    val hash: Long
        get() = hash(x, z)

    val sectionStorage = Array(if (is384World) 24 else 16) { ChunkSection(blockMapping, legacyBlockMapping) }
    val maximumHeight = sectionStorage.size * 16

    fun isInRadius(playerChunkX: Int, playerChunkZ: Int, radius: Int): Boolean {
        return abs(x - playerChunkX) <= radius && abs(z - playerChunkZ) <= radius
    }

    fun read(buf: ByteBuf, subChunks: Int) {
        repeat(subChunks) {
            readSubChunk(it, buf)
        }
    }

    fun readSubChunk(index: Int, buf: ByteBuf) {
        sectionStorage[index].read(buf)
    }

    fun getBlockAt(x: Int, yIn: Int, z: Int): Int {
        val y = if(is384World) yIn + 64 else yIn
        assert(y in 0..maximumHeight)

        return sectionStorage[y shr 4].getBlockAt(x, y and 0x0f, z)
    }

    fun setBlockAt(x: Int, yIn: Int, z: Int, runtimeId: Int) {
        val y = if(is384World) yIn + 64 else yIn
        assert(y in 0..maximumHeight)

        sectionStorage[y shr 4].setBlockAt(x, y and 0x0f, z, runtimeId)
    }

    companion object {
        fun hash(x: Int, z: Int): Long {
            return x.toLong() shl 32 or (z.toLong() and 0xffffffffL)
        }
    }
}
