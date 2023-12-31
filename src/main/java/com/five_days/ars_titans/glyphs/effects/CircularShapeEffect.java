package com.five_days.ars_titans.glyphs.effects;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.Set;

import static com.five_days.ars_titans.ArsTitans.prefix;

public class CircularShapeEffect extends AbstractEffect {

    public static CircularShapeEffect INSTANCE = new CircularShapeEffect(prefix("glyph_circle"), "A glyph for circular shapes. Use sensitive for a vertical circle, dampen to make it hollow and pierce to make a sphere. Each AOE doubles the size of the shape. The shape is always drawn from the bottom middle.");

    public CircularShapeEffect(ResourceLocation tag, String description) {
        super(tag, description);
    }

    @Override
    public int getDefaultManaCost() {
        return 5;
    }

    /*
     * This glyph has a bug which makes the vertical wall version of this spell behave differently
     * depending on which side of the block the spells resolve to. This bug is due to the fact that the
     * raytrace result gives inconsistent results for opposite-facing sides of a block.
     *
     * */


    /*
     * Disclaimer: the following code may hurt your eyes, read at your own risk.
     *
     * */
    @Override
    public void onResolve(HitResult rayTraceResult, Level world, @Nonnull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        super.onResolve(rayTraceResult, world, shooter, spellStats, spellContext, resolver);

        Direction dir = shooter.getDirection();
        System.out.println("Direction:");
        System.out.println(dir.getNormal().toString());
        System.out.println("Coordinates:");
        System.out.println(rayTraceResult.getLocation());


        //Square is drawn from the corner. Allows for vertical/horizontal stacking

        int radius = 5;
        boolean isVerticalCircle =  spellStats.hasBuff(AugmentSensitive.INSTANCE);
        boolean isHorizontalCircle = !spellStats.hasBuff(AugmentSensitive.INSTANCE) && !spellStats.hasBuff(AugmentAmplify.INSTANCE) && !spellStats.hasBuff(AugmentPierce.INSTANCE);
        boolean isSphere = spellStats.hasBuff(AugmentPierce.INSTANCE);
        boolean isHollow = spellStats.hasBuff(AugmentDampen.INSTANCE);

        spellStats.hasBuff(AugmentAOE.INSTANCE);
        radius *= 1 + spellStats.getAoeMultiplier();
        Vec3 hitLocation = rayTraceResult.getLocation();

        spellContext.setCanceled(true);
        //uncomment this once i switch to using the resolver
        //if (spellContext.getRemainingSpell().isEmpty()) return;
        SpellContext newContext = resolver.spellContext.clone().withSpell(spellContext.getRemainingSpell());

        //to make a 2d circle, filter the positions returned by withinManhattan to only the ones that have the right
        //X or Z, depending on where the player is looking. This means even for a 2d circle the code will still
        //sweep a sphere but at least that makes the code simpler

        int x0 = (int) hitLocation.x;
        int z0 = (int) hitLocation.z;
        int y0 = (int) hitLocation.y;

        if(dir.getNormal().getX() != 0){ //looking in the x axis
            int xCenter = x0;
            int zCenter = z0;
            int yCenter = y0;

            Vec3 center = new Vec3(xCenter, yCenter, zCenter);

            if(isHorizontalCircle){
                center = new Vec3(center.x + (radius * dir.getNormal().getX()), center.y, center.z);
                BlockPos centerBlockPos = new BlockPos(center);
                for (BlockPos pos : BlockPos.withinManhattan(centerBlockPos, radius, 0, radius)){
                    placeResolver(resolver, newContext, pos, centerBlockPos, radius, isHollow, world);

                }
            }else if(isVerticalCircle){
                center = new Vec3(center.x, center.y + radius, center.z);
                BlockPos centerBlockPos = new BlockPos(center);

                for (BlockPos pos : BlockPos.withinManhattan(centerBlockPos, 0, radius, radius)){
                    placeResolver(resolver, newContext, pos, centerBlockPos, radius, isHollow, world);

                }
            } else if (isSphere){
                center = new Vec3(center.x, center.y + radius, center.z);
                BlockPos centerBlockPos = new BlockPos(center);
                for (BlockPos pos : BlockPos.withinManhattan(centerBlockPos, radius, radius, radius)){
                    placeResolver(resolver, newContext, pos, centerBlockPos, radius, isHollow, world);

                }
            }



        }else if (dir.getNormal().getZ() != 0){
            int xCenter = x0;
            int zCenter = z0;
            int yCenter = y0;

            Vec3 center = new Vec3(xCenter, yCenter, zCenter);

            if(isHorizontalCircle){
                center = new Vec3(center.x, center.y, center.z + (radius * dir.getNormal().getZ()));
                BlockPos centerBlockPos = new BlockPos(center);
                for (BlockPos pos : BlockPos.withinManhattan(centerBlockPos, radius, 0, radius)){

                    placeResolver(resolver, newContext, pos, centerBlockPos, radius, isHollow, world);
                }
            }else if(isVerticalCircle){
                center = new Vec3(center.x, center.y + radius, center.z);
                BlockPos centerBlockPos = new BlockPos(center);

                for (BlockPos pos : BlockPos.withinManhattan(centerBlockPos, radius, radius, 0)){

                    placeResolver(resolver, newContext, pos, centerBlockPos, radius, isHollow, world);
                }
            } else if (isSphere){
                center = new Vec3(center.x, center.y + radius, center.z);
                BlockPos centerBlockPos = new BlockPos(center);
                for (BlockPos pos : BlockPos.withinManhattan(centerBlockPos, radius, radius, radius)){

                    placeResolver(resolver, newContext, pos, centerBlockPos, radius, isHollow, world);
                }
            }
        }



    }


    public void placeResolver(SpellResolver resolver, SpellContext context, BlockPos pos, BlockPos centerBlockPos, int radius, boolean isHollow, Level world){
        if(isHollow){
            if((BlockUtil.distanceFrom(pos, centerBlockPos) <= radius + 0.5) && (BlockUtil.distanceFrom(pos, centerBlockPos) >= radius - 0.5)){
                //world.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                resolver.getNewResolver(context.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
            }
        }else{
            if(BlockUtil.distanceFrom(pos, centerBlockPos) <= radius + 0.5){
                //world.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                resolver.getNewResolver(context.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
            }
        }
    }

    @Nonnull
    @Override
    public Set<AbstractAugment> getCompatibleAugments() {
        //Dampen for hollow, AOE to increase size, sensitive to make it vertical, amplify to make it a cube
        return augmentSetOf(AugmentDampen.INSTANCE, AugmentAOE.INSTANCE, AugmentSensitive.INSTANCE, AugmentAmplify.INSTANCE, AugmentPierce.INSTANCE);
    }

    @Override
    protected @NotNull Set<SpellSchool> getSchools() {
        return setOf(SpellSchools.MANIPULATION);
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.THREE;
    }
}
