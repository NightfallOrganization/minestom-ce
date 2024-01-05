package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.CompletableFuture;

/**
 * Manager for a ticket-based chunk system.
 * Every instance has a separate {@link ChunkManager}
 * {@code Instance#getChunkManager()}
 */
public interface ChunkManager {
    /**
     * Gets the list of all registered {@link ChunkStatus}es for this {@link ChunkManager}.
     * This allows adding or removing world generation/loading steps for more control.
     *
     * @return the list of all registered {@link ChunkStatus}es for this {@link ChunkManager}
     */
    @Contract(pure = true)
    ChunkStatusList getChunkStatusList();

    /**
     * Queries a chunk.
     * If the chunk is not loaded when this is called, the chunk will be loaded.
     * This will always return a fully loaded chunk with the ability to tick without problems.
     * The chunk will have a ticket added that keeps it loaded for a single tick.
     * This allows one-time operations to work flawlessly.
     * For longer/repeating operations consider adding a dedicated ticket to keep the chunk loaded for the duration of the operation.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @return a future for when the chunk is available
     */
    default CompletableFuture<Chunk> getChunk(int chunkX, int chunkZ) {
        return addTicket(chunkX, chunkZ, 0, 1).thenApply(ChunkAndTicket::chunk);
    }

    /**
     * Adds a ticket to a chunk.
     * Adding a ticket can take an undefined period of time, chunk generation might have to happen first.
     * Tickets added with this method will make the chunk fully generate.
     *
     * @param chunkX      the chunk X, in chunk coordinate space
     * @param chunkZ      the chunk Z, in chunk coordinate space
     * @param level       the level of this {@link ChunkStatus}. Effectively the radius that this ticket spreads to other chunks. 0 is a single chunks, 1 is 3x3 chunks, 2 is 5x5 chunks, etc.
     * @param ticksToLive how many ticks this ticket will stay in a chunk. If this is the only ticket for a chunk, this is how long a chunk remains loaded. Negative values are not allowed. 0 to load a chunk forever (or until ticket is removed).
     */
    default CompletableFuture<ChunkAndTicket> addTicket(int chunkX, int chunkZ, int level, int ticksToLive) {
        return addTicket(chunkX, chunkZ, getChunkStatusList().getLast(), level, ticksToLive);
    }

    /**
     * Adds a ticket to a chunk.
     * Adding a ticket can take an undefined period of time, chunk generation might have to happen first.
     * Depending on the {@link ChunkStatus} the chunk might not be loaded/generated completely.
     * Use {@link ChunkStatusList#getLast()} for safe access, use a specific {@link ChunkStatus} only if you know what you're doing.
     *
     * @param chunkX         the chunk X, in chunk coordinate space
     * @param chunkZ         the chunk Z, in chunk coordinate space
     * @param requiredStatus the {@link ChunkStatus} that is required for this ticket.
     * @param level          the level of this {@link ChunkStatus}. Effectively the radius that this ticket spreads to other chunks. 0 is a single chunks, 1 is 3x3 chunks, 2 is 5x5 chunks, etc.
     * @param ticksToLive    how many ticks this ticket will stay in a chunk. If this is the only ticket for a chunk, this is how long a chunk remains loaded. Negative values are not allowed. 0 to load a chunk forever (or until explicitly unloaded).
     * @return a future for when the ticket has been added successfully
     */
    CompletableFuture<ChunkAndTicket> addTicket(int chunkX, int chunkZ, ChunkStatus requiredStatus, int level, int ticksToLive);

    /**
     * Adds a ticket to a chunk.
     * Adding a ticket can take an undefined period of time, chunk generation might have to happen first.
     * Depending on the {@link ChunkStatus} the chunk might not be loaded/generated completely.
     * Use {@link ChunkStatusList#getLast()} for safe access, use a specific {@link ChunkStatus} only if you know what you're doing.
     *
     * @param chunkX      the chunk X, in chunk coordinate space
     * @param chunkZ      the chunk Z, in chunk coordinate space
     * @param information the ticket information
     * @return a future for when the ticket has been added successfully
     */
    default CompletableFuture<ChunkAndTicket> addTicket(int chunkX, int chunkZ, TicketInformation information) {
        return addTicket(chunkX, chunkZ, information.requiredStatus(), information.level(), information.ticksToLive());
    }

    /**
     * Removes a ticket from a chunk.
     * This should mostly be used to remove permanent {@link ChunkTicket}s.
     * Best practice is to use expiring {@link ChunkTicket}s and let the {@link ChunkManager} take care of removing them when they are no longer valid.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param ticket the {@link ChunkTicket} that should be removed
     * @return a future for when the ticket was removed.
     * @implNote ideally the ticket is removed as soon as the method returns (the future is already completed), this is not a requirement though.
     */
    CompletableFuture<Void> removeTicket(int chunkX, int chunkZ, ChunkTicket ticket);

    /**
     * Allows creating a sort of ticket-type that can be reused.
     * Might rename this to TicketType
     *
     * @see ChunkTicket
     */
    record TicketInformation(ChunkStatus requiredStatus, int level, int ticksToLive) {
    }
}
