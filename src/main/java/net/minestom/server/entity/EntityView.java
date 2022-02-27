package net.minestom.server.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class EntityView {
    private static final int RANGE = MinecraftServer.getEntityViewDistance() * 16;
    private final Entity entity;
    private final Set<Player> manualViewers = new HashSet<>();

    // Decide if this entity should be viewable to X players
    public final Option<Player> viewableOption;
    // Decide if this entity should view X entities
    public final Option<Entity> viewerOption;

    final Set<Player> set = new SetImpl();
    private final Object mutex = this;

    private volatile TrackedLocation trackedLocation;

    public EntityView(Entity entity) {
        this.entity = entity;
        this.viewableOption = new Option<>(EntityTracker.Target.PLAYERS, Entity::autoViewEntities,
                player -> {
                    // Add viewable
                    var lock1 = player.getEntityId() < entity.getEntityId() ? player : entity;
                    var lock2 = lock1 == entity ? player : entity;
                    synchronized (lock1.viewEngine.mutex) {
                        synchronized (lock2.viewEngine.mutex) {
                            if (!entity.viewEngine.viewableOption.predicate(player) ||
                                    !player.viewEngine.viewerOption.predicate(entity)) return;
                            entity.viewEngine.viewableOption.register(player);
                            player.viewEngine.viewerOption.register(entity);
                        }
                    }
                    entity.updateNewViewer(player);
                }, player -> {
            // Remove viewable
            var lock1 = player.getEntityId() < entity.getEntityId() ? player : entity;
            var lock2 = lock1 == entity ? player : entity;
            synchronized (lock1.viewEngine.mutex) {
                synchronized (lock2.viewEngine.mutex) {
                    entity.viewEngine.viewableOption.unregister(player);
                    player.viewEngine.viewerOption.unregister(entity);
                }
            }
            entity.updateOldViewer(player);
        });
        this.viewerOption = new Option<>(EntityTracker.Target.ENTITIES, Entity::isAutoViewable,
                entity instanceof Player player ? e -> e.viewEngine.viewableOption.addition.accept(player) : null,
                entity instanceof Player player ? e -> e.viewEngine.viewableOption.removal.accept(player) : null);
    }

    public void updateTracker(@Nullable Instance instance, @NotNull Point point) {
        this.trackedLocation = instance != null ? new TrackedLocation(instance, point) : null;
    }

    record TrackedLocation(Instance instance, Point point) {
    }

    public boolean manualAdd(@NotNull Player player) {
        if (player == this.entity) return false;
        synchronized (mutex) {
            if (manualViewers.add(player)) {
                viewableOption.bitSet.add(player.getEntityId());
                return true;
            }
            return false;
        }
    }

    public boolean manualRemove(@NotNull Player player) {
        if (player == this.entity) return false;
        synchronized (mutex) {
            if (manualViewers.remove(player)) {
                viewableOption.bitSet.remove(player.getEntityId());
                return true;
            }
            return false;
        }
    }

    public void forManuals(@NotNull Consumer<Player> consumer) {
        synchronized (mutex) {
            this.manualViewers.forEach(consumer);
        }
    }

    public boolean hasPredictableViewers() {
        // Verify if this entity's viewers can be predicted from surrounding entities
        synchronized (mutex) {
            return viewableOption.isAuto() && manualViewers.isEmpty();
        }
    }

    public void handleAutoViewAddition(Entity entity) {
        handleAutoView(entity, viewerOption.addition, viewableOption.addition, false);
    }

    public void handleAutoViewRemoval(Entity entity) {
        handleAutoView(entity, viewerOption.removal, viewableOption.removal, true);
    }

    private void handleAutoView(Entity entity, Consumer<Entity> viewer, Consumer<Player> viewable,
                                boolean requirement) {
        if (this.entity instanceof Player && viewerOption.isAuto() && entity.isAutoViewable()) {
            assert viewerOption.isRegistered(entity) == requirement : "Entity is already registered";
            if (viewer != null) viewer.accept(entity); // Send packet to this player
        }
        if (entity instanceof Player player && player.autoViewEntities() && viewableOption.isAuto()) {
            assert viewableOption.isRegistered(player) == requirement : "Entity is already registered";
            if (viewable != null) viewable.accept(player); // Send packet to the range-visible player
        }
    }

    public final class Option<T extends Entity> {
        @SuppressWarnings("rawtypes")
        private static final AtomicIntegerFieldUpdater<EntityView.Option> UPDATER = AtomicIntegerFieldUpdater.newUpdater(EntityView.Option.class, "auto");
        // Entities that should be tracked from this option
        private final EntityTracker.Target<T> target;
        // The condition that must be met for this option to be considered auto.
        private final Predicate<T> loopPredicate;
        // The consumers to be called when an entity is added/removed.
        public final Consumer<T> addition, removal;
        // Contains all the auto-entity ids that are viewable by this option.
        public final IntSet bitSet = new IntOpenHashSet();
        // 1 if auto, 0 if manual
        private volatile int auto = 1;
        // The custom rule used to determine if an entity is viewable.
        private Predicate<T> predicate = entity -> true;

        public Option(EntityTracker.Target<T> target, Predicate<T> loopPredicate,
                      Consumer<T> addition, Consumer<T> removal) {
            this.target = target;
            this.loopPredicate = loopPredicate;
            this.addition = addition;
            this.removal = removal;
        }

        public boolean isAuto() {
            return auto == 1;
        }

        public boolean predicate(T entity) {
            return predicate.test(entity);
        }

        public boolean isRegistered(T entity) {
            return bitSet.contains(entity.getEntityId());
        }

        public void register(T entity) {
            this.bitSet.add(entity.getEntityId());
        }

        public void unregister(T entity) {
            this.bitSet.remove(entity.getEntityId());
        }

        public void updateAuto(boolean autoViewable) {
            final boolean previous = UPDATER.getAndSet(this, autoViewable ? 1 : 0) == 1;
            if (previous != autoViewable) {
                synchronized (mutex) {
                    if (autoViewable) update(loopPredicate, addition);
                    else update(this::isRegistered, removal);
                }
            }
        }

        public void updateRule(Predicate<T> predicate) {
            synchronized (mutex) {
                this.predicate = predicate;
                updateRule0(predicate);
            }
        }

        public void updateRule() {
            synchronized (mutex) {
                updateRule0(predicate);
            }
        }

        void updateRule0(Predicate<T> predicate) {
            update(loopPredicate, entity -> {
                final boolean result = predicate.test(entity);
                if (result != isRegistered(entity)) {
                    if (result) addition.accept(entity);
                    else removal.accept(entity);
                }
            });
        }

        private void update(Predicate<T> visibilityPredicate,
                            Consumer<T> action) {
            references().forEach(entity -> {
                if (entity == EntityView.this.entity || !visibilityPredicate.test(entity)) return;
                if (entity instanceof Player player && manualViewers.contains(player)) return;
                if (entity.getVehicle() != null) return;
                action.accept(entity);
            });
        }

        private int lastSize;

        private Collection<T> references() {
            final TrackedLocation trackedLocation = EntityView.this.trackedLocation;
            if (trackedLocation == null) return List.of();
            final Instance instance = trackedLocation.instance();
            final Point point = trackedLocation.point();

            Int2ObjectOpenHashMap<T> entityMap = new Int2ObjectOpenHashMap<>(lastSize);
            instance.getEntityTracker().nearbyEntities(point, RANGE, target,
                    (entity) -> entityMap.putIfAbsent(entity.getEntityId(), entity));
            this.lastSize = entityMap.size();
            return entityMap.values();
        }
    }

    final class SetImpl extends AbstractSet<Player> {
        @Override
        public @NotNull Iterator<Player> iterator() {
            synchronized (mutex) {
                return viewableOption.bitSet.intStream()
                        .mapToObj(operand -> (Player) Entity.getEntity(operand))
                        .toList().iterator();
            }
        }

        @Override
        public int size() {
            synchronized (mutex) {
                return viewableOption.bitSet.size();
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                return viewableOption.bitSet.isEmpty();
            }
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Player player)) return false;
            synchronized (mutex) {
                return viewableOption.isRegistered(player);
            }
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                viewableOption.bitSet.forEach((int id) -> action.accept((Player) Entity.getEntity(id)));
            }
        }
    }
}