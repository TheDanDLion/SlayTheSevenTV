package slayTheSevenTV.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.ui.SpeechWord;

import basemod.ReflectionHacks;
import slayTheSevenTV.util.SevenTVEmoteRequests;

public class RenderEmotesPatch {

    @SpirePatch2(
        clz = SpeechWord.class,
        method = SpirePatch.CLASS
    )
    public static class InputStreamField {
        public static SpireField<Animation<TextureRegion>> emote = new SpireField<>(() -> null);
        public static SpireField<Float> elapsed = new SpireField<>(() -> 0F);
    }

    @SpirePatch2(
        clz = SpeechWord.class,
        method = SpirePatch.CONSTRUCTOR
    )
    public static class FetchEmotePatch {
        public static void Postfix(SpeechWord __instance) {
            SevenTVEmoteRequests.getEmote(__instance);
        }
    }

    @SpirePatch2(
        clz = SpeechWord.class,
        method = "render",
        paramtypez = { SpriteBatch.class }
    )
    public static class RenderEmotePatch {
        public static SpireReturn<Void> Prefix(SpeechWord __instance, SpriteBatch sb) {
            Animation<TextureRegion> animation = InputStreamField.emote.get(__instance);
            if (animation != null) {
                float elapsed = InputStreamField.elapsed.get(__instance) + Gdx.graphics.getDeltaTime();
                float x = ReflectionHacks.getPrivate(__instance, float.class, "x");
                float y = ReflectionHacks.getPrivate(__instance, float.class, "y");
                sb.draw(animation.getKeyFrame(elapsed), x, y);
                InputStreamField.elapsed.set(__instance, elapsed);
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
}
