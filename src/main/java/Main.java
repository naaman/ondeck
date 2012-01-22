import com.naamannewbold.ondeck.auth.AuthFilter;
import com.ovea.jetty.session.redis.RedisSessionIdManager;
import com.ovea.jetty.session.redis.RedisSessionManager;
import com.ovea.jetty.session.serializer.JsonSerializer;
import com.sun.jersey.api.core.ResourceConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.net.URL;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
public class Main {
    public static void main(String... args) throws Exception {
        int port = Integer.valueOf(System.getProperty("port", (System.getenv("PORT") != null) ? System.getenv("PORT") : "8080"));
        URI redisConfigURL = new URI(System.getProperty("REDISTOGO_URL", System.getenv("REDISTOGO_URL")));

        Server server = new Server(port);

        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(),
                redisConfigURL.getHost(),
                redisConfigURL.getPort(),
                Protocol.DEFAULT_TIMEOUT,
                redisConfigURL.getUserInfo().split(":",2)[1]);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        RedisSessionManager sessionManager = new RedisSessionManager(jedisPool, new JsonSerializer());
        sessionManager.setSessionPath("/");
        context.setSessionHandler(new SessionHandler(sessionManager));

        RedisSessionIdManager sessionIdManager = new RedisSessionIdManager(server, jedisPool);
        server.setSessionIdManager(sessionIdManager);

        server.setHandler(context);

        ServletHolder holder = new ServletHolder(com.sun.jersey.spi.container.servlet.ServletContainer.class);
        holder.setInitParameter("com.sun.jersey.config.property.packages", "com.naamannewbold.ondeck");
        holder.setInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, AuthFilter.class.getName());
        context.addServlet(holder, "/*");

        server.start();
        server.join();
    }
}
