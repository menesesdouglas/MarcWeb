import io.github.cdimascio.dotenv.Dotenv;

public class TestEnvLoading {
    public static void main(String[] args) {
        try {
            // Load .env file from backend directory
            Dotenv dotenv = Dotenv.configure().directory("backend").load();

            // Test if variables are loaded correctly
            String postgresUser = dotenv.get("POSTGRES_USER");
            String postgresPassword = dotenv.get("POSTGRES_PASSWORD");

            System.out.println("Environment variables loaded successfully:");
            System.out.println("POSTGRES_USER: " + postgresUser);
            System.out.println("POSTGRES_PASSWORD: " + postgresPassword);

            // Test if they match the expected values from .env file
            if ("marcuser".equals(postgresUser) && "salfsa".equals(postgresPassword)) {
                System.out.println("✓ Environment variables match expected values!");
            } else {
                System.out.println("✗ Environment variables do not match expected values!");
            }

        } catch (Exception e) {
            System.err.println("Error loading environment variables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}