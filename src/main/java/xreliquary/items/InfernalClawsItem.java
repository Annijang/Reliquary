package xreliquary.items;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import xreliquary.handler.CommonEventHandler;
import xreliquary.handler.HandlerPriority;
import xreliquary.handler.IPlayerHurtHandler;
import xreliquary.init.ModItems;
import xreliquary.reference.Settings;
import xreliquary.util.InventoryHelper;

public class InfernalClawsItem extends ItemBase {
	public InfernalClawsItem() {
		super(new Properties().stacksTo(1));

		CommonEventHandler.registerPlayerHurtHandler(new IPlayerHurtHandler() {
			@Override
			public boolean canApply(Player player, LivingAttackEvent event) {
				return (event.getSource() == DamageSource.IN_FIRE || event.getSource() == DamageSource.ON_FIRE)
						&& player.getFoodData().getFoodLevel() > 0
						&& InventoryHelper.playerHasItem(player, ModItems.INFERNAL_CLAWS.get());

			}

			@Override
			public boolean apply(Player player, LivingAttackEvent event) {
				player.causeFoodExhaustion(event.getAmount() * ((float) Settings.COMMON.items.infernalClaws.hungerCostPercent.get() / 100F));
				return true;
			}

			@Override
			public HandlerPriority getPriority() {
				return HandlerPriority.HIGH;
			}
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	// this item's effects are handled in events
}
