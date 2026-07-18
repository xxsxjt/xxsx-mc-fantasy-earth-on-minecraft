package com.xxsx.earthonlinemagic.client;

import com.xxsx.earthonlinemagic.client.model.AetherFoxModel;
import com.xxsx.earthonlinemagic.client.model.ArcaneSettlerModel;
import com.xxsx.earthonlinemagic.client.model.CrystalSpiderModel;
import com.xxsx.earthonlinemagic.client.model.ManaWispModel;
import com.xxsx.earthonlinemagic.client.model.RuneWolfModel;
import com.xxsx.earthonlinemagic.client.model.RunicWatcherModel;
import com.xxsx.earthonlinemagic.client.renderer.ArcaneSettlerRenderer;
import com.xxsx.earthonlinemagic.client.renderer.CrystalSpiderRenderer;
import com.xxsx.earthonlinemagic.client.renderer.FamiliarRenderer;
import com.xxsx.earthonlinemagic.client.renderer.RunicWatcherRenderer;
import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.ArcaneActionPayload;
import com.xxsx.earthonlinemagic.ArcaneFocus;
import com.xxsx.earthonlinemagic.ArcaneStatusPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

public final class EarthOnlineMagicClient {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(EarthOnlineMagic.id("controls"));
    private static final KeyMapping OPEN_ATTUNEMENT = new KeyMapping(
            "key.earth_online_magic.open_attunement",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY);
    private static ArcaneStatusPayload arcaneStatus = ArcaneStatusPayload.empty();
    private EarthOnlineMagicClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EarthOnlineMagicClient::registerScreens);
        modBus.addListener(EarthOnlineMagicClient::registerLayerDefinitions);
        modBus.addListener(EarthOnlineMagicClient::registerEntityRenderers);
        modBus.addListener(EarthOnlineMagicClient::registerGuiLayers);
        modBus.addListener(EarthOnlineMagicClient::registerPayloadHandlers);
        modBus.addListener(EarthOnlineMagicClient::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(EarthOnlineMagicClient::clientTick);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(EarthOnlineMagic.MAGIC_MACHINE_MENU.get(), MagicMachineScreen::new);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EarthOnlineMagic.ARCANE_SEAT.get(), InvisibleSeatRenderer::new);
        event.registerEntityRenderer(EarthOnlineMagic.RUNIC_WATCHER.get(), RunicWatcherRenderer::new);
        event.registerEntityRenderer(EarthOnlineMagic.AETHER_FOX.get(), context ->
                new FamiliarRenderer<>(context, AetherFoxModel.LAYER_LOCATION, AetherFoxModel::new,
                        EarthOnlineMagic.id("textures/entity/aether_fox.png"), 0.46F, 1.0F));
        event.registerEntityRenderer(EarthOnlineMagic.RUNE_WOLF.get(), context ->
                new FamiliarRenderer<>(context, RuneWolfModel.LAYER_LOCATION, RuneWolfModel::new,
                        EarthOnlineMagic.id("textures/entity/rune_wolf.png"), 0.52F, 1.0F));
        event.registerEntityRenderer(EarthOnlineMagic.MANA_WISP.get(), context ->
                new FamiliarRenderer<>(context, ManaWispModel.LAYER_LOCATION, ManaWispModel::new,
                        EarthOnlineMagic.id("textures/entity/mana_wisp.png"), 0.18F, 0.82F));
        event.registerEntityRenderer(EarthOnlineMagic.CRYSTAL_ARMORED_SPIDER.get(), CrystalSpiderRenderer::new);
        event.registerEntityRenderer(EarthOnlineMagic.ARCANE_SETTLER.get(), ArcaneSettlerRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RunicWatcherModel.LAYER_LOCATION, RunicWatcherModel::createBodyLayer);
        event.registerLayerDefinition(AetherFoxModel.LAYER_LOCATION, AetherFoxModel::createBodyLayer);
        event.registerLayerDefinition(RuneWolfModel.LAYER_LOCATION, RuneWolfModel::createBodyLayer);
        event.registerLayerDefinition(ManaWispModel.LAYER_LOCATION, ManaWispModel::createBodyLayer);
        event.registerLayerDefinition(CrystalSpiderModel.LAYER_LOCATION, CrystalSpiderModel::createBodyLayer);
        event.registerLayerDefinition(ArcaneSettlerModel.LAYER_LOCATION, ArcaneSettlerModel::createBodyLayer);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(EarthOnlineMagic.id("arcane_focus_hud"), ArcaneFocusHud::render);
    }

    private static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ArcaneStatusPayload.TYPE, EarthOnlineMagicClient::handleArcaneStatus);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_ATTUNEMENT);
    }

    private static void clientTick(ClientTickEvent.Post event) {
        while (OPEN_ATTUNEMENT.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.getConnection() != null) {
                requestOpenAttunement();
            }
        }
    }

    private static void handleArcaneStatus(ArcaneStatusPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            arcaneStatus = payload;
            if (payload.openScreen() && !(Minecraft.getInstance().gui.screen() instanceof ArcaneAttunementScreen)) {
                Minecraft.getInstance().gui.setScreen(new ArcaneAttunementScreen());
            }
        });
    }

    public static ArcaneStatusPayload arcaneStatus() {
        return arcaneStatus;
    }

    public static void requestArcaneFocus(ArcaneFocus focus) {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(focus.id()));
    }

    public static void requestStopAttuning() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.STOP_RIDING));
    }

    public static void requestOpenAttunement() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.OPEN_PANEL));
    }

    public static void requestPractice() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.PRACTICE));
    }

    public static void requestAttunementRefresh() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.REFRESH_STATUS));
    }

    public static void openHandbook() {
        Minecraft.getInstance().gui.setScreen(new MagicHandbookScreen());
    }
}
