package io.github.ikunkk02.elytraoverdrive.breach;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class BreachTags {
	public static final TagKey<Block> PROTECTED = TagKey.create(Registries.BLOCK, ElytraOverdrive.id("breach_protected"));

	private BreachTags() {
	}
}
