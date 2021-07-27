import com.team7.cli.Config;
import com.team7.cli.Parser;
import com.team7.exceptions.CommandLineException;

public class Entrypoint {
    public static void main(String[] args) {
        try {
            Config config = Parser.parseCommandLineArguments(args);
        } catch (CommandLineException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        };
    }
}