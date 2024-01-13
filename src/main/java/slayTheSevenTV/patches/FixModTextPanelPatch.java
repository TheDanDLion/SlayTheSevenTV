package slayTheSevenTV.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import basemod.ModTextPanel;
import slayTheSevenTV.SlayTheSevenTVInitializer;

public class FixModTextPanelPatch {
    
    public static boolean panelOpen = false;

    @SpirePatch2(
        clz = ModTextPanel.class,
        method = "updateYes"
    )
    public static class FixConfirmButtonPatch {
        public static void Postfix(ModTextPanel __instance) {
            if (panelOpen && __instance.yesHb.hovered && InputHelper.isMouseDown)
                __instance.confirm();
        }
    }

    @SpirePatch2(
        clz = ModTextPanel.class,
        method = "updateNo"
    )
    public static class FixCancelButtonPatch {
        public static void Postfix(ModTextPanel __instance) {
            if (panelOpen && __instance.noHb.hovered && InputHelper.isMouseDown)
                __instance.cancel();
        }
    }

    @SpirePatch2(
        clz = ModTextPanel.class,
        method = "resetToSettings"
    )
    public static class DontResetToSettingsPatch {
        public static SpireReturn<Void> Prefix() {
            if (panelOpen) {
                CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                CardCrawlGame.mainMenuScreen.lighten();
                SlayTheSevenTVInitializer.emoteSetId = ModTextPanel.textField;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
}
