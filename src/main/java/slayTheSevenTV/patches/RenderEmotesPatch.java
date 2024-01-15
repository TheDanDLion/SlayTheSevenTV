package slayTheSevenTV.patches;

import java.util.ArrayList;
import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.ui.DialogWord;
import com.megacrit.cardcrawl.ui.SpeechWord;
import com.megacrit.cardcrawl.ui.DialogWord.WordColor;
import com.megacrit.cardcrawl.ui.DialogWord.WordEffect;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import com.megacrit.cardcrawl.vfx.SpeechTextEffect;

import javassist.CannotCompileException;
import javassist.CtBehavior;
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
        public static SpireField<Boolean> isOneWord = new SpireField<>(() -> false);
    }

    @SpirePatch2(
        clz = SpeechTextEffect.class,
        method = SpirePatch.CONSTRUCTOR
    )
    public static class QuickWordCountPatch {
        public static void Postfix(SpeechTextEffect __instance, String msg) {
            TextEffectField.isOneWord.set(__instance, msg.split(" ").length == 1);
        }
    }

    @SpirePatch2(
        clz = SpeechTextEffect.class,
        method = "addWord"
    )
    public static class SetFromBubblePatch {
        public static void Postfix(SpeechTextEffect __instance, ArrayList<SpeechWord> ___words, GlyphLayout ___gl) {
            if (TextEffectField.fromBubble.get(__instance) && !___words.isEmpty()) {
                SpeechWord lastWord = ___words.get(___words.size() - 1);
                EmoteField.fromBubble.set(lastWord, TextEffectField.fromBubble.get(__instance));

                float width = EmoteField.wordWidth.get(lastWord);
                if (width == 0F) {
                    EmoteField.wordWidth.set(lastWord, ___gl.width);
                }

                if (!TextEffectField.isOneWord.get(__instance))
                    for (SpeechWord word : ___words)
                        EmoteField.isLarge.set(word, false);
            }
        }

        @SpireInsertPatch( // TODO: fix me, x is not actually being adjusted since it's getting reset by the other words, need to do ALL OF THEM HERE
            locator = Locator.class,
            localvars = { "word" }
        )
        public static SpireReturn<Void> Insert(SpeechTextEffect __instance, String word, BitmapFont ___font, DialogWord.AppearEffect ___a_effect, float ___x, float ___y, @ByRef int[] ___curLine, @ByRef float[] ___curLineWidth, float ___LINE_SPACING, float ___DEFAULT_WIDTH, float ___CHAR_SPACING, Scanner ___s, ArrayList<SpeechWord> ___words) {
            String size = "-1x";
            if (TextEffectField.isOneWord.get(__instance))
                size = "-3x";

            if (SevenTVEmoteRequests.emotes.containsKey(word + size)) {
                float width = SevenTVEmoteRequests.emotes.get(word + size).width;
                float temp = 0F;
                if (___curLineWidth[0] + width > ___DEFAULT_WIDTH) {
                    ___curLine[0]++;
                    for (SpeechWord w : ___words)
                        w.shiftY(___LINE_SPACING);
                    ___curLineWidth[0] = width + ___CHAR_SPACING;
                    temp = -___curLineWidth[0] / 2F;
                } else {
                    ___curLineWidth[0] += width;
                    temp = -___curLineWidth[0] / 2F;
                    for (SpeechWord w : ___words) {
                        if (w.line == ___curLine[0]) {
                            w.setX(___x + temp);
                            temp += EmoteField.wordWidth.get(w) + ___CHAR_SPACING;
                        }
                    }
                    ___curLineWidth[0] += ___CHAR_SPACING;
                }

                SpeechWord newWord = new SpeechWord(___font, word, ___a_effect, WordEffect.NONE, WordColor.WHITE, ___x + temp, ___y - ___LINE_SPACING * ___curLine[0], ___curLine[0]);
                EmoteField.wordWidth.set(newWord, width);
                ___words.add(newWord);

                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ct) throws Exception {
                return LineFinder.findInOrder(ct, new Matcher.MethodCallMatcher(GlyphLayout.class, "setText"));
            }
        }
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

        // TODO: this should work, but it doesn't for some reason, and I'm not sure why
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
        public static SpireField<Boolean> isLarge = new SpireField<>(() -> true);
        public static SpireField<Animation<TextureRegion>> animation = new SpireField<>(() -> null);
        public static SpireField<TextureRegion> emote = new SpireField<>(() -> null);
        public static SpireField<Float> elapsed = new SpireField<>(() -> 0F);
        public static SpireField<Float> wordWidth = new SpireField<>(() -> 0F);
    }

    @SpirePatch2(
        clz = SpeechWord.class,
        method = "update"
    )
    public static class FetchEmotePatch {
        public static void Postfix(SpeechWord __instance) {
            if (EmoteField.fromBubble.get(__instance) && !EmoteField.emoteSet.get(__instance)) {
                SevenTVEmoteRequests.getEmote(__instance, EmoteField.isLarge.get(__instance));
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
                        TextureRegion frame = animation.getKeyFrame(elapsed);
                        float y = (EmoteField.isLarge.get(__instance) ? -frame.getRegionHeight() / 2F : 0F);
                        sb.draw(frame, ___x, ___y + Y_OFFSET + y);
                        EmoteField.elapsed.set(__instance, elapsed);
                        return SpireReturn.Return();
                    }
                } else {
                    TextureRegion emote = EmoteField.emote.get(__instance);
                    if (emote != null) {
                        float y = (EmoteField.isLarge.get(__instance) ? -emote.getRegionHeight() / 2F : 0F);
                        sb.draw(emote, ___x, ___y + Y_OFFSET + y);
                        return SpireReturn.Return();
                    }
                }
            }

            return SpireReturn.Continue();
        }
    }
}
