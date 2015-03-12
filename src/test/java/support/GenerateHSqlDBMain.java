package support;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author:xingchaowang
 * @date: 3/11/2015.
 */
public class GenerateHSqlDBMain {

    public static void main(String[] args) throws IOException {

        File f = new File("src/main/resources/sql/create-table.sql");
        BufferedReader reader = new BufferedReader(new FileReader(f));

        String line = null;
        StringBuilder b = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            b.append(line);
            b.append("\n");
        }
        reader.close();

        String str = b.toString();

        Pattern p = Pattern.compile("\\-\\-.*");
        Matcher matcher = p.matcher(str);
        str = matcher.replaceAll("");

        p = Pattern.compile("/\\*.*");
        matcher = p.matcher(str);
        str = matcher.replaceAll("");

        p = Pattern.compile("AUTO_INCREMENT");
        matcher = p.matcher(str);
        str = matcher.replaceAll("IDENTITY");

        p = Pattern.compile("PRIMARY KEY.*");
        matcher = p.matcher(str);
        str = matcher.replaceAll("");

        p = Pattern.compile("bigint\\(.*?\\)");
        matcher = p.matcher(str);
        str = matcher.replaceAll("integer");

        p = Pattern.compile("int\\(.*?\\)");
        matcher = p.matcher(str);
        str = matcher.replaceAll("int");

        p = Pattern.compile("DEFAULT.*,");
        matcher = p.matcher(str);
        str = matcher.replaceAll(",");

        p = Pattern.compile("ENGINE.*;");
        matcher = p.matcher(str);
        str = matcher.replaceAll(";");

        p = Pattern.compile("UNIQUE KEY (`.*?`) (\\(.*?\\))");
        matcher = p.matcher(str);
        str = matcher.replaceAll("constraint con_$1 unique $2");

        p = Pattern.compile("`(.*?)`");
        matcher = p.matcher(str);
        str = matcher.replaceAll("$1");


        System.out.println(str);

        FileWriter writer = new FileWriter("src/test/resources/sql/hsqldb-create-table.sql");
        writer.write(str);
        writer.close();
    }
}
