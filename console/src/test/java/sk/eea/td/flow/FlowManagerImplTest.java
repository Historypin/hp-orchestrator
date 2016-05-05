package sk.eea.td.flow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FlowManagerImplTest {

    private static CountDownLatch startSignal = new CountDownLatch(1);

    private static final int N = 10;

    @InjectMocks
    private FlowManagerImpl flowManagerImpl = new FlowManagerImpl();

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
        Job job = new Job();
        when(jobRepository.findFirstByLastJobRunIsNullAndSourceIsInOrderByIdAsc(anyList())).thenReturn(job);
        when(paramRepository.findByJob(any(Job.class))).thenReturn(new HashSet<>());
        JobRun jobRun = new JobRun();
        when(jobRunRepository.save(any(JobRun.class))).thenReturn(jobRun);

    }

    @Test
    public void testTrigger() throws Exception {
        for (int i = 0; i < N; i++) { // create and start threads
            new Thread(() -> {
                try {
                    startSignal.await(); // wait for signal
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                flowManagerImpl.trigger(); // go for trigger
            }
            ).start();
        }

        // wait until all threads are created
        Thread.sleep(1000);
        // fire all threads
        startSignal.countDown();

        // verify that only one thread had created its run
        verify(jobRunRepository, times(2)).save((JobRun) any());
        verify(paramRepository, times(1)).findByJob(any());
        verify(jobRepository, times(1)).save((Job) any());
        verify(logRepository, times(0)).save((Log) any());

    }
}
