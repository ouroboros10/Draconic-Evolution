package com.brandon3055.draconicevolution.client.handler;


import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import com.brandon3055.brandonscore.lib.PairKV;
import com.brandon3055.brandonscore.utils.DataUtils.XZPair;
import com.brandon3055.brandonscore.utils.ModelUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.DEFeatures;
import com.brandon3055.draconicevolution.api.ICrystalBinder;
import com.brandon3055.draconicevolution.api.itemconfig.ToolConfigHelper;
import com.brandon3055.draconicevolution.handlers.BinderHandler;
import com.brandon3055.draconicevolution.helpers.ResourceHelperDE;
import com.brandon3055.draconicevolution.items.tools.CreativeExchanger;
import com.brandon3055.draconicevolution.items.tools.MiningToolBase;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Created by Brandon on 28/10/2014.
 */
public class ClientEventHandler {
    public static Map<EntityPlayer, XZPair<Float, Integer>> playerShieldStatus = new HashMap<EntityPlayer, XZPair<Float, Integer>>();

    public static volatile int elapsedTicks;
    private static float previousFOB = 0f;
    public static float previousSensitivity = 0;
    public static boolean bowZoom = false;
    public static boolean lastTickBowZoom = false;
    public static int tickSet = 0;
    public static float energyCrystalAlphaValue = 0f;
    public static float energyCrystalAlphaTarget = 0f;
    public static boolean playerHoldingWrench = false;
    public static Minecraft mc;
    private static Random rand = new Random();
    public static IBakedModel shieldModel = null;

    @SubscribeEvent
    public void renderGameOverlay(RenderGameOverlayEvent.Post event) {
        HudHandler.drawHUD(event);
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.type != TickEvent.Type.CLIENT || event.side != Side.CLIENT) {
            return;
        }

        elapsedTicks++;
        HudHandler.clientTick();

        for (Iterator<Map.Entry<EntityPlayer, XZPair<Float, Integer>>> i = playerShieldStatus.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<EntityPlayer, XZPair<Float, Integer>> entry = i.next();
            if (elapsedTicks - entry.getValue().getValue() > 5) i.remove();
        }

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            playerHoldingWrench = (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ICrystalBinder) || (player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() instanceof ICrystalBinder);
        }

