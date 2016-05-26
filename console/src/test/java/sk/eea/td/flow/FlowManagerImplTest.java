package sk.eea.td.flow;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.rest.model.Connector;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class FlowManagerImplTest {

    private static final int N = 10;

    private static CountDownLatch startSignal = new CountDownLatch(1);

    private static CountDownLatch threadsCreatedSignal = new CountDownLatch(10);

    @InjectMocks
    private FlowManagerImpl flowManagerImpl = new FlowManagerImpl(Connector.EUROPEANA, Connector.HISTORYPIN, new SingleRunJobSelector());

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobRunRepository jobRunRepository;

    @Mock
    private ParamRepository paramRepository;

    @Mock
    private LogRepository logRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doAnswer((Answer<JobRun>) invocation -> {
            Job job = new Job();
            JobRun jobRun = new JobRun();
            jobRun.setStatus(JobRun.JobRunStatus.RUNNING);
            jobRun.setJob(job);
            Thread.sleep(500); // simulate long running DB job
            return jobRun;
        }).when(jobRunRepository).findNextJobRun(anyString(), anyString());
        when(paramRepository.findByJob(any(Job.class))).thenReturn(new HashSet<>());
    }

    @Test
    public void testTrigger() throws Exception {
        for (int i = 0; i < N; i++) { // create and start threads
            new Thread(() -> {
                try {
                    threadsCreatedSignal.countDown();
                    startSignal.await(); // wait for signal
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                flowManagerImpl.trigger(); // go for trigger
            }
            ).start();
        }

        threadsCreatedSignal.await(); // wait for all threads are created
        startSignal.countDown();

        // verify that only one thread had created its run
        verify(jobRunRepository, timeout(1000).times(1)).save((JobRun) any());
        verify(jobRunRepository, timeout(1000).times(1)).findNextJobRun(anyString(), anyString());
        verify(jobRunRepository, timeout(1000).times(1)).save((JobRun) any());
        verify(jobRepository, timeout(1000).times(1)).save((Job) any());
        verify(logRepository, timeout(1000).times(0)).save((Log) any());
    }
}
