package slayTheSevenTV.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.ui.SpeechWord;

import slayTheSevenTV.SlayTheSevenTVInitializer;
import slayTheSevenTV.patches.RenderEmotesPatch;

public class SevenTVEmoteRequests {

    private static final String EMOTE_SET_URL = "https://7tv.io/v3/emote-sets/";

    private static HashMap<String, SevenTVEmote> emotes = new HashMap<>();

    private final static int ZERO_WIDTH_FLAG = 256;

    public static void loadEmotes(String emoteSetId) {
        if (emoteSetId == null || emoteSetId.isEmpty())
            return;

        SlayTheSevenTVInitializer.logger.info("fetching emote set " + emoteSetId + " from 7TV");
        emotes.clear();
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method("GET")
            .url(EMOTE_SET_URL + emoteSetId)
            .header("Accept", "application/json")
            .header("User-Agent", "sts/" + CardCrawlGame.TRUE_VERSION_NUM)
            .timeout(0)
            .build();
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                SlayTheSevenTVInitializer.logger.info("received response from 7TV for getting emote set: " + httpResponse.getStatus().getStatusCode());
                Gson gson = new Gson();
                JsonObject jobj = gson.fromJson(httpResponse.getResultAsString(), JsonObject.class);
                JsonElement emoteArray = jobj.get("emotes");
                Iterator<JsonElement> it = emoteArray.getAsJsonArray().iterator();
                while (it.hasNext()) {
                    JsonObject emoteObj = it.next().getAsJsonObject();
                    JsonObject data = emoteObj.get("data").getAsJsonObject();
                    boolean animated = data.get("animated").getAsBoolean();
                    String emoteName = emoteObj.get("name").getAsString();
                    String ext = animated ? ".gif" : ".png";
                    String url = "https:" + data.get("host").getAsJsonObject().get("url").getAsString() + "/1x" + ext;

                    SevenTVEmote emote = new SevenTVEmote();
                    emote.url = url;
                    emote.zeroWidth = data.get("flags").getAsInt() == ZERO_WIDTH_FLAG;
                    emote.animated = animated;
                    emote.ext = ext;
                    emotes.put(emoteName, emote);
                }
                SlayTheSevenTVInitializer.logger.info("loaded " + emotes.size() + " emotes");
            }

            public void failed(Throwable t) {
                SlayTheSevenTVInitializer.logger.error("request to get 7TV emote set " + emoteSetId + " failed with message: " + t.getMessage());
            }

            public void cancelled() {
                SlayTheSevenTVInitializer.logger.info("cancelling request to get 7TV emote set " + emoteSetId);
            }
        });
    }

    public static void getEmote(SpeechWord word) {
        String emoteName = word.word;
        if (!emotes.containsKey(emoteName)) {
            RenderEmotesPatch.EmoteField.doneFetching.set(word, true);
            return;
        }

        SevenTVEmote emote = emotes.get(emoteName);
        if (!emote.internalPath.equals("")) {
            SlayTheSevenTVInitializer.logger.info("retrieving " + emoteName + " from cache");
            FileHandle readHandle = Gdx.files.absolute(emote.internalPath);
            if (emote.animated) {
                RenderEmotesPatch.EmoteField.animated.set(word, true);
                RenderEmotesPatch.EmoteField.animation.set(word, GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, readHandle.read()));
            } else {
                RenderEmotesPatch.EmoteField.emote.set(word, new TextureRegion(new Texture(readHandle)));
            }
            RenderEmotesPatch.EmoteField.doneFetching.set(word, true);
            return;
        }

        SlayTheSevenTVInitializer.logger.info("fetching " + emoteName + " from 7TV");
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method("GET")
            .url(emote.url)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("User-Agent", "sts/" + CardCrawlGame.TRUE_VERSION_NUM)
            .timeout(30000)
            .build();
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                SlayTheSevenTVInitializer.logger.info("received response from 7TV for getting emote: " + httpResponse.getStatus().getStatusCode());

                try {
                    File tempFile = File.createTempFile("emote-" + emoteName, emote.ext);
                    tempFile.deleteOnExit();

                    FileHandle writeHandle = Gdx.files.absolute(tempFile.getPath());
                    writeHandle.write(httpResponse.getResultAsStream(), true);

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            FileHandle readHandle = Gdx.files.absolute(tempFile.getPath());

                            if (emotes.get(emoteName).animated) {
                                RenderEmotesPatch.EmoteField.animated.set(word, true);
                                RenderEmotesPatch.EmoteField.animation.set(word, GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, readHandle.read()));
                            } else {
                                RenderEmotesPatch.EmoteField.emote.set(word, new TextureRegion(new Texture(readHandle)));
                            }

                            emote.internalPath = tempFile.getPath();
                            RenderEmotesPatch.EmoteField.doneFetching.set(word, true);
                            SlayTheSevenTVInitializer.logger.info("successfully stored " + emoteName + ". we did it, Joe!");
                        }
                    });
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            public void failed(Throwable t) {
                SlayTheSevenTVInitializer.logger.error("request to get 7TV emote " + emoteName + " failed with message: " + t.getMessage());
            }

            public void cancelled() {
                SlayTheSevenTVInitializer.logger.info("cancelling request to get 7TV emote " + emoteName);
            }
        });
    }
}