//        if (mc == null) { TODO Do i really want to reimplement this?
//            mc = Minecraft.getMinecraft();
//        }
//        else if (mc.theWorld != null) {
//            if (bowZoom && !lastTickBowZoom) {
//                previousSensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
//                Minecraft.getMinecraft().gameSettings.mouseSensitivity = previousSensitivity / 3;
//            }
//            else if (!bowZoom && lastTickBowZoom) {
//                Minecraft.getMinecraft().gameSettings.mouseSensitivity = previousSensitivity;
//            }
//
//            lastTickBowZoom = bowZoom;
//            if (elapsedTicks - tickSet > 10) bowZoom = false;
//
//            if (energyCrystalAlphaValue < energyCrystalAlphaTarget) energyCrystalAlphaValue += 0.01f;
//            if (energyCrystalAlphaValue > energyCrystalAlphaTarget) energyCrystalAlphaValue -= 0.01f;
//
//            if (Math.abs(energyCrystalAlphaTarget - energyCrystalAlphaValue) <= 0.02f) energyCrystalAlphaTarget = rand.nextFloat();
//        }
    }

    @SubscribeEvent
    public void fovUpdate(FOVUpdateEvent event) {

        //region Bow FOV Update
//		if (event.entity.getHeldItem()!= null && (event.entity.getHeldItem().getItem() instanceof WyvernBow || event.entity.getHeldItem().getItem() instanceof DraconicBow) && Minecraft.getMinecraft().gameSettings.keyBindUseItem.getIsKeyPressed()){
//
//			BowHandler.BowProperties properties = new BowHandler.BowProperties(event.entity.getHeldItem(), event.entity);
//
//			event.newfov = ((6 - properties.zoomModifier) / 6) * event.fov;
//
////			if (ItemNBTHelper.getString(event.entity.getItemInUse(), "mode", "").equals("sharpshooter")){
////				if (event.entity.getItemInUse().getItem() instanceof WyvernBow) zMax = 1.35f;
////				else if (event.entity.getItemInUse().getItem() instanceof DraconicBow) zMax = 2.5f;
////				bowZoom = true;
////				tickSet = elapsedTicks;
////			}
//
//		}
//		//endregion
//
//		//region Armor move speed FOV effect cancellation
//		CustomArmorHandler.ArmorSummery summery = new CustomArmorHandler.ArmorSummery().getSummery(event.entity);
//
//		if (summery != null && summery.speedModifier > 0){
//			IAttributeInstance iattributeinstance = event.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
//			float f = (float)((iattributeinstance.getAttributeValue() / (double)event.entity.capabilities.getWalkSpeed() + 1.0D) / 2.0D);
//			event.newfov /= f;
//		}

        //endregion
    }

    @SubscribeEvent
    public void renderPlayerEvent(RenderPlayerEvent.Post event) {
        if (playerShieldStatus.containsKey(event.getEntityPlayer())) {
            if (shieldModel == null) {
                try {
                    shieldModel = OBJLoader.INSTANCE.loadModel(ResourceHelperDE.getResource("models/armor/shield_sphere.obj")).bake(TransformUtils.DEFAULT_BLOCK, DefaultVertexFormats.BLOCK, TextureUtils.bakedTextureGetter);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            GlStateManager.pushMatrix();
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();

            float p = playerShieldStatus.get(event.getEntityPlayer()).getKey();

            EntityPlayer viewingPlayer = Minecraft.getMinecraft().thePlayer;

            int i = 5 - (elapsedTicks - playerShieldStatus.get(event.getEntityPlayer()).getValue());

            //GlStateManager.color(1F - p, 0F, p, i / 5F);

            if (viewingPlayer != event.getEntityPlayer()) {
                double translationXLT = event.getEntityPlayer().prevPosX - viewingPlayer.prevPosX;
                double translationYLT = event.getEntityPlayer().prevPosY - viewingPlayer.prevPosY;
                double translationZLT = event.getEntityPlayer().prevPosZ - viewingPlayer.prevPosZ;

                double translationX = translationXLT + (((event.getEntityPlayer().posX - viewingPlayer.posX) - translationXLT) * event.getPartialRenderTick());
                double translationY = translationYLT + (((event.getEntityPlayer().posY - viewingPlayer.posY) - translationYLT) * event.getPartialRenderTick());
                double translationZ = translationZLT + (((event.getEntityPlayer().posZ - viewingPlayer.posZ) - translationZLT) * event.getPartialRenderTick());

                GlStateManager.translate(translationX, translationY + 1.1, translationZ);
            }
            else {
                //GL11.glTranslated(0, -0.5, 0);
                GlStateManager.translate(0, 1.15, 0);
            }

            GlStateManager.scale(1, 1.5, 1);

            GlStateManager.bindTexture(Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());

            ModelUtils.renderQuadsARGB(shieldModel.getQuads(null, null, 0), new ColourRGBA(1D - p, 0D, p, i / 5D).argb());

            GlStateManager.enableCull();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }

//    @SubscribeEvent
//    public void renderArmorEvent(RenderPlayerEvent.SetArmorModel event) {
////        if (event.isCanceled()) {
////            return;
////        }
////        if (event.getStack() != null && (event.getStack().getItem() instanceof DraconicArmor || event.getStack().getItem() instanceof WyvernArmor)) {
////            ItemArmor itemarmor = (ItemArmor) event.getStack().getItem();
////
////
//////            ModelBiped modelbiped = itemarmor.getArmorModel(event.getEntityPlayer(), event.getStack(), event.getSlot(), event.getRenderer().getMainModel());
//////            event.getRenderer().setRenderPassModel(modelbiped);
//////            modelbiped.onGround = event.renderer.modelBipedMain.onGround;
//////            modelbiped.isRiding = event.renderer.modelBipedMain.isRiding;
//////            modelbiped.isChild = event.renderer.modelBipedMain.isChild;
////            event.setResult(1);
////        }
//    }

    @SubscribeEvent
    public void guiOpenEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && rand.nextInt(150) == 0) {
            try {
                String s = rand.nextBoolean() ? "Icosahedrons proudly brought to you by CCL!!!" : Utils.addCommas(Long.MAX_VALUE) + " RF!!!!";

                ReflectionHelper.setPrivateValue(GuiMainMenu.class, (GuiMainMenu) event.getGui(), s, "splashText", "field_110353_x");
            }
            catch (Exception e) {
            }
        }
    }

    @SubscribeEvent
    public void renderWorldEvent(RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            return;
        }

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        World world = player.getEntityWorld();
        ItemStack stack = player.getHeldItemMainhand();
        ItemStack offStack = player.getHeldItemOffhand();
        Minecraft mc = Minecraft.getMinecraft();
        float partialTicks = event.getPartialTicks();

        if (stack != null && stack.getItem() instanceof ICrystalBinder) {
            BinderHandler.renderWorldOverlay(player, world, stack, mc, partialTicks);
            return;
        }
        else if (offStack != null && offStack.getItem() instanceof ICrystalBinder) {
            BinderHandler.renderWorldOverlay(player, world, offStack, mc, partialTicks);
            return;
        }


        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        if (stack != null && stack.getItem() == DEFeatures.creativeExchanger) {

            List<BlockPos> blocks = CreativeExchanger.getBlocksToReplace(stack, mc.objectMouseOver.getBlockPos(), world, mc.objectMouseOver.sideHit);

            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer buffer = tessellator.getBuffer();

            double offsetX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
            double offsetY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
            double offsetZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();

            for (BlockPos block : blocks) {
                if (world.isAirBlock(block)) {
                    continue;
                }

                double renderX = block.getX() - offsetX;
                double renderY = block.getY() - offsetY;
                double renderZ = block.getZ() - offsetZ;

                AxisAlignedBB boundingBox = new AxisAlignedBB(renderX, renderY, renderZ, renderX + 1, renderY + 1, renderZ + 1).expand(0.001, 0.001, 0.001);
                float colour = 1F;
                if (!world.getBlockState(block.offset(mc.objectMouseOver.sideHit)).getBlock().isReplaceable(world, block.offset(mc.objectMouseOver.sideHit))) {
                    GlStateManager.disableDepth();
                    colour = 0.2F;
                }

                buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                tessellator.draw();
                buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                tessellator.draw();
                buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(colour, colour, colour, colour).endVertex();
                tessellator.draw();

                if (!world.getBlockState(block.offset(mc.objectMouseOver.sideHit)).getBlock().isReplaceable(world, block.offset(mc.objectMouseOver.sideHit))) {
                    GlStateManager.enableDepth();
                }
            }

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }

        if (stack == null || !(stack.getItem() instanceof MiningToolBase) || !ToolConfigHelper.getBooleanField("showDigAOE", stack)) {
            return;
        }

        BlockPos pos = mc.objectMouseOver.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        MiningToolBase tool = (MiningToolBase) stack.getItem();

        if (!tool.isToolEffective(stack, state)) {
            return;
        }

        renderMiningAOE(world, stack, pos, player, partialTicks);
    }

    private void renderMiningAOE(World world, ItemStack stack, BlockPos pos, EntityPlayerSP player, float partialTicks) {
        MiningToolBase tool = (MiningToolBase) stack.getItem();
        PairKV<BlockPos, BlockPos> aoe = tool.getMiningArea(pos, player, tool.getDigAOE(stack), tool.getDigDepth(stack));
        List<BlockPos> blocks = Lists.newArrayList(BlockPos.getAllInBox(aoe.getKey(), aoe.getValue()));
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        double offsetX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
        double offsetY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
        double offsetZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();


        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (BlockPos block : blocks) {
            IBlockState state = world.getBlockState(block);

            if (!tool.isToolEffective(stack, state)) {
                continue;
            }

            double renderX = block.getX() - offsetX;
            double renderY = block.getY() - offsetY;
            double renderZ = block.getZ() - offsetZ;

            AxisAlignedBB box = new AxisAlignedBB(renderX, renderY, renderZ, renderX + 1, renderY + 1, renderZ + 1).contract(0.49D);

            //          buffer.pos(renderX, renderY, renderZ).color(1F, 1F, 1F, 1F).endVertex();
//            buffer.pos(renderX + 1, renderY + 1, renderZ + 1).color(1F, 1F, 1F, 1F).endVertex();

            double rDist = Utils.getDistanceSq(pos.getX(), pos.getY(), pos.getZ(), block.getX(), block.getY(), block.getZ());


            float colour = 1F - (float) rDist / 100F;
            if (colour < 0.1F) {
                colour = 0.1F;
            }
            float alpha = colour;
            if (alpha < 0.15) {
                alpha = 0.15F;
            }

            float r = 0F;
            float g = 1F;
            float b = 1F;


            buffer.pos(box.minX, box.minY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.maxX, box.maxY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

            buffer.pos(box.maxX, box.minY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.minX, box.maxY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

            buffer.pos(box.minX, box.minY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.maxX, box.maxY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

            buffer.pos(box.maxX, box.minY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.minX, box.maxY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

//
//            buffer.begin(3, DefaultVertexFormats.POSITION);
//            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
//            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
//            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//            tessellator.draw();
//            buffer.begin(3, DefaultVertexFormats.POSITION);
//            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//            tessellator.draw();
//            buffer.begin(1, DefaultVertexFormats.POSITION);
//            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
//            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
//            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//            tessellator.draw();

        }

        tessellator.draw();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


//    public static CrystalTexture crystalTexture = new CrystalTexture(256, "draconicevolution:crystal_texture");
//
//    @SubscribeEvent
//    public void textureStitch(TextureStitchEvent.Post event) {
//        event.getMap().mapUploadedSprites.put("draconicevolution:crystal_texture", crystalTexture.texture);
//    }


//          EnumFacing
//
//        if ()
//
//        switch () {
//
//        case DOWN:
//        buffer.begin(3, DefaultVertexFormats.POSITION);
//        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
//        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
//        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
//        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//        tessellator.draw();
//        break;
//        case UP:
//        break;
//        case NORTH:
//        break;
//        case SOUTH:
//        break;
//        case WEST:
//        break;
//        case EAST:
//        break;
//        }
//
//
//
//                buffer.begin(3, DefaultVertexFormats.POSITION);
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                tessellator.draw();
//
//                buffer.begin(3, DefaultVertexFormats.POSITION);
//                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
//                tessellator.draw();
//
//                buffer.begin(3, DefaultVertexFormats.POSITION);
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//                tessellator.draw();
//
//                buffer.begin(3, DefaultVertexFormats.POSITION);
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//                tessellator.draw();
//
//                buffer.begin(3, DefaultVertexFormats.POSITION);
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
//                tessellator.draw();
//
//
//                buffer.begin(1, DefaultVertexFormats.POSITION);
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
//                buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
//                tessellator.draw();
}