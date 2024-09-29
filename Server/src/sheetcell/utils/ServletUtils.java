package sheetcell.utils;

import api.CellValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletContext;
import users.UserManager;
import api.Engine;
import dto.DTOFactoryImpl;
import impl.EngineImpl;
import utils.CellValueAdapter;

public class ServletUtils {

	private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
	private static final String ENGINE_ATTRIBUTE_NAME = "engine";

	private static final Object userManagerLock = new Object();
	private static final Object engineLock = new Object();

	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(CellValue.class, new CellValueAdapter())
			.create();

	public static UserManager getUserManager(ServletContext servletContext) {
		synchronized (userManagerLock) {
			if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
			}
		}
		return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
	}

	public static Engine getEngine(ServletContext servletContext) {
		synchronized (engineLock) {
			if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, new EngineImpl(new DTOFactoryImpl()));
			}
		}
		return (Engine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
	}

	public static Gson getGson() {
		return gson;
	}
}
