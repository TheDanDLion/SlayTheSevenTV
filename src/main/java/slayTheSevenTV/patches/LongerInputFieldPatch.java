package slayTheSevenTV.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.helpers.FontHelper;

import basemod.ModTextPanel;
import javassist.CtBehavior;

public class LongerInputFieldPatch {
    
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
                return LineFinder.findAllInOrder(ct, new Matcher.MethodCallMatcher(FontHelper.class, "getSmartWidth"));
            }
        }
    }

}
