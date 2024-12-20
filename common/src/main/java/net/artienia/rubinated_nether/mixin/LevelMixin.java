package net.artienia.rubinated_nether.mixin;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.artienia.rubinated_nether.utils.BlockUpdateListener;
import net.artienia.rubinated_nether.utils.UpdateListenerHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import java.util.Iterator;
import java.util.Set;

@Mixin(Level.class)
public abstract class LevelMixin implements UpdateListenerHolder {

	@Shadow public abstract BlockState getBlockState(BlockPos pos);

	@Unique
	private final Long2ReferenceMap<Set<BlockUpdateListener>> uwu$updateListeners = new Long2ReferenceOpenHashMap<>();

	@Override
	public void rn$addUpdateListener(BlockUpdateListener listener) {
		listener.getListenedPositions()
			.mapToLong(BlockPos::asLong)
			.forEach(l -> uwu$updateListeners.computeIfAbsent(l, $ -> new ObjectArraySet<>()).add(listener));
	}

	@Override
	public void rn$handleBlockUpdate(BlockPos pos) {
		long longPos = pos.asLong();
		Set<BlockUpdateListener> listeners = uwu$updateListeners.get(longPos);
		if(listeners != null) {
			Iterator<BlockUpdateListener> iterator = listeners.iterator();
			while (iterator.hasNext()) {
				BlockUpdateListener listener = iterator.next();
				if(listener.shouldRemove()) {
					iterator.remove();
				} else {
					listener.handleBlockUpdate((Level) (Object) this, pos, getBlockState(pos));
				}
			}
			if(listeners.isEmpty()) uwu$updateListeners.remove(longPos);
		}
	}
}
