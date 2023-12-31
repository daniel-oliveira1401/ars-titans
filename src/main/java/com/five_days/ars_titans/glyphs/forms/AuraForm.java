package com.five_days.ars_titans.glyphs.forms;

import com.five_days.ars_titans.ArsTitans;
import com.five_days.ars_titans.registry.ModRegistry;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;

import static com.five_days.ars_titans.ArsTitans.prefix;

public class AuraForm extends AbstractCastMethod  {

    public static AuraForm INSTANCE = new AuraForm(prefix("glyph_aura"), "An aura that has effect 5 blocks around the player. Use AOE to increase the radius of the aura, extend time to increase the duration. Use amplify to make the aura cover every block around the player. Use dampen to make the aura a half sphere from where the player is standing upwards. Use sensitive to make the aura a half sphere from where the player is standing downwards. Use sensitive to affect entities instead of blocks.");

    private final int baseDuration = 600; //600 ticks = 30s

    public AuraForm(ResourceLocation tag, String description) {
        super(tag, description);
    }

    public void startEffect(LivingEntity playerEntity, Level world, SpellStats spellStats, SpellContext context, SpellResolver resolver, Entity mob){
        boolean isUpperHalfCircle = spellStats.hasBuff(AugmentDampen.INSTANCE);
        boolean isLowerHalfCircle = spellStats.hasBuff(AugmentPierce.INSTANCE);
        boolean isSolidSphere = spellStats.hasBuff(AugmentAmplify.INSTANCE);
        boolean targetsEntities = spellStats.hasBuff(AugmentSensitive.INSTANCE);

        int finalDuration = baseDuration * (int)(1 + spellStats.getDurationMultiplier());

        playerEntity.addEffect(new MobEffectInstance(ModRegistry.AURA_EFFECT.get(), finalDuration));

        //target entities with lowered tick rate
        if(targetsEntities){
            ArsTitans.setInterval(()-> {
                int radius = 5 + (int)spellStats.getAoeMultiplier();
                BlockPos entityBlockPos = mob != null? mob.blockPosition() : playerEntity.blockPosition();
                for(LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AABB(entityBlockPos).inflate(radius, radius, radius))){

                    if(mob != null){ //aura was cast on an mob, not the player itself
                        if(entity.equals(mob)){
                            continue; //aura does not affect the entity that it is attached to
                        }
                    }else{
                        if(entity.equals(playerEntity)){
                            continue;
                        }
                    }

                    BlockPos pos = entity.blockPosition();

                    boolean isWithinSphere = BlockUtil.distanceFrom(entityBlockPos, pos) <= radius + 0.5;
                    boolean isAtTheBorder = !isSolidSphere? BlockUtil.distanceFrom(entityBlockPos, pos) >= radius - 1 + 0.5 : true;
                    boolean isBelowThePlayer = isLowerHalfCircle? pos.getY() <= entityBlockPos.getY()-2 : true;
                    boolean isOverTheGround = isUpperHalfCircle? pos.getY() > entityBlockPos.getY()-1 : true;


                    if(isWithinSphere && isAtTheBorder && isBelowThePlayer && isOverTheGround){

                        EntityHitResult entityHitResult = new EntityHitResult(entity);
                        resolver.onResolveEffect(world, entityHitResult);
                    }


                }


            }, 10, finalDuration);
        }else{ // target blocks with high tick rate
            ArsTitans.setInterval(()-> {
                int radius = 5 + (int)spellStats.getAoeMultiplier();
                BlockPos entityBlockPos = mob != null? mob.blockPosition() : playerEntity.blockPosition();
                for(BlockPos pos : BlockPos.withinManhattan(entityBlockPos, radius, radius, radius)){

                    boolean isWithinSphere = BlockUtil.distanceFrom(entityBlockPos, pos) <= radius + 0.5;
                    boolean isAtTheBorder = !isSolidSphere? BlockUtil.distanceFrom(entityBlockPos, pos) >= radius - 1 + 0.5 : true;
                    boolean isBelowThePlayer = isLowerHalfCircle? pos.getY() <= entityBlockPos.getY()-2 : true;
                    boolean isOverTheGround = isUpperHalfCircle? pos.getY() > entityBlockPos.getY()-1 : true;



                    if(isWithinSphere && isAtTheBorder && isBelowThePlayer && isOverTheGround){
                        BlockHitResult blockHitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false);

                        resolver.onResolveEffect(world, blockHitResult);
                    }

                }


            }, 1, finalDuration);
        }



    }

    @Override
    public CastResolveType onCast(@Nullable ItemStack stack, LivingEntity playerEntity, Level world, SpellStats spellStats, SpellContext context, SpellResolver resolver) {

        startEffect(playerEntity, world, spellStats, context, resolver, null);

        return CastResolveType.SUCCESS;
    }

    @Override
    public CastResolveType onCastOnBlock(UseOnContext context, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        startEffect(context.getPlayer(), context.getLevel(), spellStats, spellContext, resolver, null);
        return CastResolveType.SUCCESS;
    }

    @Override
    public CastResolveType onCastOnBlock(BlockHitResult blockRayTraceResult, LivingEntity caster, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {

        startEffect(caster, caster.getCommandSenderWorld(), spellStats, spellContext, resolver, null);
        return CastResolveType.SUCCESS;
    }

    @Override
    public CastResolveType onCastOnEntity(@Nullable ItemStack stack, LivingEntity caster, Entity target, InteractionHand hand, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {

        startEffect(caster, caster.getCommandSenderWorld(), spellStats, spellContext, resolver, target);
        return CastResolveType.SUCCESS;
    }

    @Override
    public int getDefaultManaCost() {
        return 1000;
    }

    @Override
    protected @NotNull Set<AbstractAugment> getCompatibleAugments() {
        //AOE increases radius
        //Amplify makes the sphere solid
        //Dampen makes it a dome (upper half hemisphere)
        //Pierce makes it an inverted dome (lower half hemisphere)
        //Sensitive makes it target entities instead of blocks
        return augmentSetOf(AugmentAOE.INSTANCE, AugmentAmplify.INSTANCE, AugmentDampen.INSTANCE, AugmentSensitive.INSTANCE, AugmentExtendTime.INSTANCE, AugmentPierce.INSTANCE);
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
