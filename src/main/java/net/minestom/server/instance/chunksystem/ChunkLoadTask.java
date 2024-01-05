package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A loadTask for the chunk system.
 * <p>This allows to hook into chunks that were loaded from a {@link IChunkLoader ChunkLoader} to modify chunk data.</p>
 * <p><b>NOTE</b>: A {@link Chunk} might not be fully filled with data, as other {@link ChunkLoadTask loadTasks} might modify the chunk further.</p>
 * <p><b>NOTE</b>: A {@link Chunk} might be filled with data from an even newer {@link ChunkStatus}. The optimal use case for this is to generate data which isn't saved to disk, light being one possibility.</p>
 * This will be called when a chunk has been loaded from a {@link IChunkLoader ChunkLoader}, but not when it is newly generated.
 * Use {@link ChunkGenerationTask} to hook into generation.
 */
@FunctionalInterface
public interface ChunkLoadTask {
    /**
     * Called when a chunk has been loaded from a {@link IChunkLoader ChunkLoader} with up to at least the given chunkStatus.
     *
     * @param instance    the instance the chunk is loaded for
     * @param chunk       the chunk that is being loaded. Probably not be fully filled with all data. USE WITH CAUTION
     * @param chunkX      the chunk X, in chunk coordinate space
     * @param chunkZ      the chunk Z, in chunk coordinate space
     * @param executor    an {@link Executor} for heavy work after an async IO operation (or similar)
     * @param chunkStatus the status the chunk is currently in
     * @return a future for this task.
     */
    @NotNull CompletableFuture<Void> loadChunk(@NotNull Instance instance, @NotNull Chunk chunk, int chunkX, int chunkZ, @NotNull Executor executor, @NotNull ChunkStatus chunkStatus);
}
