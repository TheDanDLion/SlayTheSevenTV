package slayTheSevenTV.commands;

import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import basemod.devcommands.ConsoleCommand;

public class Speak extends ConsoleCommand {

    public Speak() {
        maxExtraTokens = 20;
        minExtraTokens = 1;
        requiresPlayer = true;
        simpleCheck = true;
    }

    @Override
    protected void execute(String[] tokens, int depth) {
        String phrase = "";
        for (int i = depth; i < tokens.length; i++) {
            phrase += tokens[i] + " ";
        }
        phrase = phrase.trim();
        if (AbstractDungeon.getMonsters() != null && AbstractDungeon.getMonsters().monsters.size() > 0)
            AbstractDungeon.actionManager.addToBottom(new TalkAction(AbstractDungeon.getMonsters().monsters.get(0), phrase));
    }
}
