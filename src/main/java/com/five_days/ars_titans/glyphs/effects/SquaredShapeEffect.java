package com.five_days.ars_titans.glyphs.effects;

import com.hollingsworth.arsnouveau.api.spell.*;
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

public class SquaredShapeEffect extends AbstractEffect {

    public static SquaredShapeEffect INSTANCE = new SquaredShapeEffect(prefix("glyph_square"), "A glyph for squared shapes. Use sensitive for a vertical square, dampen to make it hollow, amplify + sensitive for a ramp shape and pierce to make a cube. Each AOE doubles the size of the shape. The shape is always drawn from the bottom left corner.");

    public SquaredShapeEffect(ResourceLocation tag, String description) {
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
        //System.out.println("Direction:");
        //System.out.println(dir.getNormal().toString());
        //System.out.println("Coordinates:");
        //System.out.println(rayTraceResult.getLocation());


        //Square is drawn from the corner. Allows for vertical/horizontal stacking

        int squareSize = 5;
        boolean isVerticalSquare =  spellStats.hasBuff(AugmentSensitive.INSTANCE);
        boolean isHorizontalSquare = !spellStats.hasBuff(AugmentSensitive.INSTANCE) && !spellStats.hasBuff(AugmentAmplify.INSTANCE) && !spellStats.hasBuff(AugmentPierce.INSTANCE);
        boolean isRamp = spellStats.hasBuff(AugmentSensitive.INSTANCE) && spellStats.hasBuff(AugmentAmplify.INSTANCE);
        boolean isCube = spellStats.hasBuff(AugmentPierce.INSTANCE);


        spellStats.hasBuff(AugmentAOE.INSTANCE);
        squareSize *= 1 + spellStats.getAoeMultiplier();
        Vec3 hitLocation = rayTraceResult.getLocation();

        spellContext.setCanceled(true);
        if (spellContext.getRemainingSpell().isEmpty()) return;
        SpellContext newContext = resolver.spellContext.clone().withSpell(spellContext.getRemainingSpell());

        // the main outer loop. Horizontal position can be either x or z
        for(int horizontalPosition = 0, y = (int) Math.round(hitLocation.y); horizontalPosition < squareSize; horizontalPosition++){
            int currentX = (int) hitLocation.x;
            int currentZ = (int) hitLocation.z;
            int currentY = y;

            //player looking in the X axis
            if(dir.getNormal().getX() != 0){

                int xLowerBoundary = (int) hitLocation.x;
                int xUpperBoundary = (int)(hitLocation.x + (squareSize-1) * dir.getNormal().getX());

                int zLowerBoundary = (int)hitLocation.z;
                int zUpperBoundary = (int)(hitLocation.z + dir.getNormal().getX() * (squareSize-1));

                int yLowerBoundary = (int)Math.round(hitLocation.y);
                int yUpperBoundary = (int)(Math.round(hitLocation.y + squareSize-1));


                int zOffset = dir.getNormal().getX() * horizontalPosition;
                currentZ = (int) hitLocation.z + zOffset;


                for (int s = 0; s < squareSize; s++){


                    if(isCube){
                        //a cube
                        for(int yCube = 0;yCube < squareSize; yCube++){
                            if(spellStats.hasBuff(AugmentDampen.INSTANCE)){
                                //make the cube hollow

                                //System.out.println("Boundaries: " + "x: " + xLowerBoundary + "," + xUpperBoundary + " |y " + yLowerBoundary + "," + yUpperBoundary + " |z " + zLowerBoundary + "," + zUpperBoundary);

                                if(
                                        currentX == xLowerBoundary ||
                                        currentX == xUpperBoundary ||
                                        currentZ == zLowerBoundary ||
                                        currentZ == zUpperBoundary ||
                                        currentY + yCube == yLowerBoundary ||
                                        currentY + yCube == yUpperBoundary
                                ){
                                    //i also don't know why i have to do this, but without this the blocks
                                    //are placed with a 1 block offset in this scenario
                                    int finalZ = hitLocation.z < 0? currentZ - 1: currentZ;
                                    int finalX = hitLocation.x < 0? currentX - 1: currentX;
                                    int finalY = currentY + yCube - 1;
                                    BlockPos blockPos = new BlockPos(finalX, finalY, finalZ);

                                    resolver.getNewResolver(newContext.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(finalX, finalY, finalZ), Direction.UP, blockPos, false));
                                    //world.setBlock(blockPos, Blocks.DIRT.defaultBlockState(), 3);
                                }
                            }else{
                                //i also don't know why i have to do this, but without this the blocks
                                //are placed with a 1 block offset in this scenario
                                int finalZ = hitLocation.z < 0? currentZ - 1: currentZ;
                                int finalX = hitLocation.x < 0? currentX - 1: currentX;
                                int finalY = currentY + yCube - 1;
                                BlockPos blockPos = new BlockPos(finalX, finalY, finalZ);

                                resolver.getNewResolver(newContext.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(finalX, finalY, finalZ), Direction.UP, blockPos, false));
                                //world.setBlock(blockPos, Blocks.DIRT.defaultBlockState(), 3);
                            }

                        }

                        currentX = currentX + dir.getNormal().getX();
                    }else{
                        boolean shouldResolve = false;
                        if(spellStats.hasBuff(AugmentDampen.INSTANCE)){
                            //make it hollow
                            boolean isAtZLowerBoundary = currentZ == zLowerBoundary;
                            boolean isAtZUpperBoundary = currentZ == zUpperBoundary;
                            if(isHorizontalSquare || isRamp){
                                boolean isAtXLowerBoundary = currentX == xLowerBoundary;
                                boolean isAtXUpperBoundary = currentX == xUpperBoundary;
                                if(isAtXLowerBoundary || isAtXUpperBoundary || isAtZLowerBoundary || isAtZUpperBoundary){
                                    shouldResolve = true;
                                }
                            }else if (isVerticalSquare){
                                boolean isAtYLowerBoundary = currentY == yLowerBoundary;
                                boolean isAtYUpperBoundary = currentY == yUpperBoundary;
                                if(isAtYLowerBoundary || isAtYUpperBoundary || isAtZLowerBoundary || isAtZUpperBoundary){
                                    shouldResolve = true;
                                }
                            }
                        }else{
                            shouldResolve = true;
                        }

                        if(shouldResolve){
                            int finalZ = hitLocation.z < 0? currentZ - 1: currentZ;
                            int finalX = hitLocation.x < 0? currentX - 1: currentX;
                            int finalY = currentY - 1;
                            BlockPos blockPos = new BlockPos(finalX, finalY, finalZ);
                            //world.setBlock(blockPos, Blocks.DIRT.defaultBlockState(), 3);
                            resolver.getNewResolver(newContext.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(finalX, finalY, finalZ), Direction.UP, blockPos, false));
                        }


                        if(isRamp){
                            //a ramp
                            currentY++;
                            currentX = currentX + dir.getNormal().getX();
                        } else if (isVerticalSquare){
                            //a vertical square
                            currentY++;
                        }else if (isHorizontalSquare){
                            //a horizontal square
                            currentX = currentX + dir.getNormal().getX();
                        }
                    }


                }


            }else if (dir.getNormal().getZ() != 0){ //player looking in the Z axis

                int xLowerBoundary = (int) hitLocation.x;
                int xUpperBoundary = (int)(hitLocation.x + (squareSize-1) * dir.getNormal().getZ() * -1);

                int zLowerBoundary = (int)hitLocation.z;
                int zUpperBoundary = (int)(hitLocation.z + dir.getNormal().getZ() * (squareSize-1));

                int yLowerBoundary = (int)Math.round(hitLocation.y);
                int yUpperBoundary = (int)(Math.round(hitLocation.y + squareSize-1));

                //System.out.println("Boundaries: " + "x: " + xLowerBoundary + "," + xUpperBoundary + " |y " + yLowerBoundary + "," + yUpperBoundary + " |z " + zLowerBoundary + "," + zUpperBoundary);

                int xOffset = dir.getNormal().getZ() * -1 * horizontalPosition;
                currentX = (int) hitLocation.x + xOffset;


                for (int z = 0; z < squareSize; z++){


                    if(isCube){
                        //a cube
                        for(int yCube = 0;yCube < squareSize; yCube++){
                            if(spellStats.hasBuff(AugmentDampen.INSTANCE)){
                                //make the cube hollow

                                //System.out.println("Boundaries: " + "x: " + xLowerBoundary + "," + xUpperBoundary + " |y " + yLowerBoundary + "," + yUpperBoundary + " |z " + zLowerBoundary + "," + zUpperBoundary);

                                if(
                                        currentX == xLowerBoundary ||
                                        currentX == xUpperBoundary ||
                                        currentZ == zLowerBoundary ||
                                        currentZ == zUpperBoundary ||
                                        currentY + yCube == yLowerBoundary ||
                                        currentY + yCube == yUpperBoundary
                                ){
                                    int finalZ = hitLocation.z < 0? currentZ - 1: currentZ;
                                    int finalX = hitLocation.x < 0? currentX - 1: currentX;
                                    int finalY = currentY + yCube - 1;

                                    BlockPos blockPos = new BlockPos(finalX, finalY, finalZ);

                                    resolver.getNewResolver(newContext.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(finalX, finalY, finalZ), Direction.UP, blockPos, false));
                                    //world.setBlock(blockPos, Blocks.DIRT.defaultBlockState(), 3);
                                }
                            }else{
                                //i also don't know why i have to do this, but without this the blocks
                                //are placed with a 1 block offset in this scenario
                                int finalZ = hitLocation.z < 0? currentZ - 1: currentZ;
                                int finalX = hitLocation.x < 0? currentX - 1: currentX;
                                int finalY = currentY + yCube - 1;

                                BlockPos blockPos = new BlockPos(finalX, finalY, finalZ);

                                resolver.getNewResolver(newContext.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(finalX, finalY, finalZ), Direction.UP, blockPos, false));

                                //world.setBlock(blockPos, Blocks.DIRT.defaultBlockState(), 3);
                            }
                        }

                        currentZ = currentZ + dir.getNormal().getZ();

                    }else{
                        boolean shouldResolve = false;
                        if(spellStats.hasBuff(AugmentDampen.INSTANCE)){
                            //make it hollow
                            boolean isAtXLowerBoundary = currentX == xLowerBoundary;
                            boolean isAtXUpperBoundary = currentX == xUpperBoundary;
                            if(isHorizontalSquare || isRamp){

                                boolean isAtZLowerBoundary = currentZ == zLowerBoundary;
                                boolean isAtZUpperBoundary = currentZ == zUpperBoundary;

                                if(isAtXLowerBoundary || isAtXUpperBoundary || isAtZLowerBoundary || isAtZUpperBoundary){
                                    shouldResolve = true;
                                }
                            }else if (isVerticalSquare){

                                boolean isAtYLowerBoundary = currentY == yLowerBoundary;
                                boolean isAtYUpperBoundary = currentY == yUpperBoundary;
                                if(isAtYLowerBoundary || isAtYUpperBoundary || isAtXLowerBoundary || isAtXUpperBoundary){
                                    shouldResolve = true;
                                }
                            }
                        }else{
                            shouldResolve = true;
                        }

                        if(shouldResolve){
                            //i also don't know why i have to do this, but without this the blocks
                            //are placed with a 1 block offset in this scenario
                            int finalZ = hitLocation.z < 0? currentZ - 1: currentZ;
                            int finalX = hitLocation.x < 0? currentX - 1: currentX;
                            int finalY = currentY - 1;
                            BlockPos blockPos = new BlockPos(finalX, finalY, finalZ);

                            resolver.getNewResolver(newContext.clone()).onResolveEffect(world, new BlockHitResult(new Vec3(finalX, finalY, finalZ), Direction.UP, blockPos, false));
                            //world.setBlock(blockPos, Blocks.DIRT.defaultBlockState(), 3);
                        }
                        if(isRamp){
                            //diagonal square
                            currentY++;
                            currentZ = currentZ + dir.getNormal().getZ();
                        } else if(isVerticalSquare){
                            //flat vertical square
                            currentY++;
                        }else if(isHorizontalSquare){
                            //flat horizontal square
                            currentZ = currentZ + dir.getNormal().getZ();
                        }
                    }

                }


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
