package net.minestom.server.instance.chunksystem;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * This is a list of all {@link ChunkStatus ChunkStatuses} for an instance.
 * <p>
 * More statuses can be created using one of the create methods.
 * A {@link ChunkStatus} can be removed using {@link ChunkStatus#remove()}.
 * This is more or less a linked list of {@link ChunkStatus ChunkStatuses}
 */
public interface ChunkStatusList extends Iterable<ChunkStatusList> {

    /**
     * This will be called once the instance is being used. Will prohibit any further modification to every ChunkStatus in this list.
     * Subsequent calls to {@link #isModifiable()} will return false
     */
    @ApiStatus.Internal
    void finishSetup();

    /**
     * Checks if this {@link ChunkStatusList} can be further modified.
     * Should be true as long as this {@link ChunkStatusList} has not been used by an instance.
     *
     * @return if this {@link ChunkStatusList} can be further modified.
     */
    @Contract(pure = true)
    boolean isModifiable();

    /**
     * Checks if this is a valid {@link ChunkStatusList}.
     * A valid {@link ChunkStatusList} must contain at least one {@link ChunkStatus}.
     * That {@link ChunkStatus} may be an empty generator, but every chunk must have a status.
     * In case of exactly one {@link ChunkStatus}, {@link #getFirst()} and {@link #getLast()} will return that same {@link ChunkStatus}.
     *
     * @return if this is a valid {@link ChunkStatusList
     */
    @Contract(pure = true)
    boolean isValid();

    /**
     * Gets the first {@link ChunkStatus} in the list.
     * Every chunk should start with this status and work it's way up to {@link #getLast()}
     *
     * @return the first {@link ChunkStatus} in the list
     */
    @Contract(pure = true)
    @UnknownNullability ChunkStatus getFirst();

    /**
     * Gets the last {@link ChunkStatus} in the list.
     * Every fully loaded and ticking chunk must have this {@link ChunkStatus}.
     *
     * @return the last {@link ChunkStatus} in the list
     */
    @Contract(pure = true)
    @UnknownNullability ChunkStatus getLast();

    /**
     * Gets the {@link ChunkStatus} for a given {@link NamespaceID}
     *
     * @param key the {@link NamespaceID} that the {@link ChunkStatus} was registered by
     * @return the {@link ChunkStatus} for the given {@link NamespaceID}
     */
    @Contract(pure = true)
    @UnknownNullability ChunkStatus getChunkStatus(@NotNull NamespaceID key);

    /**
     * Creates a ChunkStatus at the beginning of this list.
     * Duplicate {@link NamespaceID keys} are not allowed.
     *
     * @param key            the key of the {@link ChunkStatus}
     * @param loadTask       the {@link ChunkLoadTask} of the {@link ChunkStatus}
     * @param generationTask the {@link ChunkGenerationTask} of the {@link ChunkStatus}
     * @return the newly created {@link ChunkStatus}
     */
    @NotNull ChunkStatus createFirst(@NotNull NamespaceID key, @NotNull ChunkLoadTask loadTask, @NotNull ChunkGenerationTask generationTask);

    /**
     * Creates a ChunkStatus at the end of this list.
     * Duplicate {@link NamespaceID keys} are not allowed.
     *
     * @param key            the key of the {@link ChunkStatus}
     * @param loadTask       the {@link ChunkLoadTask} of the {@link ChunkStatus}
     * @param generationTask the {@link ChunkGenerationTask} of the {@link ChunkStatus}
     * @return the newly created {@link ChunkStatus}
     */
    @NotNull ChunkStatus createLast(@NotNull NamespaceID key, @NotNull ChunkLoadTask loadTask, @NotNull ChunkGenerationTask generationTask);

    /**
     * Creates a ChunkStatus before another ChunkStatus.
     * Duplicate {@link NamespaceID keys} are not allowed.
     *
     * @param key            the key of the {@link ChunkStatus}
     * @param loadTask       the {@link ChunkLoadTask} of the {@link ChunkStatus}
     * @param generationTask the {@link ChunkGenerationTask} of the {@link ChunkStatus}
     * @return the newly created {@link ChunkStatus}
     */
    @NotNull ChunkStatus createBefore(@NotNull ChunkStatus status, @NotNull NamespaceID key, @NotNull ChunkLoadTask loadTask, @NotNull ChunkGenerationTask generationTask);

    /**
     * Creates a ChunkStatus after another ChunkStatus.
     * Duplicate {@link NamespaceID keys} are not allowed.
     *
     * @param key            the key of the {@link ChunkStatus}
     * @param loadTask       the {@link ChunkLoadTask} of the {@link ChunkStatus}
     * @param generationTask the {@link ChunkGenerationTask} of the {@link ChunkStatus}
     * @return the newly created {@link ChunkStatus}
     */
    @NotNull ChunkStatus createAfter(@NotNull ChunkStatus status, @NotNull NamespaceID key, @NotNull ChunkLoadTask loadTask, @NotNull ChunkGenerationTask generationTask);
}
