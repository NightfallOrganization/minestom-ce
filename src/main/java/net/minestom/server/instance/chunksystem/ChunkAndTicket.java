package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;

public record ChunkAndTicket(ChunkTicket chunkTicket, Chunk chunk) {
}
