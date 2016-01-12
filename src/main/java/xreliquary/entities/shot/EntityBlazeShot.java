package xreliquary.entities.shot;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityBlazeShot extends EntityShotBase {
    public EntityBlazeShot(World par1World) {
        super(par1World);
    }

    public EntityBlazeShot(World par1World, double par2, double par4, double par6) {
        super(par1World);
    }

    public EntityBlazeShot(World par1World, EntityPlayer par2EntityPlayer) {
        super(par1World, par2EntityPlayer);
    }

    @Override
    void doFlightEffects() {
        if (ticksInAir % 3 == 0 && ticksInAir < 9) {
            worldObj.spawnParticle( EnumParticleTypes.FLAME, posX, posY, posZ, lowGauss(motionX), lowGauss(motionY), lowGauss(motionZ));
        }
    }

    @Override
    void doFiringEffects() {
        worldObj.spawnParticle(EnumParticleTypes.SPELL_MOB_AMBIENT, posX + smallGauss(0.1D), posY + smallGauss(0.1D), posZ + smallGauss(0.1D), 0.5D, 0.5D, 0.5D);
        worldObj.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, gaussian(motionX), gaussian(motionY), gaussian( motionZ ));
    }

    @Override
    protected DamageSource getDamageSource() {
        return super.getDamageSource().setFireDamage();
    }

    @Override
    void onImpact(MovingObjectPosition mop) {
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mop.entityHit != null) {
            if (mop.entityHit == shootingEntity)
                return;
            if (!(mop.entityHit instanceof EntityLivingBase))
                return;
            this.onImpact((EntityLivingBase)mop.entityHit);
        } else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (shootingEntity == null) return;

            this.groundImpact(mop.sideHit);

            BlockPos pos = mop.getBlockPos();

            //TODO: make sure that the fire spawns on correct side of the block (may need to reverse these)

            switch (mop.sideHit) {
                case DOWN:
                    pos.add(0,-1,0);
                    break;
                case UP:
                    pos.add(0,1,0);
                    break;
                case NORTH:
                    pos.add(0,0,-1);
                    break;
                case SOUTH:
                    pos.add(0,0,1);
                    break;
                case WEST:
                    pos.add(-1,0,0);
                    break;
                case EAST:
                    pos.add(1,0,0);
                    break;
            }
            if (shootingEntity.canPlayerEdit(pos, mop.sideHit, new ItemStack(Items.flint_and_steel, 1, 0)))
            {
                if (this.worldObj.isAirBlock(pos))
                {
                    worldObj.setBlockState( pos, Blocks.fire.getDefaultState() );
                }
            }
        }
    }

    @Override
    void onImpact(EntityLivingBase mop) {
        if (mop != shootingEntity || ticksInAir > 3) {
            mop.setFire(40);
            doDamage(mop);
        }
        spawnHitParticles("flame", 8);
        this.setDead();
    }

    @Override
    void spawnHitParticles(String string, int i) {
        for (int particles = 0; particles < i; particles++) {
            worldObj.spawnParticle(string, posX, posY - (string == "portal" ? 1 : 0), posZ, gaussian(motionX), gaussian(motionY), gaussian(motionZ));
        }
    }

    @Override
    protected void groundImpact(EnumFacing sideHit) {
    }

    @Override
    int getRicochetMax() {
        return 0;
    }

    @Override
    int getDamageOfShot(EntityLivingBase mop) {
        // they're not COMPLETELY useless against fireImmune mobs, just mostly
        // useless.
        //this probably isn't gonna work now the bullets do purely fire damage.
        return mop.isImmuneToFire() ? 2 : (10 + d12());
    }

    @Override
    void doBurstEffect(int sideHit) {
        for (int particles = 0; particles < 40; particles++) {
            switch (sideHit) {
                case 0:
                    worldObj.spawnParticle("flame", posX, posY, posZ, smallGauss(0.1D) + motionX / 4, -posGauss(0.2D), smallGauss(0.2D) + motionZ / 4);
                    break;
                case 1:
                    worldObj.spawnParticle("flame", posX, posY, posZ, smallGauss(0.1D) + motionX / 4, posGauss(0.2D), smallGauss(0.2D) + motionZ / 4);
                    break;
                case 2:
                    worldObj.spawnParticle("flame", posX, posY, posZ, smallGauss(0.1D) + motionX / 4, smallGauss(0.1D) + motionY / 4, -posGauss(0.2D));
                    break;
                case 3:
                    worldObj.spawnParticle("flame", posX, posY, posZ, smallGauss(0.1D) + motionX / 4, smallGauss(0.1D) + motionY / 4, posGauss(0.2D));
                    break;
                case 4:
                    worldObj.spawnParticle("flame", posX, posY, posZ, -posGauss(0.2D), smallGauss(0.1D) + motionY / 4, smallGauss(0.1D) + motionZ / 4);
                    break;
                case 5:
                    worldObj.spawnParticle("flame", posX, posY, posZ, posGauss(0.2D), smallGauss(0.1D) + motionY / 4, smallGauss(0.1D) + motionZ / 4);
                    break;
            }
        }


    }
}
