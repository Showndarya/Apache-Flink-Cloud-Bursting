package operator;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;

import java.io.File;


public class CustomedFileSink {
    public static FileSink getSink(){
        /**
         * get home directory
         */
        String userHomeDir = System.getProperty("user.home");
        /**
         * get system separator
         */
        String dir=userHomeDir+"/FlinkFile";
        dir=dir.replaceAll("[\\/\\\\]", "\\"+ File.separator);
        System.out.println(dir);

        final FileSink<String> sink = FileSink
                .forRowFormat(new Path(dir), new SimpleStringEncoder<String>("UTF-8"))
                .withOutputFileConfig(
                        OutputFileConfig.builder()
                                .withPartSuffix(".txt")
                                .build()
                )
                .build();

        return sink;
    }

}
