package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ChunkTicket} is an object that can keep a chunk loaded/load new chunks.
 * {@link ChunkTicket}s are usually specific for a chunk, meaning it is for chunk (X,Z) and only for that chunk.
 * <p>
 * A chunk can have multiple identical {@link ChunkTicket}s, so that {@code ticket1.equals(ticket2)} equals {@code true}.
 * In case of explicit removal, only one {@link ChunkTicket} may be removed from the chunk (per call).
 * <p>
 * The {@link Chunk} reference is not in this class, but rather in {@link ChunkAndTicket}.
 * This is to reduce the memory taken up by these tickets, and to reduce references to a {@link Chunk}
 *
 * @param requiredStatus the {@link ChunkStatus} that is required for this ticket.
 * @param level          the level of this {@link ChunkStatus}. Effectively the radius that this ticket spreads to other chunks. 0 is a single chunks, 1 is 3x3 chunks, 2 is 5x5 chunks, etc.
 * @param ticksToLive    how many ticks this ticket will stay in a chunk. If this is the only ticket for a chunk, this is how long a chunk remains loaded. Negative values are not allowed. 0 to load a chunk forever (or until explicitly unloaded).
 * @param tickAdded      at which tick this {@link ChunkTicket} was added to a chunk.
 */
public record ChunkTicket(ChunkStatus requiredStatus, int level, int ticksToLive,
                          int tickAdded) implements Comparable<ChunkTicket> {
    /**
     * Compares two ChunkTickets by their level in order to quickly access the ChunkTicket with the highest level from a sorted collection.
     * This will be used mostly by the chunk system to determine which level to propagate to neighbouring chunks.
     */
    @Override
    public int compareTo(@NotNull ChunkTicket o) {
        assert requiredStatus == o.requiredStatus; // Should never compare tickets with differing ChunkStatus, leave out extra comparison because this might be called a lot.
        return Integer.compare(level, o.level);
    }
}
