package skinsrestorer.shared.storage;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import skinsrestorer.shared.utils.DataFiles;

public class ConfigStorage {
	public DataFiles config = new DataFiles("plugins" + File.separator + "SkinsRestorer" + File.separator + "",
			"config");
	public boolean UPDATE_CHECK = true;
	public boolean USE_AUTOIN_SKINS = false;
	public boolean USE_BOT_FEATURE = false;
	public int SKIN_CHANGE_COOLDOWN = 30;
	public boolean MCAPI_ENABLED = true;
	public String GET_PROFILE_URL = "https://mcapi.ca/SR/?player={username}";
	public String GET_SKIN_PROFILE_URL = "https://mcapi.ca/name/uuid/{uuid}";
	public boolean USE_MYSQL = false;
	public String MYSQL_HOST = "localhost";
	public String MYSQL_PORT = "3306";
	public String MYSQL_DATABASE = "db";
	public String MYSQL_SKINTABLE = "Skins";
	public String MYSQL_PLAYERTABLE = "Skins";
	public String MYSQL_USERNAME = "admin";
	public String MYSQL_PASSWORD = "pass";
	public List<String> DISABLED_SKINS = new ArrayList<String>();
	public boolean DISABLE_SKIN_COMMAND = false;
	public String CUSTOMSKINS_USERNAME = null;
	public String CUSTOMSKINS_PASSWORD = null;
	public String CUSTOMSKINS_NAME = null;
	public String CUSTOMSKINS_ID = null;
	public String CUSTOMSKINS_AUTHTOKEN = null;
	public String CUSTOMSKINS_CLIENTTOKEN = null;

	private static final ConfigStorage instance = new ConfigStorage();

	public static final ConfigStorage getInstance() {
		return instance;
	}

	public void init(InputStream stream, boolean overWrite) {
		config.copyDefaults(stream, overWrite);
		config.reload();

		// It should add the new option :)
		if (config.getString("Disable Skin Command") == null) {
			config.set("Disable Skin Command", false);
			config.comment("Disable Skin Command",
					new String[] { "Turn this to true if you want to disable", "The /skin command." });
			config.save();
		}

		if (config.getString("MySQL.SkinTable") == null) {
			config.set("MySQL.SkinTable", "Skins");
			config.comment("MySQL.SkinTable", new String[] { "Skin data will be saved in this table." });
			config.save();
		}

		if (config.getString("MySQL.PlayerTable") == null) {
			config.set("MySQL.PlayerTable", "Players");
			config.comment("MySQL.PlayerTable",
					new String[] { "Player data about skins will be saved in this table." });
			config.save();
		}

		UPDATE_CHECK = config.getBoolean("Update Check");
		USE_AUTOIN_SKINS = config.getBoolean("Use AutoIn Skins");
		USE_BOT_FEATURE = config.getBoolean("Use Bot Feature");
		SKIN_CHANGE_COOLDOWN = config.getInt("Skin Change Cooldown");
		MCAPI_ENABLED = config.getBoolean("MCAPI.Enabled");
		GET_PROFILE_URL = config.getString("MCAPI.Get Profile URL");
		GET_SKIN_PROFILE_URL = config.getString("MCAPI.Get SkinProfile URL");
		DISABLED_SKINS = config.getStringList("Disabled Skins");
		USE_MYSQL = config.getBoolean("MySQL.Enabled");
		MYSQL_HOST = config.getString("MySQL.Host");
		MYSQL_PORT = config.getString("MySQL.Port");
		MYSQL_DATABASE = config.getString("MySQL.Database");
		MYSQL_SKINTABLE = config.getString("MySQL.SkinTable");
		MYSQL_PLAYERTABLE = config.getString("MySQL.PlayerTable");
		MYSQL_USERNAME = config.getString("MySQL.Username");
		MYSQL_PASSWORD = config.getString("MySQL.Password");
		DISABLE_SKIN_COMMAND = config.getBoolean("Disable Skin Command");
		CUSTOMSKINS_USERNAME = config.getString("CustomSkins.Username");
		CUSTOMSKINS_PASSWORD = config.getString("CustomSkins.Password");
		CUSTOMSKINS_NAME = config.getString("CustomSkins.Name");
		CUSTOMSKINS_ID = config.getString("CustomSkins.ID");
		CUSTOMSKINS_AUTHTOKEN = config.getString("CustomSkins.Authtoken");
		CUSTOMSKINS_CLIENTTOKEN = config.getString("CustomSkins.Clienttoken");
	}
}
