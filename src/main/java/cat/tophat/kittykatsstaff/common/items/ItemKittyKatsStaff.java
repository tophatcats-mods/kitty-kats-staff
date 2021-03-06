package cat.tophat.kittykatsstaff.common.items;

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

public class ItemKittyKatsStaff extends Item {

    private final Lazy<Tag<Item>> allowedItems;

    public ItemKittyKatsStaff(Item.Properties properties, Supplier<Tag<Item>> tagSupplier) {
        super(properties);
        this.allowedItems = Lazy.of(tagSupplier);
    }

    public boolean outOfUses(@Nonnull ItemStack stack) {
        return stack.getDamage() >= (stack.getMaxDamage() - 1);
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    @Override
    public boolean getIsRepairable(ItemStack repairableItem, ItemStack repairMaterial) {
        for (int i = 0; i < allowedItems.get().getAllElements().size(); i++) {
            if (allowedItems.get().contains(repairMaterial.getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull PlayerEntity player, Hand hand) {
    	ItemStack item = player.getHeldItem(hand);
    	
    	if (hand == Hand.MAIN_HAND) {
        	//randomise the type of the cat used when spawning.
            Random random = new Random();
            int min = 1;
            int max = 3;
            int randomSkinValue = random.nextInt((max - min) + 1) + min;

            //The position the player is looking, this is used as the position the ocelot spawns.
            Vec3d vec3d = player.getEyePosition(1.0F);
            Vec3d vec3d1 = player.getLook(1.0F);
            Vec3d vec3d2 = vec3d.add(vec3d1.x * 30, vec3d1.y * 30, vec3d1.z * 30);
            BlockRayTraceResult rayTraceResult = world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2,
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));

            double x = rayTraceResult.getPos().getX();
            double y = rayTraceResult.getPos().getY();
            double z = rayTraceResult.getPos().getZ();
            if (!outOfUses(item)) {
            	
                if (!world.isRemote) {
                    CatEntity cat = new CatEntity(EntityType.CAT, world);
                    cat.setLocationAndAngles(x, y + 1, z, player.rotationYaw, 0.0F);
                    cat.setTamed(true);
                    cat.setTamedBy(player);
                    cat.setCatType(randomSkinValue);
                    world.addEntity(cat);
                }

                world.playSound(null, x, y, z, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS,
                        0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));

                if (!player.isCreative()) {
                    item.damageItem(1, player, (consumer) -> consumer.sendBreakAnimation(hand));
                    player.getCooldownTracker().setCooldown(this, 1200);
                }
            } else {
                world.playSound(null, x, y, z, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS,
                        0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, item);
    }
}