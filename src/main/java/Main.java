import com.naamannewbold.ondeck.auth.AuthFilter;
import com.sun.jersey.api.core.ResourceConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
public class Main {
    public static void main(String... args) throws Exception {
        int port = Integer.valueOf(System.getProperty("port", (System.getenv("PORT") != null) ? System.getenv("PORT") : "8080"));
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setSessionHandler(new SessionHandler(new HashSessionManager()));
        server.setHandler(context);
        
        ServletHolder holder = new ServletHolder(com.sun.jersey.spi.container.servlet.ServletContainer.class);
        holder.setInitParameter("com.sun.jersey.config.property.packages", "com.naamannewbold.ondeck");
        holder.setInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, AuthFilter.class.getName());
        context.addServlet(holder, "/*");

        server.start();
        server.join();
    }
}
