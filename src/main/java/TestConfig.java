import java.io.InputStream;
import java.util.Properties;

public class TestConfig {
    private static final String CONFIG_FILE = "config.properties";

    private static final Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Unable to find " + CONFIG_FILE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAppUrl() {
        return properties.getProperty("test.app.url", "http://localhost:8080");
    }

    public static int getThreadCount() {
        int threadCount = Integer.parseInt(properties.getProperty("thread.count", "3"));
        return (int) (Thread.currentThread().getId() % threadCount + 1);
    }
}
