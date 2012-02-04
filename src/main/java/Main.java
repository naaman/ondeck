import com.naamannewbold.ondeck.auth.AuthFilter;
import com.sun.jersey.api.core.ResourceConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
public class Main {
    public static void main(String... args) throws Exception {
        int port = port();
        String jdbcConfigURL = db();

        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        JDBCSessionIdManager sessionIdManager = sessionIdManager(jdbcConfigURL, server);
        server.setSessionIdManager(sessionIdManager);

        JDBCSessionManager sessionManager = sessionManager(sessionIdManager);

        context.setSessionHandler(new SessionHandler(sessionManager));
        server.setHandler(context);

        ServletHolder holder = ondeckServlets();
        context.addServlet(holder, "/*");

        server.start();
        server.join();
    }

    private static JDBCSessionManager sessionManager(JDBCSessionIdManager sessionIdManager) {
        JDBCSessionManager sessionManager = new JDBCSessionManager();
        sessionManager.setSessionIdManager(sessionIdManager);
        return sessionManager;
    }

    private static ServletHolder ondeckServlets() {
        ServletHolder holder = new ServletHolder(com.sun.jersey.spi.container.servlet.ServletContainer.class);
        holder.setInitParameter("com.sun.jersey.config.property.packages", "com.naamannewbold.ondeck");
        holder.setInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, AuthFilter.class.getName());
        return holder;
    }

    private static JDBCSessionIdManager sessionIdManager(String jdbcConfigURL, Server server) {
        JDBCSessionIdManager sessionIdManager = new JDBCSessionIdManager(server);
        sessionIdManager.setWorkerName("balin");
        sessionIdManager.setDriverInfo(new org.postgresql.Driver(), jdbcConfigURL);
        sessionIdManager.setScavengeInterval(60);
        return sessionIdManager;
    }

    private static String db() throws URISyntaxException {
        URI databaseURL = new URI(System.getProperty("DATABASE_URL", System.getenv("DATABASE_URL")));
        boolean useDbSSL = System.getProperty("DATABASE_SSL", System.getenv("DATABASE_SSL")).equalsIgnoreCase("true");
        String[] dbUserInfo = databaseURL.getUserInfo().split(":", 2);

        // parse DATABASE_URL into a jdbc string
        String jdbcConfigURL = "jdbc:postgresql://" + databaseURL.getHost() +
                ((databaseURL.getPort() != -1) ? ":" + databaseURL.getPort() : "") +
                ((databaseURL.getPath() != null) ? databaseURL.getPath() : "/" ) +
                "?user=" + dbUserInfo[0] +
                "&password=" + dbUserInfo[1];

        // only use SSL if the env calls for it
        if (useDbSSL)
            jdbcConfigURL += "&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
        
        return jdbcConfigURL;
    }

    private static Integer port() {
        return Integer.valueOf(System.getProperty("port", (System.getenv("PORT") != null) ? System.getenv("PORT") : "8080"));
    }
}
