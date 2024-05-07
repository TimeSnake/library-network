/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.database.util.Database;

import java.util.HashMap;
import java.util.List;

public class NetworkVariables {

  public static final String NETWORK_NAME = "network_name";
  public static final String RULES_LINK = "rules_link";
  public static final String WEBSITE_LINK = "website_link";
  public static final String SUPPORT_EMAIL = "support_email";
  public static final String DISCORD_LINK = "discord_link";
  public static final String PATREON_LINK = "patreon_link";
  public static final String YOUTUBE_LINK = "youtube_link";
  public static final String PRIVACY_POLICY_LINK = "privacy_policy_link";
  public static final String DEFAULT_TEXTURE_PACK_LINK = "default_texture_pack_link";
  public static final String COINS_NAME = "coins_name";

  public static final List<String> KEYS = List.of(NETWORK_NAME, WEBSITE_LINK, SUPPORT_EMAIL,
      DISCORD_LINK, PATREON_LINK, YOUTUBE_LINK, DEFAULT_TEXTURE_PACK_LINK, PRIVACY_POLICY_LINK,
      COINS_NAME);

  private final HashMap<String, String> variables = new HashMap<>();

  public NetworkVariables() {

  }

  public void load() {
    for (String key : KEYS) {
      this.variables.put(key, Database.getNetwork().getValue(key));
    }
  }

  public String getValue(String key) {
    return this.variables.get(key);
  }

}
