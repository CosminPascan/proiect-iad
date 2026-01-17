import org.apache.camel.main.Main;
import routes.ChannelRoutes;

public class MainApplication {
    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new ChannelRoutes());
        main.run(args);
    }
}
