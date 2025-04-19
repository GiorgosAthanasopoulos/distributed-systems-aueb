package giorgosathanasopoulos.com.github.distributed_systems_aueb.file;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.Scanner;

public class FileUtils {

    private static String s_Error = "";

    public static void writeToFile(OutputStream p_File, String p_Contents) {
        try (OutputStreamWriter writer = new OutputStreamWriter(p_File)) {
            writer.write(p_Contents);
            writer.flush();
        } catch (IOException e) {
            s_Error = e.getLocalizedMessage();
            Logger.error(
                    "FileUtils::writeToFile failed to write to file: " +
                            s_Error);
        }
    }

    public static Optional<String> readFile(String p_Path, boolean p_AddNewlines) {
        try (Scanner sc = new Scanner(new FileReader(p_Path))) {
            StringBuilder sb = new StringBuilder();

            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                if (p_AddNewlines) {
                    sb.append("\n");
                }
            }

            return Optional.of(sb.toString());
        } catch (IOException e) {
            s_Error = e.getLocalizedMessage();
            Logger.error(
                    "FileUtils::readFile failed to read from file " +
                            p_Path +
                            ": " +
                            s_Error);
        }

        return Optional.empty();
    }

    public static String getError() {
        return s_Error;
    }
}
