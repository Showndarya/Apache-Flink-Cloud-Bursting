import operator.MetricUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.isA;
import entity.URLWrapper;

@RunWith(MockitoJUnitRunner.class)
public class MetricUtilitiesTest {

    @Mock
    private URLWrapper urlWrapper;

    @InjectMocks
    private MetricUtilities metricUtilities;

    @Test
    public void testGetJobId() throws Exception {
        // Given
        HttpURLConnection con = mock(HttpURLConnection.class);
        BufferedReader in = mock(BufferedReader.class);
        when(urlWrapper.openConnection()).thenReturn(con);
        when(con.getInputStream()).thenReturn(mock(InputStream.class));

        when(in.readLine()).thenReturn("{\"jobs\":[{\"id\":\"job-123\",\"status\":\"RUNNING\"}]}");
        // When
        String jobId = metricUtilities.getjobid();
    }

    //expected 400 due to internal calls unless provided with right job id
    @Test
    public void testGetInputRate() throws Exception {
        // Given
        String jobId = "job-123";
        String taskName = "task-456";
        HttpURLConnection con = mock(HttpURLConnection.class);
        BufferedReader in = mock(BufferedReader.class);
        when(urlWrapper.openConnection()).thenReturn(con);
        when(con.getInputStream()).thenReturn(mock(InputStream.class));
        when(in.readLine()).thenReturn("{\"vertices\":[{\"name\":\"" + taskName + "\",\"id\":\"vertex-789\"}]}");
        when(in.readLine()).thenReturn("{\"metrics\":[{\"id\":\"vertex-789_numRecordsIn\",\"value\":10.0}]}");
        // When
        Double inputRate = metricUtilities.getInputRate(jobId, taskName);
    }

    //expected 400 due to internal calls unless provided with right job id
    @Test
    public void testGetBusyTime() throws Exception {
        // Given
        String jobId = "job-123";
        String taskName = "task-456";
        HttpURLConnection con = mock(HttpURLConnection.class);
        BufferedReader in = mock(BufferedReader.class);
        when(urlWrapper.openConnection()).thenReturn(con);
        when(con.getInputStream()).thenReturn(mock(InputStream.class));
        when(in.readLine()).thenReturn("{\"vertices\":[{\"name\":\"" + taskName + "\",\"id\":\"vertex-789\"}]}");
        when(in.readLine()).thenReturn("{\"metrics\":[{\"id\":\"vertex-789_busyTimeMsPerSecond\",\"value\":1000}]}");
        // When
        Long busyTime = metricUtilities.getBusyTime(jobId, taskName);
    }
}




