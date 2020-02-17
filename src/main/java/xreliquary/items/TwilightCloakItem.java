package xreliquary.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xreliquary.reference.Settings;
import xreliquary.util.InventoryHelper;

public class TwilightCloakItem extends ToggleableItem {

	public TwilightCloakItem() {
		super("twilight_cloak", new Properties().maxStackSize(1));
		MinecraftForge.EVENT_BUS.register(this);
	}


	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.EPIC;
	}

	@Override
	public void inventoryTick(ItemStack twilightCloak, World world, Entity entity, int itemSlot, boolean isSelected) {
		if(!(entity instanceof PlayerEntity)) {
			return;
		}

		updateInvisibility(twilightCloak, (PlayerEntity) entity);
	}

	private void updateInvisibility(ItemStack twilightCloak, PlayerEntity player) {
		if(!isEnabled(twilightCloak)) {
			return;
		}

		//toggled effect, makes player invisible based on light level (configurable)

		if(player.world.getLight(new BlockPos(player.getPosition())) > Settings.COMMON.items.twilightCloak.maxLightLevel.get()) {
			return;
		}

		//checks if the effect would do anything. Literally all this does is make the player invisible. It doesn't interfere with mob AI.
		//for that, we're attempting to use an event handler.
		EffectInstance quickInvisibility = new EffectInstance(Effects.INVISIBILITY, 2, 0, false, false);
		player.addPotionEffect(quickInvisibility);
	}

/* TODO implement Bauble replacement
	@Override
	@Optional.Method(modid = Compatibility.MOD_ID.BAUBLES)
	public BaubleType getBaubleType(ItemStack itemStack) {
		return BaubleType.BODY;
	}

	@Override
	@Optional.Method(modid = Compatibility.MOD_ID.BAUBLES)
	public void onWornTick(ItemStack twilightCloak, LivingEntity player) {
		updateInvisibility(twilightCloak, (PlayerEntity) player);
	}
*/

	@SubscribeEvent
	public void onEntityTargetedEvent(LivingSetAttackTargetEvent event) {
		doTwilightCloakCheck(event);
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		doTwilightCloakCheck(event);
	}

	private void doTwilightCloakCheck(LivingEvent event) {
		if(event.getEntity() instanceof MobEntity) {
			MobEntity entityLiving = ((MobEntity) event.getEntity());
			if (!(entityLiving.getAttackTarget() instanceof PlayerEntity)) {
				return;
			}
			PlayerEntity player = (PlayerEntity) entityLiving.getAttackTarget();
			if (!InventoryHelper.playerHasItem(player, this, true) || player.world.getLight(player.getPosition()) > Settings.COMMON.items.twilightCloak.maxLightLevel.get()) {
				return;
			}

			if(event.getEntity() instanceof MobEntity) {
				((MobEntity) event.getEntity()).setAttackTarget(null);
			}
		}
	}
}
