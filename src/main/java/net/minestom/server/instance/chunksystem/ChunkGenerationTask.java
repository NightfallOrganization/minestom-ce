package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.GenerationUnit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A generationTask for the chunk system.
 * <p>
 * This allows to hook into chunks that were newly generated (not able to load up to this step with {@link IChunkLoader}).
 * Be advised that the chunk might have been loaded up to a specific {@link ChunkStatus} that is lower than the {@link ChunkStatus} passed to {@link #generateChunk(Instance, Chunk, int, int, Executor, ChunkStatus, GenerationUnit) generateChunk}.
 * Querying a chunk may be a combination of loading existing data and generating new data.
 */
public interface ChunkGenerationTask {
    /**
     * Called for chunk generation at the given {@code chunkStatus}.
     * Generating in the caller thread is recommended, though the option for async generation is provided through a future.
     * An executor is provided for submitting tasks. The executor should respect the priority of the chunk.
     *
     * @param instance    the instance the chunk is generated for
     * @param chunk       the chunk that is being generated. Probably not fully filled with all data. RO-Access is recommended. MAY BREAK. USE WITH CAUTION.
     * @param chunkX      the chunk X, in chunk coordinate space
     * @param chunkZ      the chunk Z, in chunk coordinate space
     * @param executor    an {@link Executor} for heavy work after an async IO operation (or similar)
     * @param chunkStatus the status the chunk is currently in
     * @param unit        a {@link GenerationUnit} to generate the world
     * @return a future for when generation has finished
     */
    @NotNull CompletableFuture<Void> generateChunk(@NotNull Instance instance, @NotNull Chunk chunk, int chunkX, int chunkZ, @NotNull Executor executor, @NotNull ChunkStatus chunkStatus, GenerationUnit unit);
}
