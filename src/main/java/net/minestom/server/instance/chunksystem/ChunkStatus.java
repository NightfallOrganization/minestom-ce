package net.minestom.server.instance.chunksystem;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a {@link ChunkStatus} for world generation.
 * <p>
 * Multiple statuses may be used to generate a Chunk up to a specific point.
 * This is mostly useful for more complex world generators.
 * World generators can decide to do their generation in a single step (ChunkStatus).
 */
public interface ChunkStatus extends Comparable<ChunkStatus> {

    /**
     * Gets the {@link NamespaceID} this {@link ChunkStatus} was registered by
     *
     * @return the {@link NamespaceID} of this {@link ChunkStatus}
     */
    @NotNull NamespaceID getKey();

    /**
     * Gets the {@link ChunkLoadTask loadTask} of this {@link ChunkStatus}
     *
     * @return the {@link ChunkLoadTask loadTask} of this {@link ChunkStatus}
     */
    @NotNull ChunkLoadTask getLoadTask();

    /**
     * Gets the {@link ChunkGenerationTask generationTask} of this {@link ChunkStatus}
     *
     * @return the {@link ChunkGenerationTask generationTask} of this {@link ChunkStatus}
     */
    @NotNull ChunkGenerationTask getGenerationTask();

    /**
     * Gets the {@link ChunkStatusList} this {@link ChunkStatus} is registered to
     *
     * @return the {@link ChunkStatusList} this {@link ChunkStatus} is registered to
     */
    @NotNull ChunkStatusList getChunkStatusList();

    /**
     * Gets the {@link ChunkStatus} before this one.
     *
     * @return the {@link ChunkStatus} before this one.
     */
    @Nullable ChunkStatus getPreviousStatus();

    /**
     * Gets the {@link ChunkStatus} after this one.
     *
     * @return the {@link ChunkStatus} after this one.
     */
    @Nullable ChunkStatus getNextStatus();

    /**
     * Checks whether this {@link ChunkStatus} is valid, or has been removed from the {@link ChunkStatusList}
     *
     * @return if this {@link ChunkStatus} is valid
     */
    boolean isValid();

    /**
     * Removes this {@link ChunkStatus} from the {@link ChunkStatusList}.
     * <p>The previous and next {@link ChunkStatus ChunkStatuses} are linked to eachother.</p>
     * <p>Subsequent calls to {@link #isValid()} will return false.</p>
     * <p>Subsequent calls to {@link #getPreviousStatus()}, {@link #getNextStatus()}, {@link #getChunkStatusList()}, {@link #remove()} and {@link #compareTo(ChunkStatus)} will throw {@link IllegalStateException}</p>
     */
    void remove();


    /**
     * Compares this {@link ChunkStatus} to another.
     * This allows analyzing the position in the {@link ChunkStatusList} of one {@link ChunkStatus} relative to another.
     */
    @Override
    int compareTo(@NotNull ChunkStatus status);
}
