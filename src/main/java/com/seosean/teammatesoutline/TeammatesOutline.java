package com.seosean.teammatesoutline;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mod(modid = TeammatesOutline.MODID, version = TeammatesOutline.VERSION)
public class TeammatesOutline
{
    public KeyBinding keyToggleTeammatesOutline = new KeyBinding("TeammatesOutline", Keyboard.KEY_NONE, "TeammatesOutline");
    public boolean teammatesOutline = false;
    public static final String MODID = "TeammatesOutline";
    public static final String VERSION = "1.0";
    private Minecraft mc = Minecraft.getMinecraft();
    public static TeammatesOutline instance;
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(keyToggleTeammatesOutline);

    }

    public static TeammatesOutline getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void toggleHideBlock(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKey() == keyToggleTeammatesOutline.getKeyCode() && Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent() && this.mc.currentScreen == null) {
            this.teammatesOutline  = ! this.teammatesOutline;
            if (this.teammatesOutline) {
                IChatComponent text = new ChatComponentText(EnumChatFormatting.YELLOW + "Toggle TeammatesOutline: " + EnumChatFormatting.GREEN + "ON");
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(text);
            } else {
                IChatComponent text = new ChatComponentText(EnumChatFormatting.YELLOW + "Toggle TeammatesOutline: " + EnumChatFormatting.RED + "OFF");
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(text);
            }
        }
    }

    public static List<String> scoreboardLines;

    public static String scoreboardTitle;

    public boolean isInZombies(){
        return scoreboardTitle != null
                && (scoreboardTitle.contains("ZOM")
                || scoreboardTitle.contains("僵")
                || scoreboardTitle.contains("殭"))
                && scoreboardLines.size() >= 13;
    }
    @SubscribeEvent
    public void getSB(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.isSingleplayer() ) {
            return;
        }

        if(event.phase!= TickEvent.Phase.START)
            return;
        EntityPlayerSP p = mc.thePlayer;
        if(p==null)
            return;


        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebarObjective == null) {
            return;
        }
        scoreboardTitle = sidebarObjective.getDisplayName();

        scoreboardLines = new ArrayList<>();
        Collection<Score> scores = scoreboard.getSortedScores(sidebarObjective);
        List<Score> filteredScores = scores.stream().filter(p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")).collect(Collectors.toList());
        if (filteredScores.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(filteredScores, scores.size() - 15));
        } else {
            scores = filteredScores;
        }

        Collections.reverse(filteredScores);

        for (Score line : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(line.getPlayerName());
            String scoreboardLine = ScorePlayerTeam.formatPlayerName(team, line.getPlayerName()).trim();
            scoreboardLines.add(scoreboardLine);
        }
    }

}


