package slayTheSevenTV.util;

import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.ui.SpeechWord;

import basemod.Pair;
import slayTheSevenTV.SlayTheSevenTVInitializer;
import slayTheSevenTV.patches.RenderEmotesPatch;

public class SevenTVEmoteRequests {

    private static final String EMOTE_SET_URL = "https://7tv.io/v3/emote-sets/";

    private static HashMap<String, Pair<String, Boolean>> emotes = new HashMap<String, Pair<String, Boolean>>();

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
                    JsonObject emote = it.next().getAsJsonObject();
                    JsonObject data = emote.get("data").getAsJsonObject();
                    String emoteName = emote.get("name").getAsString();
                    String url = "https:" + data.get("host").getAsJsonObject().get("url").getAsString();
                    boolean zeroWidth = data.get("flags").getAsInt() == 256;
                    emotes.put(emoteName, new Pair<String, Boolean>(url, zeroWidth));
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
        if (!emotes.containsKey(emoteName))
            return;

        SlayTheSevenTVInitializer.logger.info("fetching " + emoteName + " from 7TV");
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        SlayTheSevenTVInitializer.logger.info(emotes.get(emoteName).getKey());
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method("GET")
            .url(emotes.get(emoteName).getKey() + "/2x.gif")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("User-Agent", "sts/" + CardCrawlGame.TRUE_VERSION_NUM)
            .timeout(30000)
            .build();
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                SlayTheSevenTVInitializer.logger.info("received response from 7TV for getting emote: " + httpResponse.getStatus().getStatusCode());
                try {
                    RenderEmotesPatch.InputStreamField.emote.set(word, GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, httpResponse.getResultAsStream()));
                } catch (Exception e) {
                    e.printStackTrace();
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