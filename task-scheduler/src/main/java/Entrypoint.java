import com.team7.parsing.Config;
import com.team7.parsing.CLIParser;

public class Entrypoint {
    public static void main(String[] args) {
        Config config = CLIParser.parseCommandLineArguments(args);
    }
}