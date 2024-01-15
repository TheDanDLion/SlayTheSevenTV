package slayTheSevenTV.patches;

import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.ui.DialogWord;
import com.megacrit.cardcrawl.ui.SpeechWord;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import com.megacrit.cardcrawl.vfx.SpeechTextEffect;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import slayTheSevenTV.util.SevenTVEmoteRequests;

public class RenderEmotesPatch {

    private final static float Y_OFFSET = -25F * Settings.scale;

    // --- SPEECH BUBBLE --- //

    public static SpeechTextEffect addSpeechBubble(SpeechBubble instance, float x, float y, float duration, String msg, DialogWord.AppearEffect appearEffect) {
        SpeechTextEffect effect = new SpeechTextEffect(x, y, duration, msg, appearEffect);
        TextEffectField.fromBubble.set(effect, true);
        BubbleField.effect.set(instance, effect);
        return effect;
    }

    @SpirePatch2(
        clz = SpeechBubble.class,
        method = SpirePatch.CLASS
    )
    public static class BubbleField {
        public static SpireField<SpeechTextEffect> effect = new SpireField<>(() -> null);
    }

    @SpirePatch2(
        clz = SpeechBubble.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = { float.class, float.class, float.class, String.class, boolean.class }
    )
    public static class SetFlagWhenInSpeechBubblePatch {
        @SpireInstrumentPatch
        public static ExprEditor Editor() {
            return new ExprEditor() {
                public void edit(MethodCall m) {
                    if (m.getMethodName().equals("add")) {
                        try {
                            m.replace("{ $_ = $proceed(slayTheSevenTV.patches.RenderEmotesPatch.addSpeechBubble(this, x + effect_x, y + 124.0F * com.megacrit.cardcrawl.core.Settings.scale, duration, msg, com.megacrit.cardcrawl.ui.DialogWord.AppearEffect.BUMP_IN)); }");
                        } catch (CannotCompileException e) {
                                e.printStackTrace();
                        }
                    }
                }
            };
        }
    }

    @SpirePatch2(
        clz = SpeechBubble.class,
        method = "update"
    )
    public static class PauseBubbleUntilFetchingDonePatch {
        public static SpireReturn<Void> Prefix(SpeechBubble __instance) {
            if (!TextEffectField.doneFetching.get(BubbleField.effect.get(__instance))) {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }

    // --- SPEECH TEXT EFFECT --- //

    @SpirePatch2(
        clz = SpeechTextEffect.class,
        method = SpirePatch.CLASS
    )
    public static class TextEffectField {
        public static SpireField<Boolean> fromBubble = new SpireField<>(() -> false);
        public static SpireField<Boolean> doneFetching = new SpireField<>(() -> false);
    }

    @SpirePatch2(
        clz = SpeechTextEffect.class,
        method = "addWord"
    )
    public static class SetFromBubblePatch {
        public static void Postfix(SpeechTextEffect __instance, ArrayList<SpeechWord> ___words) {
            if (TextEffectField.fromBubble.get(__instance) && !___words.isEmpty()) {
                EmoteField.fromBubble.set(___words.get(___words.size() - 1), TextEffectField.fromBubble.get(__instance));
            }
        }

        @SpireInsertPatch(
            rloc = 58
            // locator = Locator.class
        )
        public static void Insert(SpeechTextEffect __instance, ArrayList<SpeechWord> ___words) {

        }

        // TODO: this should work, but I'm not sure why it doesn't. do SpireInserts not work with some classes?
        // private class Locator extends SpireInsertLocator {
        //     @Override
        //     public int[] Locate(CtBehavior ct) throws Exception {
        //         return LineFinder.findInOrder(ct, new Matcher.MethodCallMatcher(Scanner.class, "close"));
        //     }
        // }
    }

    @SpirePatch2(
        clz = SpeechTextEffect.class,
        method = "update"
    )
    public static class PauseTextEffectUntilDoneFetchingPatch {
        @SpireInsertPatch(
            rloc = 14
            // locator = Locator.class
        )
        public static SpireReturn<Void> Insert(SpeechTextEffect __instance, ArrayList<SpeechWord> ___words) {
            if (___words.isEmpty())
                return SpireReturn.Return();
            for (SpeechWord word : ___words)
                if (!EmoteField.doneFetching.get(word))
                    return SpireReturn.Return();
            TextEffectField.doneFetching.set(__instance, true);
            return SpireReturn.Continue();
        }

        // TODO: this should work, but it doesn't for some god-forsaken reason and I'm not sure why
        // private class Locator extends SpireInsertLocator {
        //     @Override
        //     public int[] Locate(CtBehavior ct) throws Exception {
        //         return new int[] { LineFinder.findAllInOrder(ct, new Matcher.MethodCallMatcher(Graphics.class, "getDeltaTime"))[1] };
        //     }
        // }
    }

    // --- SPEECH WORD --- //

    @SpirePatch2(
        clz = SpeechWord.class,
        method = SpirePatch.CLASS
    )
    public static class EmoteField {
        public static SpireField<Boolean> doneFetching = new SpireField<>(() -> false);
        public static SpireField<Boolean> fromBubble = new SpireField<>(() -> false);
        public static SpireField<Boolean> animated = new SpireField<>(() -> false);
        public static SpireField<Boolean> emoteSet = new SpireField<>(() -> false);
        public static SpireField<Animation<TextureRegion>> animation = new SpireField<>(() -> null);
        public static SpireField<TextureRegion> emote = new SpireField<>(() -> null);
        public static SpireField<Float> elapsed = new SpireField<>(() -> 0F);
    }

    @SpirePatch2(
        clz = SpeechWord.class,
        method = "update"
    )
    public static class FetchEmotePatch {
        public static void Postfix(SpeechWord __instance) {
            if (EmoteField.fromBubble.get(__instance) && !EmoteField.emoteSet.get(__instance)) {
                SevenTVEmoteRequests.getEmote(__instance);
                EmoteField.emoteSet.set(__instance, true);
            }
        }
    }

    @SpirePatch2(
        clz = SpeechWord.class,
        method = "render",
        paramtypez = { SpriteBatch.class }
    )
    public static class RenderEmotePatch {
        public static SpireReturn<Void> Prefix(SpeechWord __instance, SpriteBatch sb, float ___x, float ___y) {
            if (!EmoteField.doneFetching.get(__instance))
                return SpireReturn.Return();

            if (EmoteField.fromBubble.get(__instance) && EmoteField.emoteSet.get(__instance)) {
                boolean animated = EmoteField.animated.get(__instance);

                if (animated) {
                    Animation<TextureRegion> animation = EmoteField.animation.get(__instance);
                    if (animation != null) {
                        float elapsed = EmoteField.elapsed.get(__instance) + Gdx.graphics.getDeltaTime();
                        sb.draw(animation.getKeyFrame(elapsed), ___x, ___y + Y_OFFSET);
                        EmoteField.elapsed.set(__instance, elapsed);
                        return SpireReturn.Return();
                    }
                } else {
                    TextureRegion emote = EmoteField.emote.get(__instance);
                    if (emote != null) {
                        sb.draw(emote, ___x, ___y + Y_OFFSET);
                        return SpireReturn.Return();
                    }
                }
            }

            return SpireReturn.Continue();
        }
    }
}
