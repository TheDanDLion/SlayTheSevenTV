package slayTheSevenTV;

import basemod.BaseMod;
import basemod.ModLabeledButton;
import basemod.ModPanel;
import basemod.ModTextPanel;
import basemod.devcommands.ConsoleCommand;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.StartGameSubscriber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import slayTheSevenTV.commands.Speak;
import slayTheSevenTV.patches.FixModTextPanelPatch;
import slayTheSevenTV.util.IDCheckDontTouchPls;
import slayTheSevenTV.util.SevenTVEmoteRequests;
import slayTheSevenTV.util.TextureLoader;

@SpireInitializer
public class SlayTheSevenTVInitializer implements
    EditStringsSubscriber,
    PostInitializeSubscriber,
    StartGameSubscriber {

    public static final Logger logger = LogManager.getLogger(SlayTheSevenTVInitializer.class.getName());
    private static String modID;

    // Mod-settings settings. This is if you want an on/off savable button
    public static Properties slayTheSevenTVProperties = new Properties();
    public static final String EMOTE_SET_ID = "emoteSetId";
    public static String emoteSetId = "";

    //This is for the in-game mod settings panel.
    private static final String MODNAME = "Slay The 7TV";
    private static final String AUTHOR = "dandylion1740";
    private static final String DESCRIPTION = "Renders 7TV emotes in the speech bubbles.";

    //Mod Badge - A small icon that appears in the mod settings menu next to your mod.
    public static final String BADGE_IMAGE = "slayTheSevenTVResources/images/Badge.png";


    // =============== SUBSCRIBE, INITIALIZE =================

    public SlayTheSevenTVInitializer(){
        logger.info("Subscribe to SlayTheSevenTV hooks");

        BaseMod.subscribe(this);
        setModID("slayTheSevenTV");

        logger.info("Done subscribing");

        logger.info("Adding mod settings");
        slayTheSevenTVProperties.setProperty(EMOTE_SET_ID, emoteSetId);
        try {
            SpireConfig config = new SpireConfig("slayTheSevenTV", "slayTheSevenTVConfig", slayTheSevenTVProperties);
            config.load();
            emoteSetId = config.getString(EMOTE_SET_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Done adding mod settings");
    }

    // ====== NO EDIT AREA ======
    // DON'T TOUCH THIS STUFF. IT IS HERE FOR STANDARDIZATION BETWEEN MODS AND TO ENSURE GOOD CODE PRACTICES.
    // IF YOU MODIFY THIS I WILL HUNT YOU DOWN AND DOWNVOTE YOUR MOD ON WORKSHOP

    public static void setModID(String ID) { // DON'T EDIT
        Gson coolG = new Gson(); // EY DON'T EDIT THIS
        //   String IDjson = Gdx.files.internal("IDCheckStringsDONT-EDIT-AT-ALL.json").readString(String.valueOf(StandardCharsets.UTF_8)); // i hate u Gdx.files
        InputStream in = SlayTheSevenTVInitializer.class.getResourceAsStream("/IDCheckStringsDONT-EDIT-AT-ALL.json"); // DON'T EDIT THIS ETHER
        IDCheckDontTouchPls EXCEPTION_STRINGS = coolG.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), IDCheckDontTouchPls.class); // OR THIS, DON'T EDIT IT
        logger.info("You are attempting to set your mod ID as: " + ID); // NO WHY
        if (ID.equals(EXCEPTION_STRINGS.DEFAULTID)) { // DO *NOT* CHANGE THIS ESPECIALLY, TO EDIT YOUR MOD ID, SCROLL UP JUST A LITTLE, IT'S JUST ABOVE
            throw new RuntimeException(EXCEPTION_STRINGS.EXCEPTION); // THIS ALSO DON'T EDIT
        } else if (ID.equals(EXCEPTION_STRINGS.DEVID)) { // NO
            modID = EXCEPTION_STRINGS.DEFAULTID; // DON'T
        } else { // NO EDIT AREA
            modID = ID; // DON'T WRITE OR CHANGE THINGS HERE NOT EVEN A LITTLE
        } // NO
        logger.info("Success! ID is " + modID); // WHY WOULD U WANT IT NOT TO LOG?? DON'T EDIT THIS.
    } // NO

    public static String getModID() { // NO
        return modID; // DOUBLE NO
    } // NU-UH

    private static void pathCheck() { // ALSO NO
        Gson coolG = new Gson(); // NOPE DON'T EDIT THIS
        //   String IDjson = Gdx.files.internal("IDCheckStringsDONT-EDIT-AT-ALL.json").readString(String.valueOf(StandardCharsets.UTF_8)); // i still hate u btw Gdx.files
        InputStream in = SlayTheSevenTVInitializer.class.getResourceAsStream("/IDCheckStringsDONT-EDIT-AT-ALL.json"); // DON'T EDIT THISSSSS
        IDCheckDontTouchPls EXCEPTION_STRINGS = coolG.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), IDCheckDontTouchPls.class); // NAH, NO EDIT
        String packageName = SlayTheSevenTVInitializer.class.getPackage().getName(); // STILL NO EDIT ZONE
        FileHandle resourcePathExists = Gdx.files.internal(getModID() + "Resources"); // PLEASE DON'T EDIT THINGS HERE, THANKS
        if (!modID.equals(EXCEPTION_STRINGS.DEVID)) { // LEAVE THIS EDIT-LESS
            if (!packageName.equals(getModID())) { // NOT HERE ETHER
                throw new RuntimeException(EXCEPTION_STRINGS.PACKAGE_EXCEPTION + getModID()); // THIS IS A NO-NO
            } // WHY WOULD U EDIT THIS
            if (!resourcePathExists.exists()) { // DON'T CHANGE THIS
                throw new RuntimeException(EXCEPTION_STRINGS.RESOURCE_FOLDER_EXCEPTION + getModID() + "Resources"); // NOT THIS
            }// NO
        }// NO
    }// NO

    // ====== YOU CAN EDIT AGAIN ======

    //Used by @SpireInitializer
    public static void initialize(){
        logger.info("========================= Initializing Blank Mod. =========================");
        //This creates an instance of our classes and gets our code going after BaseMod and ModTheSpire initialize.
        new SlayTheSevenTVInitializer();
        logger.info("========================= /Blank Mod Initialized./ =========================");
    }

    // ============== /SUBSCRIBE, INITIALIZE/ =================


    // =============== POST-INITIALIZE =================

    @Override
    public void receivePostInitialize() {
        logger.info("Loading badge image and mod options");

        Texture badgeTexture = TextureLoader.getTexture(BADGE_IMAGE);

        ModPanel settingsPanel = new ModPanel();

        ModTextPanel emoteSetText = new ModTextPanel();

        ModLabeledButton fetchEmoteSetButton = new ModLabeledButton("Set Emote Set Id", 400F, 700F, settingsPanel, (button) -> {
            FixModTextPanelPatch.panelOpen = true;
            emoteSetText.show(settingsPanel,
                emoteSetId,
                "",
                "Set the ID from a 7TV emote set. You can find this value in the URL of your browser.\nThis will be saved to your computer. The emotes will be loaded when you start a run.",
                (panel) -> {},
                (panel) -> {
                    try {
                        String newEmoteSetId = ModTextPanel.textField;
                        if (!newEmoteSetId.isEmpty() && !newEmoteSetId.equals(emoteSetId)) {
                            emoteSetId = newEmoteSetId;
                            SpireConfig config = new SpireConfig("slayTheSevenTV", "slayTheSevenTVConfig", slayTheSevenTVProperties);
                            config.setString(EMOTE_SET_ID, newEmoteSetId);
                            config.save();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        });

        settingsPanel.addUIElement(fetchEmoteSetButton);

        BaseMod.registerModBadge(badgeTexture, MODNAME, AUTHOR, DESCRIPTION, settingsPanel);

        logger.info("Done loading badge Image and mod options");

        logger.info("Registering commands");

        ConsoleCommand.addCommand("speak", Speak.class);

        logger.info("Done registering commands");
    }

    // =============== /POST-INITIALIZE/ =================


    // ================ LOAD THE TEXT ===================

    @Override
    public void receiveEditStrings() {
        logger.info("Beginning to edit strings for mod with ID: " + getModID());
        pathCheck();
        logger.info("Done editing strings");
    }

    // ================ /LOAD THE TEXT/ ===================


    // ================ LOAD EMOTES ===================

    @Override
    public void receiveStartGame() {
        SevenTVEmoteRequests.loadEmotes(emoteSetId);
    }

    // ================ /LOAD EMOTES/ ===================


    // this adds "ModName:" before the ID of any card/relic/power etc.
    // in order to avoid conflicts if any other mod uses the same ID.
    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }
}
