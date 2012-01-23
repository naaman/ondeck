import com.naamannewbold.ondeck.auth.AuthFilter;
import com.sun.jersey.api.core.ResourceConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.JDBCSessionIdManager;
import org.eclipse.jetty.server.session.JDBCSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.URI;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
public class Main {
    public static void main(String... args) throws Exception {
        int port = Integer.valueOf(System.getProperty("port", (System.getenv("PORT") != null) ? System.getenv("PORT") : "8080"));
        URI databaseURL = new URI(System.getProperty("DATABASE_URL", System.getenv("DATABASE_URL")));
        String[] dbUserInfo = databaseURL.getUserInfo().split(":", 2);

        String jdbcConfigURL = "jdbc:postgres://" + databaseURL.getHost() +
                ((databaseURL.getPort() != -1) ? ":" + databaseURL.getPort() : "") +
                ((databaseURL.getPath() != null) ? databaseURL.getPath() : "/" ) +
                "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory" +
                "&user=" + dbUserInfo[0] +
                "&password=" + dbUserInfo[1];

        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        JDBCSessionIdManager sessionIdManager = new JDBCSessionIdManager(server);
        sessionIdManager.setWorkerName("balin");
        sessionIdManager.setDriverInfo(new org.postgresql.Driver(), jdbcConfigURL);
        sessionIdManager.setScavengeInterval(60);
        server.setSessionIdManager(sessionIdManager);

        JDBCSessionManager sessionManager = new JDBCSessionManager();
        sessionManager.setSessionIdManager(sessionIdManager);
        context.setSessionHandler(new SessionHandler(sessionManager));

        server.setHandler(context);

        ServletHolder holder = new ServletHolder(com.sun.jersey.spi.container.servlet.ServletContainer.class);
        holder.setInitParameter("com.sun.jersey.config.property.packages", "com.naamannewbold.ondeck");
        holder.setInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, AuthFilter.class.getName());
        context.addServlet(holder, "/*");

        server.start();
        server.join();
    }
}
