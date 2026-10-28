package dev.poptartking.poptartcore;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import dev.poptartking.poptartcore.decoration.WoodenBucketBlock;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreParticles;

@EventBusSubscriber(modid="poptartcore",value=Dist.CLIENT)
public final class TapperVisualSmoke {
 private static int ticks;
 private static boolean done;
 private static net.minecraft.client.particle.Particle drip;
 @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
  var mc=Minecraft.getInstance();
  if(done || mc.player==null || mc.level==null || mc.screen!=null) return;
  try {
   ticks++;
   mc.options.pauseOnLostFocus=false;
   mc.options.hideGui=true;
   if(ticks==1) {
    mc.getSingleplayerServer().execute(() -> {
     var level=mc.getSingleplayerServer().overworld();
     var pos=new BlockPos(200,120,200);
     for(int x=197;x<205;x++) for(int y=118;y<126;y++) for(int z=197;z<205;z++)
      level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
     level.setBlock(pos.north(),Blocks.OAK_LOG.defaultBlockState(),3);
     level.setBlock(pos.north().above(),Blocks.OAK_LOG.defaultBlockState(),3);
     var state=PoptartWorkstationBlocks.WOODEN_BUCKET.get().defaultBlockState().setValue(WoodenBucketBlock.FACING,Direction.NORTH).setValue(WoodenBucketBlock.OPEN,true);
     level.setBlock(pos,state,3);
     level.setBlock(pos.above(),state.setValue(WoodenBucketBlock.HALF,DoubleBlockHalf.UPPER),3);
     level.setDayTime(6000);
     var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
     player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
     player.teleportTo(level,201.3,119.8,202.4,157F,12F);
    });
   }
   if(ticks>=50) { mc.player.setPos(201.3,119.8,202.4); mc.player.setYRot(157F);mc.player.setXRot(12F); }
   if(ticks==80) drip=mc.particleEngine.createParticle(PoptartCoreParticles.TAPPER_SYRUP_DRIP.get(),200.5,120.9995,200.3125,0,0,0);
   if(ticks==80+dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS
     && (drip==null || !drip.isAlive())) throw new AssertionError("Drip expired before slow forming phase completed");
   if(ticks==82 || ticks==80+dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS/3
     || ticks==80+dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS*2/3
     || ticks==80+dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS-4
     || ticks==80+dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS
     || ticks==84+dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS) {
    net.minecraft.client.Screenshot.grab(new java.io.File("."),"tapper-frame-"+ticks+".png",mc.getMainRenderTarget(),text -> {});
   }
   if(ticks==110+dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS) {done=true; com.mojang.logging.LogUtils.getLogger().info("TAPPER_VISUAL_PASS"); mc.stop();}
  } catch(Throwable e) {done=true;com.mojang.logging.LogUtils.getLogger().error("TAPPER_VISUAL_FAIL",e);mc.stop();}
 }
}
