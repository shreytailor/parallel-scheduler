import com.team7.cli.Config;
import com.team7.cli.Parser;

public class Entrypoint {
    public static void main(String[] args) {
        Config config = Parser.parseCommandLineArguments(args);
    }
}