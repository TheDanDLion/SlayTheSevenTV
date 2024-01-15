package slayTheSevenTV.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import basemod.ModTextPanel;
import javassist.CtBehavior;
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

    @SpirePatch2(
        cls = "basemod.ModTextPanelInputHelper",
        method = "keyDown"
    )
    public static class ExtendLengthOfInputPatch {
        @SpireInsertPatch(
            locator = Locator.class
        )
        public static SpireReturn<Boolean> Insert(int keycode) {
            String tmp = Input.Keys.toString(keycode);
            if (!Gdx.input.isKeyPressed(59) && !Gdx.input.isKeyPressed(60))
                tmp = tmp.toLowerCase();
            char tmp2 = tmp.charAt(0);
            if (Character.isDigit(tmp2) || Character.isLetter(tmp2))
                ModTextPanel.textField = ModTextPanel.textField + tmp2;
            return SpireReturn.Return(true);
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ct) throws Exception {
                return LineFinder.findInOrder(ct, new Matcher.MethodCallMatcher(FontHelper.class, "getSmartWidth"));
            }
        }
    }
}
