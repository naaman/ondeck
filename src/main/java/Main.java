import com.naamannewbold.ondeck.auth.AuthFilter;
import com.naamannewbold.ondeck.config.DB;
import com.sun.jersey.api.core.ResourceConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.ejb.Ejb3Configuration;

import java.net.URI;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
public class Main {
    public static void main(String... args) throws Exception {
        int port = port();
        String jdbcConfigURL = DB.PG.jdbcUrl();

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

    private static String prop(String p, String e, String def) {
        String prop = System.getProperty(p, System.getenv(e));
        return (prop != null) ? prop : def;
    }

    private static Integer port() {
        return Integer.valueOf(prop("port", "PORT", "8080"));
    }
}
